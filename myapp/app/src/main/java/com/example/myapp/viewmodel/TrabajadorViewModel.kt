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
import kotlinx.coroutines.launch

private const val PORCENTAJE_MAXIMO = 100

class TrabajadorViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AsistenciaRepository.get(app)
    private var usuario: Usuario? = null

    var historial by mutableStateOf<List<Asistencia>>(emptyList())
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    // ---------- Jornada de hoy ----------

    val hoy: Asistencia?
        get() = historial.firstOrNull { it.fecha == Fechas.hoy() }

    // ---------- Resumen de la semana ----------

    private val registrosDeEstaSemana: List<Asistencia>
        get() {
            val lunes = Fechas.inicioSemana(Fechas.hoy())
            return historial.filter { it.fecha >= lunes }
        }

    val minutosSemana: Int
        get() = registrosDeEstaSemana.sumOf { minutosTrabajados(it) }

    val diasSemana: Int
        get() = registrosDeEstaSemana.size

    val porcentaje: Int
        get() {
            val diasEsperadosHastaHoy = minOf(Fechas.DIAS_LABORALES_POR_SEMANA, Fechas.diaSemana(Fechas.hoy()))
            return minOf(PORCENTAJE_MAXIMO, diasSemana * PORCENTAJE_MAXIMO / diasEsperadosHastaHoy)
        }

    private fun minutosTrabajados(registro: Asistencia): Int {
        val minutosDeEntrada = Fechas.minutos(registro.entrada) ?: return 0
        val minutosDeSalida = Fechas.minutos(registro.salida) ?: return 0
        return if (minutosDeSalida > minutosDeEntrada) minutosDeSalida - minutosDeEntrada else 0
    }

    // ---------- Carga del historial ----------

    fun cargar(u: Usuario) {
        if (usuario?.run != u.run) historial = emptyList()
        usuario = u
        cargarHistorial()
    }

    private fun cargarHistorial() {
        val trabajador = usuario ?: return
        viewModelScope.launch {
            historial = enSegundoPlano { repo.historial(trabajador.run) }
        }
    }

    // ---------- Mensajes para la pantalla ----------

    fun avisar(texto: String) {
        mensaje = texto
    }

    fun mensajeVisto() {
        mensaje = null
    }

    // ---------- Marcar entrada o salida ----------

    fun marcar(foto: Bitmap, ubicacion: Ubicacion?) {
        val trabajador = usuario ?: return
        viewModelScope.launch {
            mensaje = enSegundoPlano { registrarEntradaOSalida(trabajador, foto, ubicacion) }
            cargarHistorial()
        }
    }

    private fun registrarEntradaOSalida(trabajador: Usuario, foto: Bitmap, ubicacion: Ubicacion?): String {
        val rutaDeLaFoto = Recursos.guardarFoto(getApplication(), foto)
        val fecha = Fechas.hoy()
        val hora = Fechas.ahora()
        val registroDeHoy = repo.asistenciaDe(trabajador.run, fecha)
        val avisoSinUbicacion = if (ubicacion == null) " (sin ubicación)" else ""

        return if (registroDeHoy == null) {
            repo.registrarEntrada(trabajador.run, fecha, hora, ubicacion?.lat, ubicacion?.lng, rutaDeLaFoto)
            "Entrada registrada a las $hora$avisoSinUbicacion"
        } else if (registroDeHoy.salida == null) {
            repo.registrarSalida(registroDeHoy.id, hora, rutaDeLaFoto)
            "Salida registrada a las $hora$avisoSinUbicacion"
        } else {
            "Tu jornada de hoy ya está completa"
        }
    }
}
