package com.example.myapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.model.Asistencia
import com.example.myapp.model.Fechas
import com.example.myapp.ui.theme.BordeCampo
import com.example.myapp.ui.theme.ErrorRojo
import com.example.myapp.ui.theme.FondoCampo
import com.example.myapp.ui.theme.FondoPantalla
import com.example.myapp.ui.theme.OkVerde
import com.example.myapp.ui.theme.TextoPrincipal
import com.example.myapp.ui.theme.TextoSecundario
import com.example.myapp.ui.theme.VerdeCenamad

data class ItemNav(val etiqueta: String, val icono: ImageVector)

@Composable
fun MarcoPrincipal(
    iniciales: String,
    items: List<ItemNav>,
    tab: Int,
    onTab: (Int) -> Unit,
    onCerrarSesion: () -> Unit,
    mensaje: String?,
    onMensajeVisto: () -> Unit,
    contenido: @Composable (Int) -> Unit
) {
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje)
            onMensajeVisto()
        }
    }

    Scaffold(
        containerColor = FondoPantalla,
        topBar = { BarraSuperior(iniciales, onCerrarSesion) },
        bottomBar = { BarraNavegacion(items, tab, onTab) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            Crossfade(targetState = tab, label = "pestañas") { t -> contenido(t) }
        }
    }
}

@Composable
fun BarraSuperior(iniciales: String, onCerrarSesion: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(VerdeCenamad)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Visibility, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text("CENAMAD", color = Color.White, fontSize = 15.sp, letterSpacing = 2.sp, modifier = Modifier.weight(1f))

        Box(Modifier.size(32.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Text(iniciales, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VerdeCenamad)
        }
        IconButton(onClick = onCerrarSesion) {
            Icon(Icons.AutoMirrored.Outlined.Logout, "Cerrar sesión", tint = Color.White)
        }
    }
}

@Composable
fun BarraNavegacion(items: List<ItemNav>, seleccionado: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = FondoCampo) {
        items.forEachIndexed { i, item ->
            NavigationBarItem(
                selected = i == seleccionado,
                onClick = { onSelect(i) },
                icon = { Icon(item.icono, item.etiqueta) },
                label = { Text(item.etiqueta, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    indicatorColor = VerdeCenamad,
                    selectedTextColor = VerdeCenamad,
                    unselectedIconColor = TextoSecundario,
                    unselectedTextColor = TextoSecundario
                )
            )
        }
    }
}

@Composable
fun Sobretitulo(texto: String) {
    Text(texto, fontSize = 10.sp, letterSpacing = 1.5.sp, color = TextoSecundario)
}

@Composable
fun Divisor() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(BordeCampo))
}

@Composable
fun ChipEstado(texto: String, color: Color) {
    Box(Modifier.background(color, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(texto, fontSize = 9.sp, letterSpacing = 1.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CampoFormulario(
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    icono: ImageVector,
    error: String?,
    valido: Boolean,
    ayuda: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    transformacion: VisualTransformation = VisualTransformation.None,
    extra: @Composable (() -> Unit)? = null
) {
    val colorBorde by animateColorAsState(if (valido) OkVerde else BordeCampo, label = "borde")

    val iconoFinal: (@Composable () -> Unit)? = extra ?: if (valido) {
        ({ Icon(Icons.Outlined.CheckCircle, "Válido", tint = OkVerde, modifier = Modifier.size(18.dp)) })
    } else null

    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        placeholder = { Text(placeholder, color = TextoSecundario, fontSize = 14.sp) },
        leadingIcon = {
            Icon(icono, null, tint = if (error != null) ErrorRojo else TextoSecundario, modifier = Modifier.size(18.dp))
        },
        trailingIcon = iconoFinal,
        supportingText = {
            AnimatedVisibility(visible = error != null || ayuda != null, enter = fadeIn(), exit = fadeOut()) {
                Text(error ?: ayuda ?: "", fontSize = 11.sp, color = if (error != null) ErrorRojo else TextoSecundario)
            }
        },
        visualTransformation = transformacion,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdeCenamad,
            unfocusedBorderColor = colorBorde,
            errorBorderColor = ErrorRojo,
            focusedContainerColor = FondoCampo,
            unfocusedContainerColor = FondoCampo,
            errorContainerColor = FondoCampo
        )
    )
}

@Composable
fun FilaAsistencia(a: Asistencia, mostrarNombre: Boolean) {
    val estado = if (a.salida == null) "En curso" else if (a.manual) "Registro manual" else "Completado"
    val titulo = if (mostrarNombre) a.nombre else if (a.fecha == Fechas.hoy()) "Hoy" else Fechas.nombreDia(a.fecha)

    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Fechas.dia(a.fecha), fontSize = 18.sp, color = TextoPrincipal)
            Text(Fechas.mes(a.fecha), fontSize = 9.sp, color = TextoSecundario)
        }
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal)
            Row(verticalAlignment = Alignment.CenterVertically) {
                val detalle = if (mostrarNombre) "${Fechas.nombreDia(a.fecha)} · $estado" else estado
                Text(detalle, fontSize = 11.sp, color = if (a.salida == null) OkVerde else TextoSecundario)
                if (a.lat != null) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Outlined.Place, "Con ubicación", tint = TextoSecundario, modifier = Modifier.size(12.dp))
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("Entrada  ${a.entrada ?: "--:--"}", fontSize = 12.sp, color = TextoPrincipal)
            Text("Salida  ${a.salida ?: "--:--"}", fontSize = 12.sp, color = TextoPrincipal)
        }
    }
}
