package com.example.myapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.model.Rol
import com.example.myapp.model.Usuario
import com.example.myapp.ui.theme.BordeCampo
import com.example.myapp.ui.theme.ErrorRojo
import com.example.myapp.ui.theme.FondoPantalla
import com.example.myapp.ui.theme.MyappTheme
import com.example.myapp.ui.theme.TextoPrincipal
import com.example.myapp.ui.theme.TextoSecundario
import com.example.myapp.ui.theme.VerdeCenamad
import com.example.myapp.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (Usuario) -> Unit = {},
    onOlvidoPassword: () -> Unit = {},
    vm: LoginViewModel = viewModel()
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).border(1.5.dp, VerdeCenamad, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Visibility, null, tint = VerdeCenamad, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("CENAMAD", fontSize = 16.sp, letterSpacing = 2.sp, color = TextoPrincipal)
                Text("CENTRO NACIONAL DE MADERAS", fontSize = 7.sp, letterSpacing = 1.sp, color = TextoSecundario)
            }
        }

        Spacer(Modifier.height(40.dp))
        Sobretitulo("BIENVENIDO/A")
        Spacer(Modifier.height(8.dp))
        Text("Inicia sesión", fontSize = 28.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal)
        Spacer(Modifier.height(6.dp))
        Text("Selecciona el tipo de acceso para continuar", fontSize = 13.sp, color = TextoSecundario)

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth()) {
            for (opcion in Rol.entries) {
                val activo = opcion == vm.rol
                val colorLinea by animateColorAsState(if (activo) VerdeCenamad else BordeCampo, label = "linea")
                Column(Modifier.weight(1f).clickable { vm.cambiarRol(opcion) }) {
                    Text(
                        opcion.etiqueta,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (activo) TextoPrincipal else TextoSecundario
                    )
                    Box(Modifier.fillMaxWidth().height(2.dp).background(colorLinea))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("RUN", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal)
        Spacer(Modifier.height(8.dp))
        CampoFormulario(
            valor = vm.run,
            onChange = vm::escribirRun,
            placeholder = "12345678-5",
            icono = Icons.Outlined.PersonOutline,
            error = vm.runError,
            valido = vm.runValido,
            ayuda = "Con guión y sin puntos, ej: 12345678-5",
            teclado = KeyboardType.Ascii
        )

        Spacer(Modifier.height(12.dp))
        Text("Contraseña", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal)
        Spacer(Modifier.height(8.dp))
        CampoFormulario(
            valor = vm.password,
            onChange = vm::escribirPassword,
            placeholder = "••••••••",
            icono = Icons.Outlined.LockOpen,
            error = vm.passwordError,
            valido = vm.passwordValida,
            teclado = KeyboardType.Password,
            transformacion = if (vm.verPassword) VisualTransformation.None else PasswordVisualTransformation(),
            extra = {
                IconButton(onClick = vm::alternarPassword) {
                    Icon(
                        if (vm.verPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Mostrar u ocultar contraseña",
                        tint = TextoSecundario,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )

        AnimatedVisibility(
            visible = vm.errorGeneral != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = ErrorRojo, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(vm.errorGeneral ?: "", color = ErrorRojo, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.ingresar(onLoginSuccess) },
            enabled = !vm.cargando,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeCenamad)
        ) {
            if (vm.cargando) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Ingresar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, modifier = Modifier.size(12.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                pedirHuella(
                    context = context,
                    onExito = { vm.entrarConHuella(onLoginSuccess) },
                    onError = { mensaje -> vm.mostrarError(mensaje) }
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, VerdeCenamad)
        ) {
            Icon(Icons.Outlined.Fingerprint, null, tint = VerdeCenamad, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ingresar con huella", color = VerdeCenamad, fontSize = 14.sp)
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "¿Olvidaste tu contraseña?",
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOlvidoPassword),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = TextoPrincipal
        )

        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Lock, null, tint = TextoSecundario, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text("Tus datos están protegidos", fontSize = 10.sp, color = TextoSecundario)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenPreview() {
    MyappTheme { LoginScreen() }
}
