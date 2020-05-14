package beltran.miguel.a3ayudaempleado

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_registro.*
import java.io.ByteArrayOutputStream


class RegistroActivity : AppCompatActivity() {

    private var RESULT_LOAD_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            et_nombre.setText(acct.givenName + " " + acct.familyName)
            val personPhoto: Uri? = acct.photoUrl
            Picasso.get().load(personPhoto).into(iv_imagen)
        }
        btn_guardar.setOnClickListener {
            guardarPerfil()
        }

        iv_imagen.setOnClickListener {
            seleccionarImagen()
        }
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, RESULT_LOAD_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_LOAD_IMAGE) {
            iv_imagen.setImageURI(data?.data)
        }
    }

    private fun guardarPerfil() {
        if (!et_nombre.text.isBlank() && !et_edad.text.isBlank() && !et_servicios.text.isBlank()) {
            val acct = GoogleSignIn.getLastSignedInAccount(this)
            if (acct != null) {
                val usuarioPerfil = acct.email
                if (usuarioPerfil != null) {
                    val perfil = Perfil(
                        et_nombre.text.toString(),
                        Integer.parseInt(et_edad.text.toString()),
                        et_servicios.text.toString()
                    )
                    subirPerfil(usuarioPerfil, perfil)
                    updateUI()
                }
            }
        } else {
            Toast.makeText(this, "Todos los campos son necesarios!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun subirPerfil(id: String, perfil: Perfil) {
        var mFirestore = FirebaseFirestore.getInstance()
        mFirestore.collection("perfiles").document(id).set(perfil)
        subirImagen()
        Toast.makeText(this, "Perfil creado!", Toast.LENGTH_SHORT).show()
    }

    private fun subirImagen(){
        val storage=FirebaseStorage.getInstance()
        val storageRef=storage.reference
        val acct = GoogleSignIn.getLastSignedInAccount(this)?.email
        val imgRef = storageRef.child("perfiles/$acct.jpg")

        iv_imagen.isDrawingCacheEnabled = true
        iv_imagen.buildDrawingCache()
        var bitmap = (iv_imagen.drawable as BitmapDrawable).bitmap
        bitmap=Bitmap.createScaledBitmap(bitmap, 128, 128, true)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        var uploadTask = imgRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener {
        }
    }

    private fun updateUI() {
        val intent = Intent(this, TrabajosActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

}
