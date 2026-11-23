package com.example.myapp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `validarRun acepta un RUN correcto y rechaza los incorrectos`() {
        assertNull(Validators.validarRun("11222333-9"))
        assertNull(Validators.validarRun("10000013-K"))

        assertEquals("Ingresa tu RUN", Validators.validarRun(""))
        assertEquals("Formato: 12345678-5 (con guión, sin puntos)", Validators.validarRun("11.222.333-9"))
        assertEquals("Dígito verificador incorrecto", Validators.validarRun("11222333-8"))
    }
}
