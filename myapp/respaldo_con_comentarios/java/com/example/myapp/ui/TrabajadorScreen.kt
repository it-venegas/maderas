package com.example.myapp.ui

import com.example.myapp.model.Asistencia
import com.example.myapp.model.Fechas
import com.example.myapp.model.Usuario
import com.example.myapp.repository.Recursos
import com.example.myapp.viewmodel.TrabajadorViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.ui.theme.FondoCampo
import com.example.myapp.ui.theme.GrisJornadaCerrada
import com.example.myapp.ui.theme.TextoPrincipal
import com.example.myapp.ui.theme.TextoSecundario
import com.example.myapp.ui.theme.VerdeCenamad
import com.example.myapp.ui.theme.VerdeChip
import com.example.myapp.ui.theme.VerdeSuave
import kotlinx.coroutines.delay

private const val INTERVALO_ACTUALIZAR_HORA_MS = 10_000L
private const val DURACION_ANIMACION_CHIP_MS = 400
private const val CANTIDAD_DE_REGISTROS_RECIENTES = 3

@Composable
fun TrabajadorScreen(usuario: Usuario, onCerrarSesion: () -> Unit, vm: TrabajadorViewModel = viewModel()) {
    LaunchedEffect(usuario.run) { vm.cargar(usuario) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    // Marcar entrada o salida: primero se piden los permisos, luego se toma la foto.
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
        if (foto != null) vm.marcar(foto, Recursos.ubicacion(context))
        else vm.avisar("Registro cancelado: se necesita la foto")
    }
    val permisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultados ->
        if (resultados[Manifest.permission.CAMERA] == true) camara.launch(null)
        else vm.avisar("Sin permiso de cámara no se puede registrar")
    }

    MarcoPrincipal(
        iniciales = usuario.iniciales,
        items = listOf(ItemNav("Inicio", Icons.Outlined.Home), ItemNav("Historial", Icons.Outlined.History)),
        tab = tab, onTab = { tab = it }, onCerrarSesion = onCerrarSesion,
        mensaje = vm.mensaje, onMensajeVisto = vm::mensajeVisto
    ) { pestana ->
        if (pestana == 0) Inicio(
            usuario = usuario,
            hoy = vm.hoy,
            minutosSemana = vm.minutosSemana,
            diasSemana = vm.diasSemana,
            porcentaje = vm.porcentaje,
            historial = vm.historial,
            onRegistrar = { permisos.launch(Recursos.PERMISOS) },
            onVerHistorial = { tab = 1 }
        ) else HistorialScreen(
            titulo = "Mi historial", subtitulo = "REGISTROS", registros = vm.historial,
            mostrarNombre = false, buscable = false
        )
    }
}

// =====================================================================
// Pestaña "Inicio"
// =====================================================================

@Composable
private fun Inicio(
    usuario: Usuario,
    hoy: Asistencia?,
    minutosSemana: Int,
    diasSemana: Int,
    porcentaje: Int,
    historial: List<Asistencia>,
    onRegistrar: () -> Unit,
    onVerHistorial: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Saludo(usuario)

        Spacer(Modifier.height(18.dp))
        TarjetaJornada(hoy, onRegistrar)

        Spacer(Modifier.height(24.dp))
        ResumenDeLaSemana(minutosSemana, diasSemana, porcentaje, onVerHistorial)

        Spacer(Modifier.height(24.dp))
        ActividadReciente(historial, onVerHistorial)
    }
}

@Composable
private fun Saludo(usuario: Usuario) {
    Sobretitulo(Fechas.saludo().uppercase())
    Spacer(Modifier.height(6.dp))
    Text("Hola, ${usuario.primerNombre}", fontSize = 26.sp, color = TextoPrincipal)
    Text("Que tengas una excelente jornada", fontSize = 12.sp, color = TextoSecundario)
}

@Composable
private fun ResumenDeLaSemana(minutosSemana: Int, diasSemana: Int, porcentaje: Int, onVerHistorial: () -> Unit) {
    EncabezadoSeccion("ESTA SEMANA", "Resumen de asistencia", "Ver historial", onVerHistorial)
    FilaResumen(Icons.Outlined.AccessTime, Fechas.duracion(minutosSemana), "Horas trabajadas")
    FilaResumen(Icons.Outlined.CalendarMonth, "$diasSemana de ${Fechas.DIAS_LABORALES_POR_SEMANA}", "Días registrados")
    FilaResumen(Icons.Outlined.CheckCircle, "$porcentaje%", "Asistencia")
}

@Composable
private fun ActividadReciente(historial: List<Asistencia>, onVerHistorial: () -> Unit) {
    EncabezadoSeccion("REGISTROS", "Actividad reciente", "Ver todo", onVerHistorial)
    if (historial.isEmpty()) Text("Aún no tienes registros", fontSize = 12.sp, color = TextoSecundario)
    historial.take(CANTIDAD_DE_REGISTROS_RECIENTES).forEach { registro ->
        FilaAsistencia(registro, mostrarNombre = false)
        Divisor()
    }
}

// =====================================================================
// Tarjeta verde con la jornada de hoy
// =====================================================================

@Composable
private fun TarjetaJornada(hoy: Asistencia?, onRegistrar: () -> Unit) {
    val hora = horaActual()
    val jornadaCompleta = hoy?.salida != null
    val (etiquetaDeEstado, colorDeEstado) = estadoDeLaJornada(hoy)
    val colorAnimado by animateColorAsState(colorDeEstado, tween(DURACION_ANIMACION_CHIP_MS), label = "chip")

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(VerdeCenamad).padding(18.dp)
    ) {
        EstadoYHoraActual(etiquetaDeEstado, colorAnimado, hora)

        Spacer(Modifier.height(10.dp))
        FechaDeHoy()

        Spacer(Modifier.height(16.dp))
        IngresoYSalida(hoy)

        Spacer(Modifier.height(16.dp))
        BotonRegistrarJornada(hoy, jornadaCompleta, onRegistrar)
    }
}

/** La hora que se muestra en la tarjeta; se vuelve a leer cada pocos segundos. */
@Composable
private fun horaActual(): String {
    var hora by remember { mutableStateOf(Fechas.ahora()) }
    LaunchedEffect(Unit) {
        while (true) {
            hora = Fechas.ahora()
            delay(INTERVALO_ACTUALIZAR_HORA_MS)
        }
    }
    return hora
}

private fun estadoDeLaJornada(hoy: Asistencia?): Pair<String, Color> = when {
    hoy == null -> "SIN REGISTRAR" to GrisJornadaCerrada
    hoy.salida != null -> "JORNADA COMPLETA" to GrisJornadaCerrada
    else -> "JORNADA ACTIVA" to VerdeChip
}

private fun textoDelBotonDeRegistro(hoy: Asistencia?, jornadaCompleta: Boolean): String = when {
    hoy == null -> "Registrar ingreso"
    jornadaCompleta -> "Jornada completada"
    else -> "Registrar salida"
}

@Composable
private fun EstadoYHoraActual(etiquetaDeEstado: String, colorDeEstado: Color, hora: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ChipEstado(etiquetaDeEstado, colorDeEstado)
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(hora, fontSize = 30.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text("hrs", fontSize = 10.sp, color = Color.White.copy(alpha = .7f))
        }
    }
}

@Composable
private fun FechaDeHoy() {
    val fechaDeHoy = Fechas.hoy()
    val anio = fechaDeHoy.take(4)
    Text(Fechas.fechaLarga(fechaDeHoy), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
    Text(
        "Semana ${Fechas.semanaDelAnio(fechaDeHoy)} · $anio",
        fontSize = 11.sp, color = Color.White.copy(alpha = .7f)
    )
}

@Composable
private fun IngresoYSalida(hoy: Asistencia?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BloqueHora("INGRESO", hoy?.entrada, Icons.Outlined.Check)
        Box(Modifier.weight(1f).padding(horizontal = 10.dp).height(1.dp).background(Color.White.copy(alpha = .35f)))
        BloqueHora("SALIDA", hoy?.salida, Icons.Outlined.AccessTime)
    }
}

@Composable
private fun BotonRegistrarJornada(hoy: Asistencia?, jornadaCompleta: Boolean, onRegistrar: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FondoCampo)
            .clickable(enabled = !jornadaCompleta, onClick = onRegistrar).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).background(VerdeSuave, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.CameraAlt, null, tint = VerdeCenamad, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                textoDelBotonDeRegistro(hoy, jornadaCompleta),
                fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextoPrincipal
            )
            Text("Verificación con foto y ubicación", fontSize = 10.sp, color = TextoSecundario)
        }
        if (!jornadaCompleta) {
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = TextoSecundario, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun BloqueHora(etiqueta: String, hora: String?, icono: ImageVector) {
    val registrado = hora != null
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(30.dp).background(if (registrado) VerdeChip else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icono, null, tint = Color.White.copy(alpha = if (registrado) 1f else .6f), modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(etiqueta, fontSize = 8.sp, letterSpacing = 1.sp, color = Color.White.copy(alpha = .7f))
            Text(hora ?: "--:--", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

// =====================================================================
// Piezas de la pestaña "Inicio"
// =====================================================================

@Composable
private fun EncabezadoSeccion(sobre: String, titulo: String, accion: String, onAccion: () -> Unit) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Column {
            Sobretitulo(sobre)
            Text(titulo, fontSize = 20.sp, color = TextoPrincipal)
        }
        TextButton(onClick = onAccion) { Text(accion, fontSize = 11.sp, color = VerdeCenamad) }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun FilaResumen(icono: ImageVector, valor: String, etiqueta: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(valor, fontSize = 18.sp, color = TextoPrincipal)
            Text(etiqueta, fontSize = 10.sp, color = TextoSecundario)
        }
    }
}
