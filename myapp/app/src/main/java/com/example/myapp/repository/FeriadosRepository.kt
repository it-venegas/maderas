package com.example.myapp.repository

import com.example.myapp.network.CalendarioApi
import com.example.myapp.network.ErrorDeRed
import com.example.myapp.network.llamarServidor

class FeriadosRepository(private val calendarioApi: CalendarioApi) {
    private var feriadosCargados: Map<String, String>? = null

    suspend fun feriados(): Map<String, String> {
        feriadosCargados?.let { return it }
        return try {
            val feriados = llamarServidor { calendarioApi.feriados() }.associate { it.fecha to it.nombre }
            feriadosCargados = feriados
            feriados
        } catch (e: ErrorDeRed) {
            emptyMap()
        }
    }
}
