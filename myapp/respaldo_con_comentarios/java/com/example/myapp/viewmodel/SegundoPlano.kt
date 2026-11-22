package com.example.myapp.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Ejecuta el trabajo fuera del hilo de la pantalla (acceso a la base de datos, archivos, etc.). */
internal suspend fun <T> enSegundoPlano(trabajo: () -> T): T = withContext(Dispatchers.IO) { trabajo() }
