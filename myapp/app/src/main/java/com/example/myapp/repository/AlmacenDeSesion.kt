package com.example.myapp.repository

import android.content.Context
import com.example.myapp.model.Rol
import com.example.myapp.model.Usuario
import java.security.GeneralSecurityException

private const val NOMBRE_PREFERENCIAS_SESION = "cenamad_sesion"
private const val CLAVE_TOKEN = "token_cifrado"
private const val CLAVE_RUN = "usuario_run"
private const val CLAVE_NOMBRE = "usuario_nombre"
private const val CLAVE_CARGO = "usuario_cargo"
private const val CLAVE_ROL = "usuario_rol"

class SesionGuardada(val token: String, val usuario: Usuario)

class AlmacenDeSesion(context: Context) {
    private val preferencias = context.getSharedPreferences(NOMBRE_PREFERENCIAS_SESION, Context.MODE_PRIVATE)

    @Volatile
    var tokenActual: String? = null

    // ---------- Guardar y olvidar ----------

    fun guardar(token: String, usuario: Usuario) {
        preferencias.edit()
            .putString(CLAVE_TOKEN, CifradoDeToken.cifrar(token))
            .putString(CLAVE_RUN, usuario.run)
            .putString(CLAVE_NOMBRE, usuario.nombre)
            .putString(CLAVE_CARGO, usuario.cargo)
            .putString(CLAVE_ROL, usuario.rol.name)
            .apply()
    }

    fun olvidar() {
        tokenActual = null
        preferencias.edit().clear().apply()
    }

    // ---------- Leer la sesión guardada ----------

    fun recordada(): SesionGuardada? {
        val tokenCifrado = preferencias.getString(CLAVE_TOKEN, null)
        val run = preferencias.getString(CLAVE_RUN, null)
        val nombre = preferencias.getString(CLAVE_NOMBRE, null)
        val cargo = preferencias.getString(CLAVE_CARGO, null)
        val nombreDelRol = preferencias.getString(CLAVE_ROL, null)
        val rol = Rol.entries.firstOrNull { it.name == nombreDelRol }
        val token = if (tokenCifrado == null) null else descifrarToken(tokenCifrado)

        if (token == null || run == null || nombre == null || cargo == null || rol == null) {
            if (tokenCifrado != null) olvidar()
            return null
        }
        return SesionGuardada(token, Usuario(run, nombre, cargo, rol))
    }

    private fun descifrarToken(tokenCifrado: String): String? =
        try {
            CifradoDeToken.descifrar(tokenCifrado)
        } catch (e: GeneralSecurityException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
}
