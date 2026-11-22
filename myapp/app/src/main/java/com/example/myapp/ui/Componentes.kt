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

// =====================================================================
// Marco común de las pantallas con sesión iniciada
// =====================================================================

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
    val mensajesEmergentes = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            mensajesEmergentes.showSnackbar(mensaje)
            onMensajeVisto()
        }
    }

    Scaffold(
        containerColor = FondoPantalla,
        topBar = { BarraSuperior(iniciales, onCerrarSesion) },
        bottomBar = { BarraNavegacion(items, tab, onTab) },
        snackbarHost = { SnackbarHost(mensajesEmergentes) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            Crossfade(targetState = tab, label = "pestañas") { pestana -> contenido(pestana) }
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

        CirculoConIniciales(iniciales)
        IconButton(onClick = onCerrarSesion) {
            Icon(Icons.AutoMirrored.Outlined.Logout, "Cerrar sesión", tint = Color.White)
        }
    }
}

@Composable
private fun CirculoConIniciales(iniciales: String) {
    Box(Modifier.size(32.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
        Text(iniciales, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VerdeCenamad)
    }
}

@Composable
fun BarraNavegacion(items: List<ItemNav>, seleccionado: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = FondoCampo) {
        items.forEachIndexed { posicion, item ->
            NavigationBarItem(
                selected = posicion == seleccionado,
                onClick = { onSelect(posicion) },
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

// =====================================================================
// Piezas pequeñas que se repiten en varias pantallas
// =====================================================================

@Composable
fun Sobretitulo(texto: String) {
    Text(texto, fontSize = 10.sp, letterSpacing = 1.5.sp, color = TextoSecundario)
}

@Composable
fun EtiquetaDeCampo(texto: String) {
    Text(texto, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal)
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

// =====================================================================
// Campo de formulario con ícono, borde animado y mensaje de error o ayuda
// =====================================================================

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
    val hayError = error != null
    val colorDelBorde by animateColorAsState(if (valido) OkVerde else BordeCampo, label = "borde")

    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = hayError,
        placeholder = { Text(placeholder, color = TextoSecundario, fontSize = 14.sp) },
        leadingIcon = {
            Icon(icono, null, tint = if (hayError) ErrorRojo else TextoSecundario, modifier = Modifier.size(18.dp))
        },
        trailingIcon = iconoAlFinalDelCampo(extra, valido),
        supportingText = { TextoDeApoyo(error, ayuda) },
        visualTransformation = transformacion,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdeCenamad,
            unfocusedBorderColor = colorDelBorde,
            errorBorderColor = ErrorRojo,
            focusedContainerColor = FondoCampo,
            unfocusedContainerColor = FondoCampo,
            errorContainerColor = FondoCampo
        )
    )
}

private fun iconoAlFinalDelCampo(extra: @Composable (() -> Unit)?, valido: Boolean): (@Composable () -> Unit)? {
    if (extra != null) return extra
    if (!valido) return null
    return { Icon(Icons.Outlined.CheckCircle, "Válido", tint = OkVerde, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun TextoDeApoyo(error: String?, ayuda: String?) {
    AnimatedVisibility(visible = error != null || ayuda != null, enter = fadeIn(), exit = fadeOut()) {
        Text(error ?: ayuda ?: "", fontSize = 11.sp, color = if (error != null) ErrorRojo else TextoSecundario)
    }
}

// =====================================================================
// Fila del historial de asistencias
// =====================================================================

@Composable
fun FilaAsistencia(asistencia: Asistencia, mostrarNombre: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        DiaYMes(asistencia.fecha)
        Spacer(Modifier.width(12.dp))
        TituloYEstado(asistencia, mostrarNombre, Modifier.weight(1f))
        HorasDeEntradaYSalida(asistencia)
    }
}

@Composable
private fun DiaYMes(fecha: String) {
    Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(Fechas.dia(fecha), fontSize = 18.sp, color = TextoPrincipal)
        Text(Fechas.mes(fecha), fontSize = 9.sp, color = TextoSecundario)
    }
}

@Composable
private fun TituloYEstado(asistencia: Asistencia, mostrarNombre: Boolean, modifier: Modifier) {
    val estado = estadoDelRegistro(asistencia)
    val titulo = when {
        mostrarNombre -> asistencia.nombre
        asistencia.fecha == Fechas.hoy() -> "Hoy"
        else -> Fechas.nombreDia(asistencia.fecha)
    }
    val detalle = if (mostrarNombre) "${Fechas.nombreDia(asistencia.fecha)} · $estado" else estado
    val sigueEnCurso = asistencia.salida == null

    Column(modifier) {
        Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(detalle, fontSize = 11.sp, color = if (sigueEnCurso) OkVerde else TextoSecundario)
            if (asistencia.lat != null) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.Place, "Con ubicación", tint = TextoSecundario, modifier = Modifier.size(12.dp))
            }
        }
    }
}

private fun estadoDelRegistro(asistencia: Asistencia): String = when {
    asistencia.salida == null -> "En curso"
    asistencia.manual -> "Registro manual"
    else -> "Completado"
}

@Composable
private fun HorasDeEntradaYSalida(asistencia: Asistencia) {
    Column(horizontalAlignment = Alignment.End) {
        Text("Entrada  ${asistencia.entrada ?: "--:--"}", fontSize = 12.sp, color = TextoPrincipal)
        Text("Salida  ${asistencia.salida ?: "--:--"}", fontSize = 12.sp, color = TextoPrincipal)
    }
}
