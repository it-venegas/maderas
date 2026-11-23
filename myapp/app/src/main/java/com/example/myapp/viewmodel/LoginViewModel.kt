package com.example.myapp.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.model.Rol
import com.example.myapp.model.Usuario
import com.example.myapp.model.Validators
import com.example.myapp.network.ErrorDeRed
import com.example.myapp.repository.Repositorios
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorios.asistencias(app)

    // ---------- Lo que se muestra en la pantalla ----------

    var rol by mutableStateOf(Rol.TRABAJADOR)
        private set
    var run by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var verPassword by mutableStateOf(false)
        private set
    var recordar by mutableStateOf(false)
        private set
    var cargando by mutableStateOf(false)
        private set
    var errorGeneral by mutableStateOf<String?>(null)
        private set

    private var yaIntentoIngresar by mutableStateOf(false)

    val runError: String?
        get() = if (run.isNotEmpty() || yaIntentoIngresar) Validators.validarRun(run) else null
    val passwordError: String?
        get() = if (password.isNotEmpty() || yaIntentoIngresar) Validators.validarPassword(password) else null

    val runValido: Boolean
        get() = run.isNotEmpty() && Validators.validarRun(run) == null
    val passwordValida: Boolean
        get() = password.isNotEmpty() && Validators.validarPassword(password) == null

    // ---------- Acciones sobre el formulario ----------

    fun cambiarRol(nuevo: Rol) {
        rol = nuevo
        errorGeneral = null
    }

    fun escribirRun(texto: String) {
        run = texto.filter { it.isDigit() || it == 'k' || it == 'K' || it == '-' }.uppercase().take(10)
        errorGeneral = null
    }

    fun escribirPassword(texto: String) {
        password = texto
        errorGeneral = null
    }

    fun alternarPassword() {
        verPassword = !verPassword
    }

    fun alternarRecordar() {
        recordar = !recordar
    }

    fun mostrarError(texto: String) {
        errorGeneral = texto
    }

    // ---------- Ingreso con RUN y contraseña ----------

    fun ingresar(onExito: (Usuario, Boolean) -> Unit) {
        yaIntentoIngresar = true
        if (Validators.validarRun(run) != null || Validators.validarPassword(password) != null) return

        cargando = true
        viewModelScope.launch {
            try {
                val usuario = enSegundoPlano { autenticarYGuardarUltimoRun() }
                terminarIngreso(usuario, onExito)
            } catch (error: ErrorDeRed) {
                errorGeneral = error.message
            } finally {
                cargando = false
            }
        }
    }

    private suspend fun autenticarYGuardarUltimoRun(): Usuario? {
        val usuario = repo.autenticar(run, password)
        if (usuario != null && usuario.rol == rol) repo.guardarUltimoRun(usuario.run)
        return usuario
    }

    private fun terminarIngreso(usuario: Usuario?, onExito: (Usuario, Boolean) -> Unit) {
        if (usuario == null) {
            errorGeneral = "RUN o contraseña incorrectos"
        } else if (usuario.rol != rol) {
            errorGeneral = mensajeRolNoPermitido()
        } else {
            limpiar()
            onExito(usuario, recordar)
        }
    }

    // ---------- Ingreso con huella ----------

    fun entrarConHuella(onExito: (Usuario, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val usuario = enSegundoPlano { repo.ultimoUsuario() }
                if (usuario == null) {
                    errorGeneral = "Primero ingresa una vez con tu RUN y contraseña"
                } else if (usuario.rol != rol) {
                    errorGeneral = mensajeRolNoPermitido()
                } else {
                    limpiar()
                    onExito(usuario, recordar)
                }
            } catch (error: ErrorDeRed) {
                errorGeneral = error.message
            }
        }
    }

    // ---------- Apoyo interno ----------

    private fun mensajeRolNoPermitido() = "Esta cuenta no tiene acceso como ${rol.etiqueta}"

    private fun limpiar() {
        run = ""
        password = ""
        verPassword = false
        yaIntentoIngresar = false
        errorGeneral = null
    }
}
