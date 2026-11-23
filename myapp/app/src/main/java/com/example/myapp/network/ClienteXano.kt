package com.example.myapp.network

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val SEGUNDOS_PARA_CONECTAR = 15L
private const val SEGUNDOS_PARA_RESPONDER = 20L

object ClienteXano {
    fun crearAuth(baseUrl: String = ConfiguracionServidor.URL_AUTH, token: () -> String?): AuthApi =
        crear(AuthApi::class.java, baseUrl, token)

    fun crearAsistencia(baseUrl: String = ConfiguracionServidor.URL_ASISTENCIA, token: () -> String?): AsistenciaApi =
        crear(AsistenciaApi::class.java, baseUrl, token)

    fun crearCalendario(baseUrl: String = ConfiguracionServidor.URL_CALENDARIO): CalendarioApi =
        crear(CalendarioApi::class.java, baseUrl) { null }

    private fun <T> crear(servicio: Class<T>, baseUrl: String, token: () -> String?): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clienteHttp(token))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(servicio)

    private fun clienteHttp(token: () -> String?): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(SEGUNDOS_PARA_CONECTAR, TimeUnit.SECONDS)
            .readTimeout(SEGUNDOS_PARA_RESPONDER, TimeUnit.SECONDS)
            .addInterceptor { cadena ->
                val pedidoOriginal = cadena.request()
                val tokenActual = token()
                val pedido = if (tokenActual == null) {
                    pedidoOriginal
                } else {
                    pedidoOriginal.newBuilder().header("Authorization", "Bearer $tokenActual").build()
                }
                cadena.proceed(pedido)
            }
            .build()
}
