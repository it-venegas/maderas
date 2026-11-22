package com.example.myapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.model.Rol
import com.example.myapp.ui.AdminScreen
import com.example.myapp.ui.LoginScreen
import com.example.myapp.ui.TrabajadorScreen
import com.example.myapp.ui.theme.FondoPantalla
import com.example.myapp.ui.theme.MyappTheme
import com.example.myapp.viewmodel.SesionViewModel

private const val DURACION_CAMBIO_DE_PANTALLA_MS = 300

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyappTheme {
                val sesion: SesionViewModel = viewModel()

                if (sesion.cargando) {
                    FondoMientrasSeRevisaLaSesion()
                } else {
                    PantallaSegunLaSesion(sesion)
                }
            }
        }
    }
}

/** Se revisa si hay una sesión guardada antes de decidir qué pantalla mostrar. */
@Composable
private fun FondoMientrasSeRevisaLaSesion() {
    Box(Modifier.fillMaxSize().background(FondoPantalla))
}

/** Sin sesión: login. Con sesión: el panel del administrador o el del trabajador. */
@Composable
private fun PantallaSegunLaSesion(sesion: SesionViewModel) {
    val context = LocalContext.current

    Crossfade(
        targetState = sesion.usuario,
        animationSpec = tween(DURACION_CAMBIO_DE_PANTALLA_MS),
        label = "navegacion"
    ) { usuario ->
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
