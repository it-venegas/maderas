package com.example.myapp.ui

import com.example.myapp.model.Asistencia
import com.example.myapp.model.Fechas

import com.example.myapp.model.Usuario
import com.example.myapp.viewmodel.AdminViewModel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.ui.theme.ErrorRojo
import com.example.myapp.ui.theme.TextoPrincipal
import com.example.myapp.ui.theme.TextoSecundario
import com.example.myapp.ui.theme.VerdeCenamad
import com.example.myapp.ui.theme.VerdeChip
import com.example.myapp.ui.theme.VerdeSuave

private const val CANTIDAD_DE_MOVIMIENTOS_RECIENTES = 5
private const val DURACION_ANIMACION_PROGRESO_MS = 700

@Composable
fun AdminScreen(usuario: Usuario, onCerrarSesion: () -> Unit, vm: AdminViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.cargar() }
    var tab by rememberSaveable { mutableIntStateOf(0) }

    MarcoPrincipal(
        iniciales = usuario.iniciales,
        items = listOf(
            ItemNav("Resumen", Icons.Outlined.Groups),
            ItemNav("Historial", Icons.Outlined.History),
            ItemNav("Registrar", Icons.Outlined.EditCalendar)
        ),
        tab = tab, onTab = { tab = it }, onCerrarSesion = onCerrarSesion,
        mensaje = vm.mensaje, onMensajeVisto = vm::mensajeVisto
    ) { pestana ->
        when (pestana) {
            0 -> PanelResumen(usuario, vm)
            1 -> HistorialScreen(
                titulo = "Historial del equipo", subtitulo = "TODOS LOS REGISTROS",
                registros = vm.historial, mostrarNombre = true, buscable = true
            )
            else -> RegistroManual(vm)
        }
    }
}

// =====================================================================
// Pestaña "Resumen"
// =====================================================================

@Composable
private fun PanelResumen(usuario: Usuario, vm: AdminViewModel) {
    val fraccionPresente = if (vm.total == 0) 0f else vm.presentes.toFloat() / vm.total
    val progresoAnimado by animateFloatAsState(fraccionPresente, tween(DURACION_ANIMACION_PROGRESO_MS), label = "progreso")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        EncabezadoDelPanel(usuario)

        Spacer(Modifier.height(18.dp))
        TarjetaPersonasPresentes(vm.presentes, vm.total, fraccionPresente, progresoAnimado)

        Spacer(Modifier.height(24.dp))
        UltimosMovimientos(vm.historial)
    }
}

@Composable
private fun EncabezadoDelPanel(usuario: Usuario) {
    Sobretitulo("PANEL DE CONTROL")
    Spacer(Modifier.height(6.dp))
    Text("${Fechas.saludo()}, ${usuario.primerNombre}", fontSize = 26.sp, color = TextoPrincipal)
    Text("Este es el estado del equipo hoy", fontSize = 12.sp, color = TextoSecundario)
}

@Composable
private fun TarjetaPersonasPresentes(presentes: Int, total: Int, fraccionPresente: Float, progresoAnimado: Float) {
    Column(Modifier.fillMaxWidth().background(VerdeCenamad, RoundedCornerShape(20.dp)).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Groups, null, tint = Color.White, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("PERSONAS PRESENTES", fontSize = 10.sp, letterSpacing = 1.sp, color = Color.White.copy(alpha = .8f))
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$presentes", fontSize = 40.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(" / $total", fontSize = 18.sp, color = Color.White.copy(alpha = .7f), modifier = Modifier.padding(bottom = 6.dp))
        }

        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progresoAnimado }, modifier = Modifier.fillMaxWidth().height(6.dp),
            color = Color.White, trackColor = Color.White.copy(alpha = .25f)
        )

        Spacer(Modifier.height(6.dp))
        Text("${(fraccionPresente * 100).toInt()}% del equipo", fontSize = 11.sp, color = Color.White.copy(alpha = .8f))
    }
}

@Composable
private fun UltimosMovimientos(historial: List<Asistencia>) {
    Sobretitulo("MOVIMIENTOS")
    Text("Últimos registros", fontSize = 20.sp, color = TextoPrincipal)
    Spacer(Modifier.height(6.dp))
    historial.take(CANTIDAD_DE_MOVIMIENTOS_RECIENTES).forEach { registro ->
        FilaAsistencia(registro, mostrarNombre = true)
        Divisor()
    }
}

// =====================================================================
// Pestaña "Registrar" (asistencia manual)
// =====================================================================

@Composable
private fun RegistroManual(vm: AdminViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        EncabezadoDelRegistro()

        Spacer(Modifier.height(20.dp))
        SelectorDeTrabajador(vm)

        Spacer(Modifier.height(16.dp))
        CampoFecha(vm)

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                CampoDeHora(
                    etiqueta = "Entrada", valor = vm.entrada, alCambiar = vm::onEntrada, ejemplo = "08:30",
                    error = vm.entradaError, valida = vm.entradaValida
                )
            }
            Column(Modifier.weight(1f)) {
                CampoDeHora(
                    etiqueta = "Salida (opcional)", valor = vm.salida, alCambiar = vm::onSalida, ejemplo = "17:30",
                    error = vm.salidaError, valida = vm.salidaValida
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        BotonGuardarAsistencia(alPresionar = vm::guardar)
    }
}

@Composable
private fun EncabezadoDelRegistro() {
    Sobretitulo("ADMINISTRACIÓN")
    Text("Registrar asistencia", fontSize = 24.sp, color = TextoPrincipal)
    Text("Crea o corrige la asistencia de un trabajador", fontSize = 12.sp, color = TextoSecundario)
}

@Composable
private fun SelectorDeTrabajador(vm: AdminViewModel) {
    EtiquetaDeCampo("Trabajador")
    Spacer(Modifier.height(8.dp))
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        vm.trabajadores.forEach { trabajador ->
            FilterChip(
                selected = vm.runSeleccionado == trabajador.run,
                onClick = { vm.onTrabajador(trabajador.run) },
                label = { Text(trabajador.nombre, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdeSuave, selectedLabelColor = VerdeCenamad
                )
            )
        }
    }
    vm.trabajadorError?.let { Text(it, fontSize = 11.sp, color = ErrorRojo, modifier = Modifier.padding(top = 4.dp)) }
}

@Composable
private fun CampoFecha(vm: AdminViewModel) {
    EtiquetaDeCampo("Fecha")
    Spacer(Modifier.height(8.dp))
    CampoFormulario(
        valor = vm.fecha, onChange = vm::onFecha, placeholder = "AAAA-MM-DD", icono = Icons.Outlined.CalendarMonth,
        error = vm.fechaError, valido = vm.fechaValida, ayuda = "Ejemplo: 2026-10-28", teclado = KeyboardType.Number
    )
}

@Composable
private fun CampoDeHora(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    ejemplo: String,
    error: String?,
    valida: Boolean
) {
    EtiquetaDeCampo(etiqueta)
    Spacer(Modifier.height(8.dp))
    CampoFormulario(
        valor = valor, onChange = alCambiar, placeholder = ejemplo, icono = Icons.Outlined.AccessTime,
        error = error, valido = valida, teclado = KeyboardType.Number
    )
}

@Composable
private fun BotonGuardarAsistencia(alPresionar: () -> Unit) {
    Button(
        onClick = alPresionar,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerdeCenamad)
    ) {
        Icon(Icons.Outlined.Save, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Guardar asistencia", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
