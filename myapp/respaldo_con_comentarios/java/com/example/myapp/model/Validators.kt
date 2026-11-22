package com.example.myapp.model

object Validators {

    private val formatoRun = Regex("[0-9]{7,8}-[0-9K]")
    private val formatoFecha = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")
    private val formatoHora = Regex("([01][0-9]|2[0-3]):[0-5][0-9]")

    private const val LARGO_MINIMO_PASSWORD = 6

    // Constantes del cálculo del dígito verificador (módulo 11)
    private const val FACTOR_INICIAL = 2
    private const val FACTOR_FINAL = 7
    private const val MODULO_DEL_RUN = 11
    private const val RESTO_QUE_SE_ESCRIBE_K = 10

    // ---------- RUN ----------

    fun validarRun(run: String): String? {
        if (run.isEmpty()) return "Ingresa tu RUN"
        if (!formatoRun.matches(run)) return "Formato: 12345678-5 (con guión, sin puntos)"

        val cuerpo = run.substringBefore("-")
        val digitoIngresado = run.substringAfter("-")
        if (calcularDv(cuerpo) != digitoIngresado) return "Dígito verificador incorrecto"
        return null
    }

    fun calcularDv(cuerpo: String): String {
        var suma = 0
        var factor = FACTOR_INICIAL
        for (digito in cuerpo.reversed()) {
            suma += (digito - '0') * factor
            factor = if (factor == FACTOR_FINAL) FACTOR_INICIAL else factor + 1
        }

        return when (val resultado = MODULO_DEL_RUN - suma % MODULO_DEL_RUN) {
            MODULO_DEL_RUN -> "0"
            RESTO_QUE_SE_ESCRIBE_K -> "K"
            else -> resultado.toString()
        }
    }

    // ---------- Contraseña ----------

    fun validarPassword(password: String): String? {
        if (password.isBlank()) return "Ingresa tu contraseña"
        if (password.length < LARGO_MINIMO_PASSWORD) return "Mínimo $LARGO_MINIMO_PASSWORD caracteres"
        return null
    }

    // ---------- Fecha y horas del registro manual ----------

    fun validarFecha(fecha: String): String? {
        if (fecha.isBlank()) return "Ingresa la fecha"
        if (!formatoFecha.matches(fecha)) return "Formato AAAA-MM-DD"
        if (Fechas.parse(fecha) == null) return "Fecha inexistente"
        if (Fechas.esFuturo(fecha)) return "No puede ser una fecha futura"
        return null
    }

    fun validarHora(hora: String): String? {
        if (hora.isBlank()) return "Ingresa la hora"
        if (!formatoHora.matches(hora)) return "Formato HH:MM (24 horas)"
        return null
    }

    fun validarSalida(entrada: String, salida: String): String? {
        if (salida.isBlank()) return null
        validarHora(salida)?.let { return it }

        val minutosDeEntrada = Fechas.minutos(entrada) ?: return null
        val minutosDeSalida = Fechas.minutos(salida) ?: return null
        val laSalidaEsAnteriorALaEntrada = minutosDeSalida <= minutosDeEntrada
        if (laSalidaEsAnteriorALaEntrada) return "La salida debe ser posterior a la entrada"
        return null
    }

    // ---------- Selección de trabajador ----------

    fun validarTrabajador(run: String?): String? {
        if (run == null) return "Selecciona un trabajador"
        return null
    }
}
