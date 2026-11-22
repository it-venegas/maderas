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
import kotlinx.coroutines.launch

class AdminViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AsistenciaRepository.get(app)

    // ---------- Datos del panel de resumen e historial ----------

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

    // ---------- Formulario de registro manual ----------

    var runSeleccionado by mutableStateOf<String?>(null)
        private set
    var fecha by mutableStateOf(Fechas.hoy())
        private set
    var entrada by mutableStateOf("")
        private set
    var salida by mutableStateOf("")
        private set

    private var yaIntentoGuardar by mutableStateOf(false)

    val trabajadorError: String?
        get() = if (yaIntentoGuardar) Validators.validarTrabajador(runSeleccionado) else null
    val fechaError: String?
        get() = if (fecha.isNotEmpty() || yaIntentoGuardar) Validators.validarFecha(fecha) else null
    val entradaError: String?
        get() = if (entrada.isNotEmpty() || yaIntentoGuardar) Validators.validarHora(entrada) else null
    val salidaError: String?
        get() = if (salida.isNotEmpty() || yaIntentoGuardar) Validators.validarSalida(entrada, salida) else null

    val fechaValida: Boolean
        get() = fecha.isNotEmpty() && Validators.validarFecha(fecha) == null
    val entradaValida: Boolean
        get() = entrada.isNotEmpty() && Validators.validarHora(entrada) == null
    val salidaValida: Boolean
        get() = salida.isNotEmpty() && Validators.validarSalida(entrada, salida) == null

    private val formularioTieneErrores: Boolean
        get() = fechaError != null || entradaError != null || salidaError != null

    // ---------- Acciones sobre el formulario ----------

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

    // ---------- Carga y guardado ----------

    fun cargar() {
        viewModelScope.launch {
            val hoy = Fechas.hoy()
            historial = enSegundoPlano { repo.historial() }
            trabajadores = enSegundoPlano { repo.trabajadores() }
            presentes = enSegundoPlano { repo.presentes(hoy) }
            total = enSegundoPlano { repo.totalTrabajadores() }
        }
    }

    fun guardar() {
        yaIntentoGuardar = true
        val run = runSeleccionado
        if (run == null || formularioTieneErrores) return

        viewModelScope.launch {
            enSegundoPlano { repo.registrarManual(run, fecha, entrada, salida.ifBlank { null }) }
            mensaje = "Asistencia de ${nombreDelTrabajador(run)} registrada"
            vaciarFormulario()
            cargar()
        }
    }

    private fun nombreDelTrabajador(run: String): String =
        trabajadores.firstOrNull { it.run == run }?.nombre ?: ""

    private fun vaciarFormulario() {
        runSeleccionado = null
        fecha = Fechas.hoy()
        entrada = ""
        salida = ""
        yaIntentoGuardar = false
    }
}
