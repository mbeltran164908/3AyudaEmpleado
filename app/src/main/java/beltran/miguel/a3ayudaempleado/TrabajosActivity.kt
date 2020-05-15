package beltran.miguel.a3ayudaempleado

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_trabajos.*
import kotlinx.android.synthetic.main.trabajos.view.*

class TrabajosActivity : AppCompatActivity() {

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
        //obtenerTrabajos();
        agregarListenerTrabajos()
    }

    private fun actualizarLista() {
        val adaptador = AdaptadorTrabajos(this, listaTrabajos)
        lv_trabajos.adapter = adaptador
    }

    private fun getIdUsuario(): String? {
        return GoogleSignIn.getLastSignedInAccount(this)?.email
    }

    private fun agregarListenerTrabajos() {
        db.collection("trabajos")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("Error", "Listen failed.", e)
                    return@addSnapshotListener
                }
                for (doc in value!!) {
                    val trabajo = doc.toObject(Trabajo::class.java)
                    if (trabajo.idEmpleado.equals(GoogleSignIn.getLastSignedInAccount(this)?.email)) {
                        if (!listaTrabajos.contains(trabajo)) {
                            listaTrabajos.add(trabajo)
                        }
                    }
                }
                actualizarLista()
            }
    }

    private class AdaptadorTrabajos : BaseAdapter {
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

            vista.tv_correo.text = trabajo.idCliente
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
                val intent = Intent(contexto!!, ChatActivity::class.java)
                intent.putExtra("idTrabajo", trabajo.idTrabajo)
                intent.putExtra("idCliente", trabajo.idCliente)
                intent.putExtra("fecha", trabajo.fechaCreacion)
                intent.putExtra("nombre", trabajo.nombreCliente)
                intent.putExtra("estado", trabajo.estado)
                contexto!!.startActivity(intent)
            }

            vista.btn_lugar.setOnClickListener {
                val gmmIntentUri =
                    Uri.parse(trabajo.locacion)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                contexto!!.startActivity(mapIntent)
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
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun updateUI(){
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
    }
}
