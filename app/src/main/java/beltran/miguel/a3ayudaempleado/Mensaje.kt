package beltran.miguel.a3ayudaempleado

import com.google.firebase.database.PropertyName

data class Mensaje(
    var fecha:String="",
    var idTrabajo:String="",
    var mensaje:String="",
    var remitente:String=""
)