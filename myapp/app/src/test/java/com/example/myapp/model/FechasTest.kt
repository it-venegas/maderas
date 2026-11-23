package com.example.myapp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FechasTest {

    @Test
    fun `convierte horas en minutos, duraciones y días de la semana`() {
        assertEquals(510, Fechas.minutos("08:30"))
        assertEquals(1065, Fechas.minutos("17:45"))
        assertNull(Fechas.minutos(null))
        assertNull(Fechas.minutos("sin hora"))

        assertEquals("8h 30m", Fechas.duracion(510))
        assertEquals("1h 05m", Fechas.duracion(65))

        assertEquals(7, Fechas.diaSemana("2026-11-22"))
        assertEquals("2026-11-16", Fechas.inicioSemana("2026-11-22"))
        assertTrue(Fechas.esFinDeSemana("2026-11-21"))
        assertFalse(Fechas.esFinDeSemana("2026-11-18"))
    }
}
