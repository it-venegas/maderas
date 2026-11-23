package com.example.myapp.network

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.IOException
import retrofit2.HttpException

private const val CODIGO_NO_AUTORIZADO = 401
private const val CODIGO_DEMASIADAS_SOLICITUDES = 429
private const val MENSAJE_DEMASIADAS_SOLICITUDES = "Demasiadas solicitudes. Espera unos segundos e intenta de nuevo"
private const val MENSAJE_RESPUESTA_INESPERADA = "El servidor respondió con un formato inesperado"

sealed class ErrorDeRed(mensaje: String) : Exception(mensaje) {
    class SinConexion : ErrorDeRed("Sin conexión a internet")

    class NoAutorizado : ErrorDeRed("Tu sesión venció. Ingresa nuevamente")

    class ErrorDelServidor(mensaje: String, val codigoHttp: Int) : ErrorDeRed(mensaje)

    companion object {
        fun desde(error: HttpException): ErrorDeRed = when (error.code()) {
            CODIGO_NO_AUTORIZADO -> NoAutorizado()
            CODIGO_DEMASIADAS_SOLICITUDES -> ErrorDelServidor(MENSAJE_DEMASIADAS_SOLICITUDES, error.code())
            else -> ErrorDelServidor(mensajeDelServidor(error) ?: "Error del servidor (${error.code()})", error.code())
        }

        private fun mensajeDelServidor(error: HttpException): String? = try {
            val cuerpo = error.response()?.errorBody()?.string()
            if (cuerpo.isNullOrBlank()) null else JsonParser.parseString(cuerpo).asJsonObject.get("message")?.asString
        } catch (e: Exception) {
            null
        }
    }
}

internal suspend fun <T> llamarServidor(llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (e: IOException) {
        throw ErrorDeRed.SinConexion()
    } catch (e: HttpException) {
        throw ErrorDeRed.desde(e)
    } catch (e: JsonParseException) {
        throw ErrorDeRed.ErrorDelServidor(MENSAJE_RESPUESTA_INESPERADA, 0)
    }
