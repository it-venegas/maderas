package com.example.myapp.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Fechas {
    const val DIAS_LABORALES_POR_SEMANA = 5

    private const val PATRON_FECHA = "yyyy-MM-dd"
    private const val PATRON_HORA = "HH:mm"
    private const val MINUTOS_POR_HORA = 60
    private const val PRIMER_DIA_DE_FIN_DE_SEMANA = 6
    private const val DIAS_MINIMOS_EN_PRIMERA_SEMANA = 4
    private const val HORA_DEL_MEDIODIA = 12
    private const val HORA_DE_LA_NOCHE = 20

    private val localeChile: Locale = Locale.forLanguageTag("es-CL")

    // ---------- Fecha y hora actuales ----------

    fun hoy(): String = escribirFecha(Date())

    fun ahora(): String = formato(PATRON_HORA).format(Date())

    fun haceDias(cantidadDeDias: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_MONTH, -cantidadDeDias)
        return escribirFecha(calendario.time)
    }

    fun esFuturo(fecha: String): Boolean = fecha > hoy()

    // ---------- Lectura de una fecha escrita como texto ----------

    fun parse(fecha: String): Date? = try {
        formato(PATRON_FECHA).apply { isLenient = false }.parse(fecha)
    } catch (e: Exception) {
        null
    }

    // ---------- Fecha escrita para mostrar en pantalla ----------

    fun nombreDia(fecha: String): String =
        formato("EEEE", localeChile).format(fechaOHoy(fecha)).replaceFirstChar { it.uppercase() }

    fun fechaLarga(fecha: String): String {
        val diaYMes = formato("d 'de' MMMM", localeChile).format(fechaOHoy(fecha))
        return "${nombreDia(fecha)}, $diaYMes"
    }

    fun dia(fecha: String): String = formato("dd", localeChile).format(fechaOHoy(fecha))

    fun mes(fecha: String): String =
        formato("MMM", localeChile).format(fechaOHoy(fecha)).uppercase().trim('.')

    // ---------- Semanas y días de la semana ----------

    fun semanaDelAnio(fecha: String): Int {
        val calendario = calendarioDe(fecha)
        calendario.firstDayOfWeek = Calendar.MONDAY
        calendario.minimalDaysInFirstWeek = DIAS_MINIMOS_EN_PRIMERA_SEMANA
        return calendario.get(Calendar.WEEK_OF_YEAR)
    }

    /** Lunes = 1, martes = 2, ... domingo = 7. */
    fun diaSemana(fecha: String): Int {
        val diaSegunCalendar = calendarioDe(fecha).get(Calendar.DAY_OF_WEEK)
        return ((diaSegunCalendar + 5) % 7) + 1
    }

    fun inicioSemana(fecha: String): String {
        val calendario = calendarioDe(fecha)
        val diasDesdeElLunes = diaSemana(fecha) - 1
        calendario.add(Calendar.DAY_OF_MONTH, -diasDesdeElLunes)
        return escribirFecha(calendario.time)
    }

    fun esFinDeSemana(fecha: String): Boolean = diaSemana(fecha) >= PRIMER_DIA_DE_FIN_DE_SEMANA

    // ---------- Horas y duraciones ----------

    fun minutos(hora: String?): Int? {
        val partes = hora?.split(":") ?: return null
        if (partes.size != 2) return null
        val horas = partes[0].toIntOrNull() ?: return null
        val minutos = partes[1].toIntOrNull() ?: return null
        return horas * MINUTOS_POR_HORA + minutos
    }

    fun duracion(totalMinutos: Int): String {
        val horas = totalMinutos / MINUTOS_POR_HORA
        val minutosSobrantes = totalMinutos % MINUTOS_POR_HORA
        return "${horas}h ${"%02d".format(minutosSobrantes)}m"
    }

    fun saludo(): String {
        val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            horaActual < HORA_DEL_MEDIODIA -> "Buenos días"
            horaActual < HORA_DE_LA_NOCHE -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    // ---------- Apoyo interno ----------

    private fun formato(patron: String, locale: Locale = Locale.US) = SimpleDateFormat(patron, locale)

    private fun escribirFecha(fecha: Date): String = formato(PATRON_FECHA).format(fecha)

    private fun fechaOHoy(fecha: String): Date = parse(fecha) ?: Date()

    private fun calendarioDe(fecha: String): Calendar =
        Calendar.getInstance().apply { time = fechaOHoy(fecha) }
}
