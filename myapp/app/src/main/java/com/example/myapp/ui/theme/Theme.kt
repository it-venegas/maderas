package com.example.myapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = VerdeCenamad,
    background = FondoPantalla,
    surface = FondoCampo,
    onBackground = TextoPrincipal,
    onSurface = TextoPrincipal
)

@Composable
fun MyappTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
