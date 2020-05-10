package beltran.miguel.a3ayudaempleado

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_perfil.*

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        obtenerPerfil()

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
}
