package com.example.myapp.viewmodel

import com.example.myapp.model.Asistencia
import com.example.myapp.model.Fechas
import com.example.myapp.model.Resumen
import com.example.myapp.model.ResumenSemana
import com.example.myapp.model.Usuario
import com.example.myapp.repository.AsistenciaRepository
import com.example.myapp.repository.Recursos
import com.example.myapp.repository.Ubicacion

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrabajadorViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AsistenciaRepository.get(app)
    private var usuario: Usuario? = null

    var historial by mutableStateOf<List<Asistencia>>(emptyList()); private set
    var mensaje by mutableStateOf<String?>(null); private set

    val hoy: Asistencia? get() = historial.firstOrNull { it.fecha == Fechas.hoy() }
    val resumen: ResumenSemana get() = Resumen.semana(historial, Fechas.hoy())

    fun cargar(u: Usuario) {
        if (usuario?.run != u.run) historial = emptyList()
        usuario = u
        refrescar()
    }

    fun avisar(texto: String) { mensaje = texto }
    fun mensajeVisto() { mensaje = null }

    private fun refrescar() {
        val u = usuario ?: return
        viewModelScope.launch { historial = withContext(Dispatchers.IO) { repo.historial(u.run) } }
    }

    /** Primer toque del día = entrada; segundo = salida. Guarda foto y ubicación del dispositivo. */
    fun marcar(foto: Bitmap, ubicacion: Ubicacion?) {
        val u = usuario ?: return
        viewModelScope.launch {
            val texto = withContext(Dispatchers.IO) {
                val ruta = Recursos.guardarFoto(getApplication(), foto)
                val fecha = Fechas.hoy()
                val hora = Fechas.ahora()
                val existente = repo.asistenciaDe(u.run, fecha)
                val sufijo = if (ubicacion == null) " (sin ubicación)" else ""
                when {
                    existente == null -> {
                        repo.registrarEntrada(u.run, fecha, hora, ubicacion?.lat, ubicacion?.lng, ruta)
                        "Entrada registrada a las $hora$sufijo"
                    }
                    existente.salida == null -> {
                        repo.registrarSalida(existente.id, hora, ruta)
                        "Salida registrada a las $hora$sufijo"
                    }
                    else -> "Tu jornada de hoy ya está completa"
                }
            }
            mensaje = texto
            refrescar()
        }
    }
}
