package com.example.myapp.ui

import com.example.myapp.model.Asistencia
import com.example.myapp.model.Fechas

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.ui.theme.BordeCampo
import com.example.myapp.ui.theme.ErrorRojo
import com.example.myapp.ui.theme.FondoCampo
import com.example.myapp.ui.theme.FondoPantalla
import com.example.myapp.ui.theme.OkVerde
import com.example.myapp.ui.theme.TextoPrincipal
import com.example.myapp.ui.theme.TextoSecundario
import com.example.myapp.ui.theme.VerdeCenamad
import com.example.myapp.ui.theme.VerdeSuave

data class ItemNav(val etiqueta: String, val icono: ImageVector)

/** Marco común (barra superior + navegación inferior + snackbar) para trabajador y administrador. */
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
        if (mensaje != null) { snackbar.showSnackbar(mensaje); onMensajeVisto() }
    }
    Scaffold(
        containerColor = FondoPantalla,
        topBar = { BarraSuperior(iniciales, onCerrarSesion) },
        bottomBar = { BarraNavegacion(items, tab, onTab) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                label = "tabs"
            ) { t -> contenido(t) }
        }
    }
}

@Composable
fun BarraSuperior(iniciales: String, onCerrarSesion: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(VerdeCenamad).statusBarsPadding()
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
                    selectedIconColor = Color.White, indicatorColor = VerdeCenamad,
                    selectedTextColor = VerdeCenamad,
                    unselectedIconColor = TextoSecundario, unselectedTextColor = TextoSecundario
                )
            )
        }
    }
}

@Composable
fun Sobretitulo(texto: String) =
    Text(texto, fontSize = 10.sp, letterSpacing = 1.5.sp, color = TextoSecundario)

/**
 * Campo de formulario con validación visual (rúbrica IE 2.1.2): borde animado, ícono de estado,
 * mensaje de error animado y texto de ayuda. No contiene reglas: recibe el error ya calculado.
 */
@Composable
fun CampoFormulario(
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    icono: ImageVector,
    error: String?,
    valido: Boolean,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    transformacion: VisualTransformation = VisualTransformation.None,
    extra: @Composable (() -> Unit)? = null
) {
    val borde by animateColorAsState(if (valido) OkVerde else BordeCampo, tween(250), label = "borde")
    val iconoFinal: (@Composable () -> Unit)? = extra ?: if (valido) {
        ({ Icon(Icons.Outlined.CheckCircle, "Válido", tint = OkVerde, modifier = Modifier.size(18.dp)) })
    } else null
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        placeholder = { Text(placeholder, color = TextoSecundario, fontSize = 14.sp) },
        leadingIcon = { Icon(icono, null, tint = if (error != null) ErrorRojo else TextoSecundario, modifier = Modifier.size(18.dp)) },
        trailingIcon = iconoFinal,
        supportingText = {
            AnimatedVisibility(visible = error != null || ayuda != null, enter = fadeIn(), exit = fadeOut()) {
                Text(error ?: ayuda.orEmpty(), fontSize = 11.sp, color = if (error != null) ErrorRojo else TextoSecundario)
            }
        },
        visualTransformation = transformacion,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdeCenamad,
            unfocusedBorderColor = borde,
            errorBorderColor = ErrorRojo,
            focusedContainerColor = FondoCampo,
            unfocusedContainerColor = FondoCampo,
            errorContainerColor = FondoCampo
        )
    )
}

/** Fila del historial: día/mes, quién y cuándo, estado y horas de entrada/salida. */
@Composable
fun FilaAsistencia(a: Asistencia, mostrarNombre: Boolean) {
    val hoy = Fechas.hoy()
    val estado = when {
        a.salida == null -> "En curso"
        a.manual -> "Registro manual"
        else -> "Completado"
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Fechas.dia(a.fecha), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal)
            Text(Fechas.mes(a.fecha), fontSize = 9.sp, color = TextoSecundario)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (mostrarNombre) a.nombre else if (a.fecha == hoy) "Hoy" else Fechas.nombreDia(a.fecha),
                fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (mostrarNombre) "${Fechas.nombreDia(a.fecha)} · $estado" else estado,
                    fontSize = 11.sp, color = if (a.salida == null) OkVerde else TextoSecundario
                )
                if (a.lat != null) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Outlined.Place, "Con ubicación", tint = TextoSecundario, modifier = Modifier.size(12.dp))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            HoraEtiquetada("Entrada", a.entrada)
            HoraEtiquetada("Salida", a.salida)
        }
    }
}

@Composable
private fun HoraEtiquetada(etiqueta: String, hora: String?) {
    Column(horizontalAlignment = Alignment.End) {
        Text(etiqueta, fontSize = 9.sp, color = TextoSecundario)
        Text(hora ?: "--:--", fontSize = 13.sp, color = TextoPrincipal)
    }
}

@Composable
fun Divisor() = Box(Modifier.fillMaxWidth().height(1.dp).background(BordeCampo))

@Composable
fun ChipEstado(texto: String, color: Color) {
    Box(
        Modifier.background(color, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(texto, fontSize = 9.sp, letterSpacing = 1.sp, color = Color.White, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun TarjetaBlanca(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth().background(FondoCampo, RoundedCornerShape(14.dp)).padding(16.dp)) { contenido() }
}

val FondoIcono = VerdeSuave
