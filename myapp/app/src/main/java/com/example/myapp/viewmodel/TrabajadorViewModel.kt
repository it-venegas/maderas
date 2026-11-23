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
import com.example.myapp.network.ErrorDeRed
import com.example.myapp.repository.Recursos
import com.example.myapp.repository.Repositorios
import com.example.myapp.repository.Ubicacion
import kotlinx.coroutines.launch

private const val PORCENTAJE_MAXIMO = 100
private const val MENSAJE_SIN_DETALLE = "No se pudo completar la operación"

class TrabajadorViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorios.asistencias(app)
    private val repoFeriados = Repositorios.feriados()
    private var usuario: Usuario? = null

    var historial by mutableStateOf<List<Asistencia>>(emptyList())
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var feriados by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    // ---------- Jornada de hoy ----------

    val hoy: Asistencia?
        get() = historial.firstOrNull { it.fecha == Fechas.hoy() }

    val feriadoDeHoy: String?
        get() = feriados[Fechas.hoy()]

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
        cargarFeriados()
    }

    private fun cargarFeriados() {
        viewModelScope.launch { feriados = enSegundoPlano { repoFeriados.feriados() } }
    }

    private fun cargarHistorial() {
        val trabajador = usuario ?: return
        viewModelScope.launch {
            try {
                historial = enSegundoPlano { repo.historial(trabajador.run) }
            } catch (error: ErrorDeRed) {
                mensaje = error.message ?: MENSAJE_SIN_DETALLE
            }
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
            mensaje = try {
                enSegundoPlano { registrarEntradaOSalida(trabajador, foto, ubicacion) }
            } catch (error: ErrorDeRed) {
                error.message ?: MENSAJE_SIN_DETALLE
            }
            cargarHistorial()
        }
    }

    private suspend fun registrarEntradaOSalida(trabajador: Usuario, foto: Bitmap, ubicacion: Ubicacion?): String {
        val rutaDeLaFoto = Recursos.guardarFoto(getApplication(), foto)
        val registroDeHoy = repo.asistenciaDe(trabajador.run, Fechas.hoy())
        val avisoSinUbicacion = if (ubicacion == null) " (sin ubicación)" else ""

        return if (registroDeHoy == null) {
            val registro = repo.registrarEntrada(trabajador.run, rutaDeLaFoto, ubicacion)
            "Entrada registrada a las ${registro.entrada}$avisoSinUbicacion"
        } else if (registroDeHoy.salida == null) {
            val registro = repo.registrarSalida(registroDeHoy, rutaDeLaFoto)
            "Salida registrada a las ${registro.salida}$avisoSinUbicacion"
        } else {
            "Tu jornada de hoy ya está completa"
        }
    }
}
