package beltran.miguel.a3ayudaempleado

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.mensaje_enviado.view.*
import kotlinx.android.synthetic.main.mensajes.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : AppCompatActivity() {

    var locacion: String? = null
    val db = FirebaseFirestore.getInstance()
    var listaMensajes = ArrayList<Mensaje>()
    var idTrabajo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        var adaptador = AdaptadorMensajes(this, listaMensajes)
        lv_mensajes.adapter = adaptador

        btn_enviar.setOnClickListener {
            enviarMensaje()
        }
        btn_cancelar_trabajo.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Terminar trabajo")
            builder.setMessage("¿Desea terminar el trabajo?")
            builder.setPositiveButton("Si") { dialog, which -> actualizarTrabajo() }
            builder.setNegativeButton("No") { dialog, which -> null }
            builder.show()
        }

        configurarVistaTitulo()
        agregarListenerMensajes()
        agregarListenerLocacion()
    }


    private fun agregarListenerLocacion() {
        btn_lugar_chat.setOnClickListener {
            var packageManager = this.packageManager
            val mapIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(locacion)
            )
            var list = packageManager.queryIntentActivities(
                mapIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (list.size > 0) {
                mapIntent.setPackage("com.google.android.apps.maps")
                this.startActivity(mapIntent)
            } else {
                val appPackageName = "com.google.android.apps.maps"
                try {
                    this.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (anfe: ActivityNotFoundException) {
                    this.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                }
            }
        }
    }

    private fun configurarVistaTitulo() {
        var bundle: Bundle? = intent.extras
        when (bundle!!.getString("estado")) {
            "cancelado" -> ViewCompat.setBackgroundTintList(
                layout_chat_fondo,
                ContextCompat.getColorStateList(this, R.color.naranja)
            )
            "terminado" -> ViewCompat.setBackgroundTintList(
                layout_chat_fondo,
                ContextCompat.getColorStateList(this, R.color.colorBoton)
            )
        }
        tv_nombre_chat.text = bundle.getString("nombre")
        tv_correo_chat.text = bundle.getString("idCliente")
        tv_fecha_chat.text = bundle.getString("fecha")
        locacion = bundle.getString("locacion")
        idTrabajo = bundle.getString("idTrabajo")
    }

    private fun enviarMensaje() {
        if (!et_mensaje.text.isBlank()) {
            val sdf = SimpleDateFormat("hh:mm:ss a dd/M/yyyy")
            val currentDate = sdf.format(Calendar.getInstance().time)
            val sender = GoogleSignIn.getLastSignedInAccount(this)?.displayName
            val mensaje = Mensaje(currentDate, idTrabajo!!, et_mensaje.text.toString(), sender!!)

            db.collection("mensajes").add(mensaje)
            et_mensaje.setText("")
        }
    }

    private fun agregarListenerMensajes() {
        db.collection("mensajes")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("Error", "Listen failed.", e)
                    return@addSnapshotListener
                }
                for (doc in value!!) {
                    var mensaje = doc.toObject(Mensaje::class.java)
                    if (mensaje.idTrabajo.equals(idTrabajo)) {
                        if (!listaMensajes.contains(mensaje)) {
                            listaMensajes.add(mensaje)
                        }
                    }
                }
                actualizarLista()
            }
    }

    private fun actualizarLista() {
        var adaptador = AdaptadorMensajes(this, listaMensajes)
        lv_mensajes.adapter = adaptador
    }

    private class AdaptadorMensajes : BaseAdapter {
        var mensaje: List<Mensaje>? = null
        var contexto: Context? = null
        var usuarioActual: String? = null

        constructor(contexto: Context, mensaje: ArrayList<Mensaje>) {
            this.contexto = contexto
            this.mensaje = mensaje.sortedWith(compareBy { it.fecha })
            usuarioActual = GoogleSignIn.getLastSignedInAccount(contexto)?.displayName
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var mensaj = mensaje!![position]
            var inflador = LayoutInflater.from(contexto)
            var vista: View
            if (mensaj.remitente.equals(usuarioActual)) {
                vista = inflador.inflate(R.layout.mensaje_enviado, null)
                vista.tv_nombre_mensaje!!.text = mensaj.remitente
                vista.tv_fecha_mensaje!!.text = mensaj.fecha
                vista.contenido_mensaje!!.text = mensaj.mensaje
            } else {
                vista = inflador.inflate(R.layout.mensajes, null)
                vista.tv_nombre_mensaje1!!.text = mensaj.remitente
                vista.tv_fecha_mensaje1!!.text = mensaj.fecha
                vista.contenido_mensaje1!!.text = mensaj.mensaje
            }
            return vista
        }

        override fun getItem(position: Int): Any {
            return mensaje!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mensaje!!.size
        }
    }

    private fun actualizarTrabajo(){
            var trabajoRef = db.collection("trabajos").document(idTrabajo!!);
            trabajoRef.update("estado", "terminado")
                .addOnSuccessListener {
                    Toast.makeText(this,"Trabajo terminado!",Toast.LENGTH_SHORT).show()
                    updateUI()
                }

    }

    private fun updateUI(){
        val intent = Intent(this, TrabajosActivity::class.java)
        startActivity(intent)
    }

}
