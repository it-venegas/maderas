package com.example.myapp.model


import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Utilidades de fecha/hora. Fechas se guardan como "yyyy-MM-dd" y horas como "HH:mm". */
object Fechas {
    private val es: Locale = Locale.forLanguageTag("es-CL")
    private const val F = "yyyy-MM-dd"

    private fun fmt(p: String, l: Locale = Locale.US) = SimpleDateFormat(p, l)

    fun hoy(): String = fmt(F).format(Date())
    fun ahora(): String = fmt("HH:mm").format(Date())

    fun parse(f: String): Date? = try {
        fmt(F).apply { isLenient = false }.parse(f)
    } catch (e: Exception) { null }

    private fun cal(f: String): Calendar = Calendar.getInstance().apply { time = parse(f) ?: Date() }

    fun nombreDia(f: String): String =
        fmt("EEEE", es).format(parse(f) ?: Date()).replaceFirstChar { it.uppercase() }

    fun fechaLarga(f: String): String =
        "${nombreDia(f)}, ${fmt("d 'de' MMMM", es).format(parse(f) ?: Date())}"

    fun dia(f: String): String = fmt("dd", es).format(parse(f) ?: Date())
    fun mes(f: String): String = fmt("MMM", es).format(parse(f) ?: Date()).uppercase().trim('.')

    fun semanaDelAnio(f: String): Int =
        cal(f).apply { firstDayOfWeek = Calendar.MONDAY; minimalDaysInFirstWeek = 4 }.get(Calendar.WEEK_OF_YEAR)

    /** 1 = lunes ... 7 = domingo */
    fun diaSemana(f: String): Int = ((cal(f).get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1

    fun inicioSemana(f: String): String {
        val c = cal(f); c.add(Calendar.DAY_OF_MONTH, -(diaSemana(f) - 1)); return fmt(F).format(c.time)
    }

    fun haceDias(n: Int): String {
        val c = Calendar.getInstance(); c.add(Calendar.DAY_OF_MONTH, -n); return fmt(F).format(c.time)
    }

    fun esFinDeSemana(f: String) = diaSemana(f) >= 6
    fun esFuturo(f: String): Boolean = f > hoy()

    fun minutos(hora: String?): Int? {
        val p = hora?.split(":") ?: return null
        if (p.size != 2) return null
        val h = p[0].toIntOrNull() ?: return null
        val m = p[1].toIntOrNull() ?: return null
        return h * 60 + m
    }

    fun duracion(min: Int): String = "${min / 60}h ${"%02d".format(min % 60)}m"

    fun saludo(): String {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when { h < 12 -> "Buenos días"; h < 20 -> "Buenas tardes"; else -> "Buenas noches" }
    }
}
