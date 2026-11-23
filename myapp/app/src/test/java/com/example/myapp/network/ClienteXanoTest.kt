package com.example.myapp.network

import com.example.myapp.model.Rol
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClienteXanoTest {
    private lateinit var servidor: MockWebServer

    @Before
    fun iniciarServidorFalso() {
        servidor = MockWebServer()
        servidor.start()
    }

    @After
    fun cerrarServidorFalso() {
        servidor.shutdown()
    }

    @Test
    fun `el inicio de sesión envía RUN y contraseña con el token y entrega el usuario`() = runTest {
        servidor.enqueue(
            MockResponse().setBody(
                """{"authToken":"abc","usuario":{"id":1,"run":"11222333-9","nombre":"Valentina Rojas","cargo":"Jefa de operaciones","rol":"ADMINISTRADOR"}}"""
            )
        )
        val api = ClienteXano.crearAuth(
            baseUrl = servidor.url("/api:grupo/").toString(),
            token = { "token-de-prueba" }
        )

        val respuesta = api.iniciarSesion(LoginDto("11222333-9", "admin123"))

        assertEquals("abc", respuesta.authToken)
        assertEquals(Rol.ADMINISTRADOR, respuesta.usuario?.aUsuario()?.rol)
        assertEquals("Valentina Rojas", respuesta.usuario?.aUsuario()?.nombre)

        val solicitud = servidor.takeRequest()
        assertEquals("POST", solicitud.method)
        assertEquals("/api:grupo/auth/login", solicitud.path)
        assertEquals("Bearer token-de-prueba", solicitud.getHeader("Authorization"))
        assertEquals("""{"run":"11222333-9","password":"admin123"}""", solicitud.body.readUtf8())
    }
}
