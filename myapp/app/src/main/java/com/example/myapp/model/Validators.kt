package com.example.myapp.model

object Validators {

    fun validarRun(run: String): String? {
        if (run.isEmpty()) return "Ingresa tu RUN"
        if (!Regex("[0-9]{7,8}-[0-9K]").matches(run)) {
            return "Formato: 12345678-5 (con guión, sin puntos)"
        }
        val cuerpo = run.substringBefore("-")
        val dv = run.substringAfter("-")
        if (calcularDv(cuerpo) != dv) return "Dígito verificador incorrecto"
        return null
    }

    fun calcularDv(cuerpo: String): String {
        var suma = 0
        var factor = 2
        for (c in cuerpo.reversed()) {
            suma += (c - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val resto = 11 - suma % 11
        return when (resto) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }
    }

    fun validarPassword(password: String): String? {
        if (password.isBlank()) return "Ingresa tu contraseña"
        if (password.length < 6) return "Mínimo 6 caracteres"
        return null
    }

    fun validarFecha(fecha: String): String? {
        if (fecha.isBlank()) return "Ingresa la fecha"
        if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(fecha)) return "Formato AAAA-MM-DD"
        if (Fechas.parse(fecha) == null) return "Fecha inexistente"
        if (Fechas.esFuturo(fecha)) return "No puede ser una fecha futura"
        return null
    }

    fun validarHora(hora: String): String? {
        if (hora.isBlank()) return "Ingresa la hora"
        if (!Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(hora)) return "Formato HH:MM (24 horas)"
        return null
    }

    fun validarSalida(entrada: String, salida: String): String? {
        if (salida.isBlank()) return null
        validarHora(salida)?.let { return it }
        val minutosEntrada = Fechas.minutos(entrada) ?: return null
        val minutosSalida = Fechas.minutos(salida) ?: return null
        if (minutosSalida <= minutosEntrada) return "La salida debe ser posterior a la entrada"
        return null
    }

    fun validarTrabajador(run: String?): String? {
        if (run == null) return "Selecciona un trabajador"
        return null
    }
}
