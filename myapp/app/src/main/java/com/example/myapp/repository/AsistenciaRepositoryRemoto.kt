package com.example.myapp.repository

import com.example.myapp.model.Asistencia
import com.example.myapp.model.Rol
import com.example.myapp.model.Usuario
import com.example.myapp.network.AsistenciaApi
import com.example.myapp.network.AuthApi
import com.example.myapp.network.ErrorDeRed
import com.example.myapp.network.LoginDto
import com.example.myapp.network.RegistroManualDto
import com.example.myapp.network.aAsistencia
import com.example.myapp.network.aUsuario
import com.example.myapp.network.llamarServidor

private const val CODIGO_DEMASIADAS_SOLICITUDES = 429
private val CODIGOS_DE_CREDENCIALES_RECHAZADAS = 400..499

class AsistenciaRepositoryRemoto(
    private val authApi: AuthApi,
    private val asistenciaApi: AsistenciaApi,
    private val sesion: AlmacenDeSesion
) : AsistenciaRepository {

    // ---------- Usuarios e inicio de sesión ----------

    override suspend fun autenticar(run: String, password: String): Usuario? =
        try {
            val respuesta = llamarServidor { authApi.iniciarSesion(LoginDto(run, password)) }
            sesion.tokenActual = respuesta.authToken
            val usuarioDto = respuesta.usuario ?: llamarServidor { authApi.usuarioActual() }
            usuarioDto.aUsuario()
        } catch (e: ErrorDeRed.NoAutorizado) {
            null
        } catch (e: ErrorDeRed.ErrorDelServidor) {
            if (e.codigoHttp in CODIGOS_DE_CREDENCIALES_RECHAZADAS && e.codigoHttp != CODIGO_DEMASIADAS_SOLICITUDES) null else throw e
        }

    override suspend fun trabajadores(): List<Usuario> =
        llamarServidor { asistenciaApi.trabajadores(Rol.TRABAJADOR.name) }.map { it.aUsuario() }

    // ---------- Último usuario (botón de huella) ----------

    override fun guardarUltimoRun(run: String) {
    }

    override suspend fun ultimoUsuario(): Usuario? = cargarSesionGuardada()?.usuario

    // ---------- Sesión recordada ("Recuérdame") ----------

    override fun recordarSesion(usuario: Usuario) {
        val token = sesion.tokenActual ?: return
        sesion.guardar(token, usuario)
    }

    override fun olvidarSesion() {
        sesion.olvidar()
    }

    override suspend fun sesionRecordada(): Usuario? {
        val guardada = cargarSesionGuardada() ?: return null
        return try {
            llamarServidor { authApi.usuarioActual() }.aUsuario()
        } catch (e: ErrorDeRed.NoAutorizado) {
            olvidarSesion()
            null
        } catch (e: ErrorDeRed) {
            guardada.usuario
        }
    }

    private fun cargarSesionGuardada(): SesionGuardada? {
        val guardada = sesion.recordada() ?: return null
        sesion.tokenActual = guardada.token
        return guardada
    }

    // ---------- Consulta de asistencias ----------

    override suspend fun historial(run: String?): List<Asistencia> =
        llamarServidor { asistenciaApi.asistencias(run) }.map { it.aAsistencia() }

    override suspend fun asistenciaDe(run: String, fecha: String): Asistencia? =
        historial(run).firstOrNull { it.fecha == fecha }

    // ---------- Registro de asistencias ----------

    override suspend fun registrarEntrada(run: String, foto: String?, ubicacion: Ubicacion?): Asistencia =
        llamarServidor { asistenciaApi.marcarEntrada() }.aAsistencia()

    override suspend fun registrarSalida(registro: Asistencia, foto: String?): Asistencia =
        llamarServidor { asistenciaApi.marcarSalida() }.aAsistencia()

    override suspend fun registrarManual(run: String, fecha: String, entrada: String, salida: String?) {
        llamarServidor { asistenciaApi.registrarManual(RegistroManualDto(run, fecha, entrada, salida)) }
    }
}
