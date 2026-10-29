package com.example.myapp

import com.example.myapp.model.Rol
import com.example.myapp.ui.AdminScreen
import com.example.myapp.ui.LoginScreen
import com.example.myapp.ui.TrabajadorScreen
import com.example.myapp.viewmodel.SesionViewModel

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.ui.theme.MyappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyappTheme {
                val context = LocalContext.current
                val sesion: SesionViewModel = viewModel()

                // Navegación entre vistas según sesión y rol, con transición animada
                Crossfade(targetState = sesion.usuario, animationSpec = tween(300), label = "navegacion") { usuario ->
                    if (usuario == null) {
                        LoginScreen(
                            onLoginSuccess = sesion::iniciar,
                            onOlvidoPassword = {
                                Toast.makeText(context, "Recuperación de contraseña: pendiente", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else if (usuario.rol == Rol.ADMINISTRADOR) {
                        AdminScreen(usuario, onCerrarSesion = sesion::cerrar)
                    } else {
                        TrabajadorScreen(usuario, onCerrarSesion = sesion::cerrar)
                    }
                }
            }
        }
    }
}
