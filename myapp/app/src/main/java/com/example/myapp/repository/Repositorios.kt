package com.example.myapp.repository

import android.content.Context
import com.example.myapp.network.ClienteXano
import com.example.myapp.network.ConfiguracionServidor

object Repositorios {
    private var instanciaUnica: AsistenciaRepository? = null
    private val feriadosUnicos: FeriadosRepository by lazy { FeriadosRepository(ClienteXano.crearCalendario()) }

    fun feriados(): FeriadosRepository = feriadosUnicos

    @Synchronized
    fun asistencias(context: Context): AsistenciaRepository {
        val instancia = instanciaUnica ?: crear(context.applicationContext)
        instanciaUnica = instancia
        return instancia
    }

    private fun crear(context: Context): AsistenciaRepository =
        if (ConfiguracionServidor.usarServidor) crearRemoto(context) else AsistenciaRepositoryLocal(context)

    private fun crearRemoto(context: Context): AsistenciaRepository {
        val sesion = AlmacenDeSesion(context)
        val leerToken: () -> String? = { sesion.tokenActual }
        return AsistenciaRepositoryRemoto(
            authApi = ClienteXano.crearAuth(token = leerToken),
            asistenciaApi = ClienteXano.crearAsistencia(token = leerToken),
            sesion = sesion
        )
    }
}
