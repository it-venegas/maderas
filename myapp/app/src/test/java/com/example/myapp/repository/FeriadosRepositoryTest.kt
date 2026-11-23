package com.example.myapp.repository

import com.example.myapp.network.ClienteXano
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FeriadosRepositoryTest {
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

    private fun crearRepositorio() =
        FeriadosRepository(ClienteXano.crearCalendario(servidor.url("/api:calendario/").toString()))

    @Test
    fun `los feriados se entregan por fecha y se piden una sola vez`() = runTest {
        servidor.enqueue(MockResponse().setBody("""[{"fecha":"2026-12-25","nombre":"Navidad"}]"""))
        val repositorio = crearRepositorio()

        val primeraConsulta = repositorio.feriados()
        val segundaConsulta = repositorio.feriados()

        assertEquals("Navidad", primeraConsulta["2026-12-25"])
        assertEquals(primeraConsulta, segundaConsulta)
        assertEquals(1, servidor.requestCount)
        assertEquals("/api:calendario/feriados", servidor.takeRequest().path)
    }

    @Test
    fun `si el servicio falla no se interrumpe la app y no hay feriados`() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(500).setBody("""{"message":"Error interno"}"""))

        assertTrue(crearRepositorio().feriados().isEmpty())
    }
}
