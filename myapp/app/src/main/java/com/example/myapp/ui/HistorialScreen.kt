package com.example.myapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.model.Asistencia
import com.example.myapp.ui.theme.TextoPrincipal
import com.example.myapp.ui.theme.TextoSecundario

@Composable
fun HistorialScreen(
    titulo: String,
    subtitulo: String,
    registros: List<Asistencia>,
    mostrarNombre: Boolean,
    buscable: Boolean,
    feriados: Map<String, String> = emptyMap()
) {
    var textoDeBusqueda by rememberSaveable { mutableStateOf("") }

    val registrosVisibles = if (textoDeBusqueda.isBlank()) {
        registros
    } else {
        registros.filter { it.nombre.contains(textoDeBusqueda, ignoreCase = true) || it.run.contains(textoDeBusqueda) }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Sobretitulo(subtitulo)
        Text(titulo, fontSize = 24.sp, color = TextoPrincipal)

        if (buscable) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = textoDeBusqueda,
                onValueChange = { textoDeBusqueda = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o RUN", fontSize = 13.sp, color = TextoSecundario) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextoSecundario) }
            )
        }

        Spacer(Modifier.height(8.dp))
        if (registrosVisibles.isEmpty()) {
            Text("Sin registros para mostrar", fontSize = 13.sp, color = TextoSecundario)
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(registrosVisibles, key = { it.id }) { registro ->
                FilaAsistencia(registro, mostrarNombre, feriados[registro.fecha])
                Divisor()
            }
        }
    }
}
