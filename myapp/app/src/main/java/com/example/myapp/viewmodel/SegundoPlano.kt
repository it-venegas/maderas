package com.example.myapp.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun <T> enSegundoPlano(trabajo: suspend () -> T): T = withContext(Dispatchers.IO) { trabajo() }
