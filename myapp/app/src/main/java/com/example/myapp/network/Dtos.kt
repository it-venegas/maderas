package com.example.myapp.network

import com.example.myapp.model.Asistencia
import com.example.myapp.model.Rol
import com.example.myapp.model.Usuario

// ---------- Lo que se envía al servidor ----------

data class LoginDto(val run: String, val password: String)

data class RegistroManualDto(
    val run: String,
    val fecha: String,
    val entrada: String,
    val salida: String?
)

// ---------- Lo que responde el servidor ----------

data class RespuestaLoginDto(val authToken: String, val usuario: UsuarioDto?)

data class UsuarioDto(
    val id: Int,
    val run: String,
    val nombre: String,
    val cargo: String,
    val rol: String
)

data class FeriadoDto(val fecha: String, val nombre: String)

data class AsistenciaDto(
    val id: Long,
    val run: String,
    val nombre: String,
    val fecha: String,
    val entrada: String?,
    val salida: String?,
    val foto: String?,
    val manual: Boolean
)

// ---------- Conversión al modelo de la aplicación ----------

fun UsuarioDto.aUsuario() = Usuario(
    run = run,
    nombre = nombre,
    cargo = cargo,
    rol = Rol.entries.firstOrNull { it.name == rol } ?: Rol.TRABAJADOR
)

fun AsistenciaDto.aAsistencia() = Asistencia(
    id = id,
    run = run,
    nombre = nombre,
    fecha = fecha,
    entrada = entrada,
    salida = salida,
    lat = null,
    lng = null,
    foto = foto,
    manual = manual
)
