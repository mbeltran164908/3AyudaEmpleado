package beltran.miguel.a3ayudaempleado

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_perfil.*
import kotlinx.android.synthetic.main.trabajos.view.*

class PerfilActivity : AppCompatActivity() {
    var listaTrabajos=ArrayList<Trabajos>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        obtenerPerfil()
        var adaptador=AdaptadorTrabajos(this, listaTrabajos)
        lista_trabajos.adapter=adaptador

        btn_salir.setOnClickListener {
            cerrarSesion()
        }
    }

    fun obtenerPerfil(){
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val currentUserId=acct.email
            if(currentUserId!=null){
                var mFirestore = FirebaseFirestore.getInstance()
                mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
                mFirestore
                    .collection("perfiles").document(currentUserId)
                    .get()
                    .addOnSuccessListener { document ->
                            var perfil: Perfil? = null
                            if (document.exists()) {
                                var perfil = document.toObject(Perfil::class.java) ?: Perfil()
                                tv_nombre.setText(perfil.nombre)
                                tv_edad.setText(perfil.edad.toString())
                                tv_servicios.setText(perfil.servicios)
                            }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al recibir datos!", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun cerrarSesion(){
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
    private class AdaptadorTrabajos: BaseAdapter {
        var trabajo=ArrayList<Trabajos>()
        var contexto: Context?=null

        constructor(contexto: Context, trabajo:ArrayList<Trabajos>){
            this.contexto=contexto
            this.trabajo=trabajo
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var trabaj=trabajo[position]
            var inflador= LayoutInflater.from(contexto)
            var vista=inflador.inflate(R.layout.mensajes,null)

            vista.trabajo_titulo.setText(trabaj.trabajo)
            vista.trabajo_nombre.setText(trabaj.nombre)

            vista.trabajo_select.setOnClickListener{
                var intent = Intent(contexto, ChatActivity::class.java)
                intent.putExtra("trabajo",trabaj.trabajo)
                intent.putExtra("nombre",trabaj.nombre)
                intent.putExtra("listaTrabajo",trabaj.listaMensaje)
                contexto!!.startActivity(intent)
            }

            return vista
        }

        override fun getItem(position: Int): Any {
            return trabajo[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return trabajo.size
        }
    }
}
