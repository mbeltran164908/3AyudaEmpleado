package beltran.miguel.a3ayudaempleado

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_perfil.*
import kotlinx.android.synthetic.main.mensajes.view.*

class ChatActivity : AppCompatActivity() {
    var listaMensajes=ArrayList<Mensaje>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        var adaptador=AdaptadorMensajes(this,listaMensajes)
        lista_mensajes.adapter=adaptador

    }

    private class AdaptadorMensajes: BaseAdapter {
        var mensaje=ArrayList<Mensaje>()
        var contexto: Context?=null

        constructor(contexto: Context, mensaje:ArrayList<Mensaje>){
            this.contexto=contexto
            this.mensaje=mensaje
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var mensaj=mensaje[position]
            var inflador= LayoutInflater.from(contexto)
            var vista=inflador.inflate(R.layout.mensajes,null)

            vista.nombre_mensaje.setText(mensaj.nombre)
            vista.contenido_mensaje.setText(mensaj.mensaje)


            return vista
        }

        override fun getItem(position: Int): Any {
            return mensaje[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mensaje.size
        }
    }

}
