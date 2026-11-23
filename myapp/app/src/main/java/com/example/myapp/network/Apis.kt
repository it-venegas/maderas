package com.example.myapp.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/login")
    suspend fun iniciarSesion(@Body datos: LoginDto): RespuestaLoginDto

    @GET("auth/me")
    suspend fun usuarioActual(): UsuarioDto
}

interface CalendarioApi {
    @GET("feriados")
    suspend fun feriados(): List<FeriadoDto>
}

interface AsistenciaApi {
    @GET("usuarios")
    suspend fun trabajadores(@Query("rol") rol: String): List<UsuarioDto>

    @GET("asistencias")
    suspend fun asistencias(@Query("run") run: String?): List<AsistenciaDto>

    @POST("asistencias/entrada")
    suspend fun marcarEntrada(): AsistenciaDto

    @POST("asistencias/salida")
    suspend fun marcarSalida(): AsistenciaDto

    @POST("asistencias/manual")
    suspend fun registrarManual(@Body datos: RegistroManualDto): AsistenciaDto
}
