package com.example.myapp.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.model.Usuario
import com.example.myapp.repository.Repositorios
import kotlinx.coroutines.launch

class SesionViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorios.asistencias(app)

    var usuario by mutableStateOf<Usuario?>(null)
        private set

    var cargando by mutableStateOf(true)
        private set

    init {
        recuperarSesionGuardada()
    }

    fun iniciar(usuarioQueIngresa: Usuario, recordar: Boolean) {
        if (recordar) repo.recordarSesion(usuarioQueIngresa) else repo.olvidarSesion()
        usuario = usuarioQueIngresa
    }

    fun cerrar() {
        repo.olvidarSesion()
        usuario = null
    }

    private fun recuperarSesionGuardada() {
        viewModelScope.launch {
            val usuarioGuardado = enSegundoPlano { repo.sesionRecordada() }
            if (usuario == null) usuario = usuarioGuardado
            cargando = false
        }
    }
}
