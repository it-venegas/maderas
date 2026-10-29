package com.example.myapp.model


enum class Rol(val etiqueta: String) {
    TRABAJADOR("Trabajador"),
    ADMINISTRADOR("Administrador")
}

data class Usuario(val run: String, val nombre: String, val cargo: String, val rol: Rol) {
    val iniciales: String
        get() = nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val primerNombre: String get() = nombre.substringBefore(" ")
}

data class Asistencia(
    val id: Long,
    val run: String,
    val nombre: String,
    val fecha: String,
    val entrada: String?,
    val salida: String?,
    val lat: Double?,
    val lng: Double?,
    val foto: String?,
    val manual: Boolean
)
