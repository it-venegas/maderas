package com.example.myapp.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.model.Asistencia
import com.example.myapp.model.Fechas
import com.example.myapp.model.Usuario
import com.example.myapp.repository.AsistenciaRepository
import com.example.myapp.repository.Recursos
import com.example.myapp.repository.Ubicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrabajadorViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AsistenciaRepository.get(app)
    private var usuario: Usuario? = null

    var historial by mutableStateOf<List<Asistencia>>(emptyList())
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    val hoy: Asistencia?
        get() = historial.firstOrNull { it.fecha == Fechas.hoy() }

    val minutosSemana: Int
        get() {
            val lunes = Fechas.inicioSemana(Fechas.hoy())
            var total = 0
            for (a in historial) {
                val entrada = Fechas.minutos(a.entrada)
                val salida = Fechas.minutos(a.salida)
                if (a.fecha >= lunes && entrada != null && salida != null && salida > entrada) {
                    total += salida - entrada
                }
            }
            return total
        }

    val diasSemana: Int
        get() = historial.count { it.fecha >= Fechas.inicioSemana(Fechas.hoy()) }

    val porcentaje: Int
        get() {
            val esperados = minOf(5, Fechas.diaSemana(Fechas.hoy()))
            return minOf(100, diasSemana * 100 / esperados)
        }

    fun cargar(u: Usuario) {
        if (usuario?.run != u.run) historial = emptyList()
        usuario = u
        cargarHistorial()
    }

    fun avisar(texto: String) {
        mensaje = texto
    }

    fun mensajeVisto() {
        mensaje = null
    }

    private fun cargarHistorial() {
        val u = usuario ?: return
        viewModelScope.launch {
            historial = withContext(Dispatchers.IO) { repo.historial(u.run) }
        }
    }

    fun marcar(foto: Bitmap, ubicacion: Ubicacion?) {
        val u = usuario ?: return
        viewModelScope.launch {
            val texto = withContext(Dispatchers.IO) {
                val ruta = Recursos.guardarFoto(getApplication(), foto)
                val fecha = Fechas.hoy()
                val hora = Fechas.ahora()
                val existente = repo.asistenciaDe(u.run, fecha)
                val aviso = if (ubicacion == null) " (sin ubicación)" else ""

                if (existente == null) {
                    repo.registrarEntrada(u.run, fecha, hora, ubicacion?.lat, ubicacion?.lng, ruta)
                    "Entrada registrada a las $hora$aviso"
                } else if (existente.salida == null) {
                    repo.registrarSalida(existente.id, hora, ruta)
                    "Salida registrada a las $hora$aviso"
                } else {
                    "Tu jornada de hoy ya está completa"
                }
            }
            mensaje = texto
            cargarHistorial()
        }
    }
}
