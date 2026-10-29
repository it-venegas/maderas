package com.example.myapp

import com.example.myapp.model.RunUtils
import com.example.myapp.model.Validators

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {
    @Test fun runValidoConPuntosYGuion() = assertNull(Validators.validarRun("18.452.763-4"))
    @Test fun runValidoSinFormato() = assertNull(Validators.validarRun("184527634"))
    @Test fun runConDvIncorrecto() = assertEquals("Dígito verificador incorrecto", Validators.validarRun("18.452.763-5"))
    @Test fun runIncompleto() = assertNotNull(Validators.validarRun("1845"))
    @Test fun runVacio() = assertEquals("Ingresa tu RUN", Validators.validarRun(""))
    @Test fun dvK() = assertEquals('K', RunUtils.calcularDv("6"))
    @Test fun formatea() = assertEquals("18.452.763-4", RunUtils.formatear("184527634"))
    @Test fun normaliza() = assertEquals("18452763-4", RunUtils.normalizar("18.452.763-4"))
    @Test fun horaInvalida() = assertNotNull(Validators.validarHora("25:00"))
    @Test fun horaValida() = assertNull(Validators.validarHora("08:30"))
    @Test fun salidaAntesDeEntrada() = assertNotNull(Validators.validarSalida("09:00", "08:00"))
    @Test fun salidaOpcional() = assertNull(Validators.validarSalida("09:00", ""))
    @Test fun fechaInexistente() = assertNotNull(Validators.validarFecha("2026-02-31"))
}
