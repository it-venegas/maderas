package com.example.myapp.repository

import com.example.myapp.model.Asistencia
import com.example.myapp.model.Usuario

interface AsistenciaRepository {

    // ---------- Usuarios e inicio de sesión ----------

    suspend fun autenticar(run: String, password: String): Usuario?

    suspend fun trabajadores(): List<Usuario>

    // ---------- Último usuario (botón de huella) ----------

    fun guardarUltimoRun(run: String)

    suspend fun ultimoUsuario(): Usuario?

    // ---------- Sesión recordada ("Recuérdame") ----------

    fun recordarSesion(usuario: Usuario)

    fun olvidarSesion()

    suspend fun sesionRecordada(): Usuario?

    // ---------- Consulta de asistencias ----------

    suspend fun historial(run: String? = null): List<Asistencia>

    suspend fun asistenciaDe(run: String, fecha: String): Asistencia?

    // ---------- Registro de asistencias ----------

    suspend fun registrarEntrada(run: String, foto: String?, ubicacion: Ubicacion?): Asistencia

    suspend fun registrarSalida(registro: Asistencia, foto: String?): Asistencia

    suspend fun registrarManual(run: String, fecha: String, entrada: String, salida: String?)
}
