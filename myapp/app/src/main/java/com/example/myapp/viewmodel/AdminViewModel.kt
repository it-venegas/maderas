package com.example.myapp.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.model.Asistencia
import com.example.myapp.model.Fechas
import com.example.myapp.model.Usuario
import com.example.myapp.model.Validators
import com.example.myapp.repository.AsistenciaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AsistenciaRepository.get(app)

    var historial by mutableStateOf<List<Asistencia>>(emptyList())
        private set
    var trabajadores by mutableStateOf<List<Usuario>>(emptyList())
        private set
    var presentes by mutableStateOf(0)
        private set
    var total by mutableStateOf(0)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    var runSeleccionado by mutableStateOf<String?>(null)
        private set
    var fecha by mutableStateOf(Fechas.hoy())
        private set
    var entrada by mutableStateOf("")
        private set
    var salida by mutableStateOf("")
        private set

    private var intento by mutableStateOf(false)

    val trabajadorError: String?
        get() = if (intento) Validators.validarTrabajador(runSeleccionado) else null
    val fechaError: String?
        get() = if (fecha.isNotEmpty() || intento) Validators.validarFecha(fecha) else null
    val entradaError: String?
        get() = if (entrada.isNotEmpty() || intento) Validators.validarHora(entrada) else null
    val salidaError: String?
        get() = if (salida.isNotEmpty() || intento) Validators.validarSalida(entrada, salida) else null

    val fechaValida: Boolean
        get() = fecha.isNotEmpty() && Validators.validarFecha(fecha) == null
    val entradaValida: Boolean
        get() = entrada.isNotEmpty() && Validators.validarHora(entrada) == null
    val salidaValida: Boolean
        get() = salida.isNotEmpty() && Validators.validarSalida(entrada, salida) == null

    fun onTrabajador(run: String) {
        runSeleccionado = run
    }

    fun onFecha(texto: String) {
        fecha = texto.filter { it.isDigit() || it == '-' }.take(10)
    }

    fun onEntrada(texto: String) {
        entrada = texto.filter { it.isDigit() || it == ':' }.take(5)
    }

    fun onSalida(texto: String) {
        salida = texto.filter { it.isDigit() || it == ':' }.take(5)
    }

    fun mensajeVisto() {
        mensaje = null
    }

    fun cargar() {
        viewModelScope.launch {
            val hoy = Fechas.hoy()
            historial = withContext(Dispatchers.IO) { repo.historial() }
            trabajadores = withContext(Dispatchers.IO) { repo.trabajadores() }
            presentes = withContext(Dispatchers.IO) { repo.presentes(hoy) }
            total = withContext(Dispatchers.IO) { repo.totalTrabajadores() }
        }
    }

    fun guardar() {
        intento = true
        val run = runSeleccionado
        if (run == null || fechaError != null || entradaError != null || salidaError != null) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repo.registrarManual(run, fecha, entrada, salida.ifBlank { null })
            }
            val nombre = trabajadores.firstOrNull { it.run == run }?.nombre ?: ""
            mensaje = "Asistencia de $nombre registrada"

            runSeleccionado = null
            fecha = Fechas.hoy()
            entrada = ""
            salida = ""
            intento = false
            cargar()
        }
    }
}
