package com.example.myapp.ui

import com.example.myapp.model.Rol
import com.example.myapp.model.Usuario
import com.example.myapp.viewmodel.LoginViewModel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.ui.theme.BordeCampo
import com.example.myapp.ui.theme.ErrorRojo
import com.example.myapp.ui.theme.FondoPantalla
import com.example.myapp.ui.theme.MyappTheme
import com.example.myapp.ui.theme.TextoPrincipal
import com.example.myapp.ui.theme.TextoSecundario
import com.example.myapp.ui.theme.VerdeCenamad

@Composable
fun LoginScreen(
    onLoginSuccess: (Usuario) -> Unit = {},
    onOlvidoPassword: () -> Unit = {},
    vm: LoginViewModel = viewModel()
) {
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
        Logo()
        Spacer(Modifier.height(40.dp))

        Text("BIENVENIDO/A", fontSize = 10.sp, letterSpacing = 1.5.sp, color = TextoSecundario)
        Spacer(Modifier.height(8.dp))
        Text("Inicia sesión", fontSize = 28.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal)
        Spacer(Modifier.height(6.dp))
        Text("Selecciona el tipo de acceso para continuar", fontSize = 13.sp, color = TextoSecundario)

        Spacer(Modifier.height(24.dp))
        TabsAcceso(seleccionado = vm.rol, onSelect = vm::onRol)

        Spacer(Modifier.height(24.dp))
        Etiqueta("RUN")
        CampoFormulario(
            valor = vm.run, onChange = vm::onRun,
            placeholder = "12.345.678-5", icono = Icons.Outlined.PersonOutline,
            error = vm.runError, valido = vm.runValido,
            ayuda = "Con dígito verificador, ej: 12.345.678-5",
            teclado = KeyboardType.Ascii
        )

        Spacer(Modifier.height(12.dp))
        Etiqueta("Contraseña")
        CampoFormulario(
            valor = vm.password, onChange = vm::onPassword,
            placeholder = "••••••••", icono = Icons.Outlined.LockOpen,
            error = vm.passwordError, valido = vm.passwordValida,
            teclado = KeyboardType.Password,
            transformacion = if (vm.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            extra = {
                IconButton(onClick = vm::togglePasswordVisible) {
                    Icon(
                        if (vm.passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Mostrar u ocultar contraseña",
                        tint = TextoSecundario, modifier = Modifier.size(18.dp)
                    )
                }
            }
        )

        // Error general (credenciales / rol): aparece y desaparece con animación
        AnimatedVisibility(
            visible = vm.errorGeneral != null,
            enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()
        ) {
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = ErrorRojo, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(vm.errorGeneral.orEmpty(), color = ErrorRojo, fontSize = 12.sp)
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

        Spacer(Modifier.height(20.dp))
        Text(
            "¿Olvidaste tu contraseña?",
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOlvidoPassword),
            textAlign = TextAlign.Center, fontSize = 12.sp, color = TextoPrincipal
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

@Composable
private fun Logo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(34.dp).border(BorderStroke(1.5.dp, VerdeCenamad), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Outlined.Visibility, null, tint = VerdeCenamad, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("CENAMAD", fontSize = 16.sp, letterSpacing = 2.sp, color = TextoPrincipal)
            Text("CENTRO NACIONAL DE MADERAS", fontSize = 7.sp, letterSpacing = 1.sp, color = TextoSecundario)
        }
    }
}

/** Pestañas con indicador deslizante animado entre Trabajador y Administrador. */
@Composable
private fun TabsAcceso(seleccionado: Rol, onSelect: (Rol) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val mitad = maxWidth / Rol.entries.size
        val desplazamiento by animateDpAsState(mitad * seleccionado.ordinal, tween(250), label = "indicador")
        Column {
            Row(Modifier.fillMaxWidth()) {
                Rol.entries.forEach { rol ->
                    val activo = rol == seleccionado
                    Text(
                        rol.etiqueta,
                        modifier = Modifier.weight(1f).clickable { onSelect(rol) }.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (activo) TextoPrincipal else TextoSecundario
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(2.dp).background(BordeCampo)) {
                Box(Modifier.offset(x = desplazamiento).width(mitad).height(2.dp).background(VerdeCenamad))
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(texto, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal)
    Spacer(Modifier.height(8.dp))
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenPreview() {
    MyappTheme { LoginScreen() }
}
