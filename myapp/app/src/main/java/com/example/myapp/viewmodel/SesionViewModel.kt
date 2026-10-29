package com.example.myapp.viewmodel

import com.example.myapp.model.Usuario

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SesionViewModel : ViewModel() {
    var usuario by mutableStateOf<Usuario?>(null)
        private set

    fun iniciar(u: Usuario) { usuario = u }
    fun cerrar() { usuario = null }
}
