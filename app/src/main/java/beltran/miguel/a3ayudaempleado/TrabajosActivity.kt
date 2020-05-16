package beltran.miguel.a3ayudaempleado

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_trabajos.*
import kotlinx.android.synthetic.main.trabajos.view.*

class TrabajosActivity : AppCompatActivity() {

    var adaptador:AdaptadorTrabajos?=null
    val db = FirebaseFirestore.getInstance()
    var listaTrabajos = ArrayList<Trabajo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trabajos)
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        btn_perfil.setOnClickListener {
            updateUI()
        }
        adaptador = AdaptadorTrabajos(this, listaTrabajos)
        lv_trabajos.adapter = adaptador
        agregarListenerTrabajos()
    }

    private fun agregarListenerTrabajos() {
        db.collection("trabajos")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("Error", "Listen failed.", e)
                    return@addSnapshotListener
                }
                for (doc in value!!) {
                    var trabajo = doc.toObject(Trabajo::class.java)
                    if (trabajo.idEmpleado.equals(GoogleSignIn.getLastSignedInAccount(this)?.email)) {
                        if (!trabajo.estado.equals("terminado")) {
                            if(!listaTrabajos.contains(trabajo)){
                                listaTrabajos.add(trabajo)
                            }
                        }else{
                            var listaIndex=ArrayList<Trabajo>()
                            for(item in listaTrabajos){
                                if(item.idTrabajo.equals(trabajo.idTrabajo)){
                                    listaIndex.add(item)
                                }
                            }
                            listaTrabajos.removeAll(listaIndex)
                        }
                        adaptador!!.setLista(listaTrabajos)
                        adaptador!!.notifyDataSetChanged()
                    }
                }
            }
    }

    class AdaptadorTrabajos : BaseAdapter {
        var trabajos: List<Trabajo>? = null
        var contexto: Context? = null

        constructor(contexto: Context, trabajos: ArrayList<Trabajo>) {
            this.contexto = contexto
            this.trabajos = trabajos.sortedWith(compareByDescending { it.fechaCreacion })
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val trabajo = trabajos!![position]
            val inflador = LayoutInflater.from(contexto)
            val vista = inflador.inflate(R.layout.trabajos, null)
            vista.tv_correo.text = trabajo.idTrabajo
            vista.tv_fecha.text = trabajo.fechaCreacion
            vista.tv_nombre.text = trabajo.nombreCliente
            vista.tv_estado.text = trabajo.estado

            when (trabajo.estado) {
                "cancelado" -> ViewCompat.setBackgroundTintList(
                    vista,
                    ContextCompat.getColorStateList(contexto!!, R.color.naranja)
                )
                "terminado" -> ViewCompat.setBackgroundTintList(
                    vista,
                    ContextCompat.getColorStateList(contexto!!, R.color.colorBoton)
                )
            }

            vista.setOnClickListener {
                if(!trabajo.estado.equals("terminado")){
                    val intent = Intent(contexto!!, ChatActivity::class.java)
                    intent.putExtra("idTrabajo", trabajo.idTrabajo)
                    intent.putExtra("idCliente", trabajo.idTrabajo)
                    intent.putExtra("fecha", trabajo.fechaCreacion)
                    intent.putExtra("nombre", trabajo.nombreCliente)
                    intent.putExtra("estado", trabajo.estado)
                    intent.putExtra("locacion",trabajo.locacion)
                    contexto!!.startActivity(intent)
                }else{
                    Toast.makeText(contexto!!,"Trabajo terminado",Toast.LENGTH_SHORT).show()
                }
            }

            vista.btn_lugar.setOnClickListener {
                if(!trabajo.estado.equals("terminado")) {
                    var packageManager = contexto!!.packageManager
                    val mapIntent = Intent(
                        android.content.Intent.ACTION_VIEW,
                        Uri.parse(trabajo.locacion)
                    )
                    var list = packageManager.queryIntentActivities(
                        mapIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                    if (list.size > 0) {
                        mapIntent.setPackage("com.google.android.apps.maps")
                        contexto!!.startActivity(mapIntent)
                    } else {
                        val appPackageName = "com.google.android.apps.maps"
                        try {
                            contexto!!.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$appPackageName")
                                )
                            )
                        } catch (anfe: ActivityNotFoundException) {
                            contexto!!.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                                )
                            )
                        }
                    }
                }
            }
            return vista
        }

        override fun getItem(position: Int): Any {
            return trabajos!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return trabajos!!.size
        }
        fun remove(id:Int) {
            trabajos!!.drop(id)
        }

        fun setLista(lista:ArrayList<Trabajo>){
            trabajos= lista.sortedWith(compareByDescending { it.fechaCreacion })
        }

    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun updateUI() {
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
    }

}
