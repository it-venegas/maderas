package com.example.myapp.model



/**
 * Lógica de validación centralizada y desacoplada de la UI (rúbrica IE 2.2.1).
 * Todas las funciones son puras: devuelven un mensaje de error o null si el valor es válido.
 */
object RunUtils {
    /** Deja solo dígitos y K (mayúscula). */
    fun limpiar(s: String): String = s.filter { it.isDigit() || it == 'k' || it == 'K' }.uppercase()

    /** Dígito verificador por módulo 11. */
    fun calcularDv(cuerpo: String): Char {
        var suma = 0
        var mult = 2
        for (c in cuerpo.reversed()) {
            suma += (c - '0') * mult
            mult = if (mult == 7) 2 else mult + 1
        }
        return when (val r = 11 - suma % 11) { 11 -> '0'; 10 -> 'K'; else -> '0' + r }
    }

    /** Formato visual mientras se escribe: 12.345.678-5 */
    fun formatear(input: String): String {
        val l = limpiar(input).take(9)
        if (l.length <= 1) return l
        val cuerpo = l.dropLast(1)
        return cuerpo.reversed().chunked(3).joinToString(".").reversed() + "-" + l.last()
    }

    /** Formato canónico para guardar/buscar en BD: 12345678-5 */
    fun normalizar(input: String): String {
        val l = limpiar(input)
        return if (l.length <= 1) l else l.dropLast(1) + "-" + l.last()
    }
}

object Validators {
    fun validarRun(input: String): String? {
        val l = RunUtils.limpiar(input)
        if (l.isEmpty()) return "Ingresa tu RUN"
        if (l.length < 8) return "RUN incompleto (ej: 12.345.678-5)"
        val cuerpo = l.dropLast(1)
        if (!cuerpo.all { it.isDigit() }) return "El RUN solo admite números y K al final"
        if (RunUtils.calcularDv(cuerpo) != l.last()) return "Dígito verificador incorrecto"
        return null
    }

    fun validarPassword(p: String): String? = when {
        p.isBlank() -> "Ingresa tu contraseña"
        p.length < 6 -> "Mínimo 6 caracteres"
        else -> null
    }

    fun validarFecha(f: String): String? {
        if (f.isBlank()) return "Ingresa la fecha"
        if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(f)) return "Formato AAAA-MM-DD"
        if (Fechas.parse(f) == null) return "Fecha inexistente"
        if (Fechas.esFuturo(f)) return "No puede ser una fecha futura"
        return null
    }

    fun validarHora(h: String, obligatoria: Boolean = true): String? {
        if (h.isBlank()) return if (obligatoria) "Ingresa la hora" else null
        return if (Regex("""([01]\d|2[0-3]):[0-5]\d""").matches(h)) null else "Formato HH:MM (24 h)"
    }

    fun validarSalida(entrada: String, salida: String): String? {
        validarHora(salida, obligatoria = false)?.let { return it }
        if (salida.isBlank() || validarHora(entrada) != null) return null
        val s = Fechas.minutos(salida) ?: return null
        val e = Fechas.minutos(entrada) ?: return null
        return if (s > e) null else "La salida debe ser posterior a la entrada"
    }

    fun validarTrabajador(run: String?): String? = if (run.isNullOrBlank()) "Selecciona un trabajador" else null
}
