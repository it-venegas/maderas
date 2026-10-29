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
import com.example.myapp.repository.AsistenciaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AsistenciaRepository.get(app)

    var rol by mutableStateOf(Rol.TRABAJADOR)
        private set
    var run by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var verPassword by mutableStateOf(false)
        private set
    var cargando by mutableStateOf(false)
        private set
    var errorGeneral by mutableStateOf<String?>(null)
        private set

    private var intento by mutableStateOf(false)

    val runError: String?
        get() = if (run.isNotEmpty() || intento) Validators.validarRun(run) else null
    val passwordError: String?
        get() = if (password.isNotEmpty() || intento) Validators.validarPassword(password) else null

    val runValido: Boolean
        get() = run.isNotEmpty() && Validators.validarRun(run) == null
    val passwordValida: Boolean
        get() = password.isNotEmpty() && Validators.validarPassword(password) == null

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

    fun mostrarError(texto: String) {
        errorGeneral = texto
    }

    fun ingresar(onExito: (Usuario) -> Unit) {
        intento = true
        if (Validators.validarRun(run) != null || Validators.validarPassword(password) != null) return

        cargando = true
        viewModelScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                val u = repo.autenticar(run, password)
                if (u != null && u.rol == rol) repo.guardarUltimoRun(u.run)
                u
            }
            cargando = false

            if (usuario == null) {
                errorGeneral = "RUN o contraseña incorrectos"
            } else if (usuario.rol != rol) {
                errorGeneral = "Esta cuenta no tiene acceso como ${rol.etiqueta}"
            } else {
                limpiar()
                onExito(usuario)
            }
        }
    }

    fun entrarConHuella(onExito: (Usuario) -> Unit) {
        viewModelScope.launch {
            val usuario = withContext(Dispatchers.IO) { repo.ultimoUsuario() }
            if (usuario == null) {
                errorGeneral = "Primero ingresa una vez con tu RUN y contraseña"
            } else {
                limpiar()
                onExito(usuario)
            }
        }
    }

    private fun limpiar() {
        run = ""
        password = ""
        verPassword = false
        intento = false
        errorGeneral = null
    }
}
