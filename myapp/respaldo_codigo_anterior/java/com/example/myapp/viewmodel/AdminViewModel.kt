package com.example.myapp.viewmodel

import com.example.myapp.model.Asistencia
import com.example.myapp.model.Fechas
import com.example.myapp.model.Usuario
import com.example.myapp.model.Validators
import com.example.myapp.repository.AsistenciaRepository

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AsistenciaRepository.get(app)

    var historial by mutableStateOf<List<Asistencia>>(emptyList()); private set
    var trabajadores by mutableStateOf<List<Usuario>>(emptyList()); private set
    var presentes by mutableStateOf(0); private set
    var total by mutableStateOf(0); private set
    var mensaje by mutableStateOf<String?>(null); private set

    // Formulario de registro manual
    var runSeleccionado by mutableStateOf<String?>(null); private set
    var fecha by mutableStateOf(Fechas.hoy()); private set
    var entrada by mutableStateOf(""); private set
    var salida by mutableStateOf(""); private set
    private var intentoEnvio by mutableStateOf(false)
    private var fechaTocada by mutableStateOf(false)
    private var entradaTocada by mutableStateOf(false)
    private var salidaTocada by mutableStateOf(false)

    val trabajadorError get() = if (intentoEnvio) Validators.validarTrabajador(runSeleccionado) else null
    val fechaError get() = if (fechaTocada || intentoEnvio) Validators.validarFecha(fecha) else null
    val entradaError get() = if (entradaTocada || intentoEnvio) Validators.validarHora(entrada) else null
    val salidaError get() = if (salidaTocada || intentoEnvio) Validators.validarSalida(entrada, salida) else null
    val fechaValida get() = fechaTocada && Validators.validarFecha(fecha) == null
    val entradaValida get() = entradaTocada && Validators.validarHora(entrada) == null
    val salidaValida get() = salida.isNotBlank() && Validators.validarSalida(entrada, salida) == null

    private class Carga(val historial: List<Asistencia>, val trabajadores: List<Usuario>, val presentes: Int, val total: Int)

    fun cargar() {
        viewModelScope.launch {
            val hoy = Fechas.hoy()
            val c = withContext(Dispatchers.IO) {
                Carga(repo.historial(), repo.trabajadores(), repo.presentes(hoy), repo.totalTrabajadores())
            }
            historial = c.historial
            trabajadores = c.trabajadores
            presentes = c.presentes
            total = c.total
        }
    }

    fun mensajeVisto() { mensaje = null }
    fun onTrabajador(run: String) { runSeleccionado = run }
    fun onFecha(v: String) { fecha = v.filter { it.isDigit() || it == '-' }.take(10); fechaTocada = true }
    fun onEntrada(v: String) { entrada = formatearHora(v); entradaTocada = true }
    fun onSalida(v: String) { salida = formatearHora(v); salidaTocada = true }

    /** Inserta ":" automáticamente: "0830" -> "08:30". */
    private fun formatearHora(v: String): String {
        val d = v.filter { it.isDigit() }.take(4)
        return if (d.length <= 2) d else d.take(2) + ":" + d.drop(2)
    }

    fun guardar() {
        intentoEnvio = true
        val run = runSeleccionado
        if (run == null || trabajadorError != null || fechaError != null || entradaError != null || salidaError != null) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.registrarManual(run, fecha, entrada, salida.ifBlank { null }) }
            val nombre = trabajadores.firstOrNull { it.run == run }?.nombre.orEmpty()
            mensaje = "Asistencia de $nombre registrada"
            runSeleccionado = null; entrada = ""; salida = ""; fecha = Fechas.hoy()
            intentoEnvio = false; fechaTocada = false; entradaTocada = false; salidaTocada = false
            cargar()
        }
    }
}
