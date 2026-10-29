package com.example.myapp.viewmodel

import com.example.myapp.model.Rol
import com.example.myapp.model.RunUtils
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

/**
 * Estado del login. Las reglas viven en [Validators]; aquí solo se decide CUÁNDO mostrar los errores.
 * La UI observa runError/passwordError y reacciona sola ante cada cambio de estado.
 */
class LoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AsistenciaRepository.get(app)

    var rol by mutableStateOf(Rol.TRABAJADOR); private set
    var run by mutableStateOf(""); private set
    var password by mutableStateOf(""); private set
    var passwordVisible by mutableStateOf(false); private set
    var cargando by mutableStateOf(false); private set
    var errorGeneral by mutableStateOf<String?>(null); private set

    private var runTocado by mutableStateOf(false)
    private var passTocado by mutableStateOf(false)

    val runError: String? get() = if (runTocado) Validators.validarRun(run) else null
    val passwordError: String? get() = if (passTocado) Validators.validarPassword(password) else null
    val runValido: Boolean get() = run.isNotEmpty() && Validators.validarRun(run) == null
    val passwordValida: Boolean get() = password.isNotEmpty() && Validators.validarPassword(password) == null

    fun onRol(r: Rol) { rol = r; errorGeneral = null }
    fun onRun(v: String) { run = v.filter { it.isDigit() || it in "kK.-" }.uppercase().take(12); runTocado = true; errorGeneral = null }
    fun onPassword(v: String) { password = v; passTocado = true; errorGeneral = null }
    fun togglePasswordVisible() { passwordVisible = !passwordVisible }

    fun ingresar(onExito: (Usuario) -> Unit) {
        runTocado = true; passTocado = true
        if (Validators.validarRun(run) != null || Validators.validarPassword(password) != null) return
        cargando = true
        viewModelScope.launch {
            val usuario = withContext(Dispatchers.IO) { repo.autenticar(RunUtils.normalizar(run), password) }
            cargando = false
            when {
                usuario == null -> errorGeneral = "RUN o contraseña incorrectos"
                usuario.rol != rol -> errorGeneral = "Esta cuenta no tiene acceso como ${rol.etiqueta}"
                else -> { limpiar(); onExito(usuario) }
            }
        }
    }

    private fun limpiar() {
        run = ""; password = ""; passwordVisible = false
        runTocado = false; passTocado = false; errorGeneral = null
    }
}
