package com.example.myapp.model



data class ResumenSemana(val minutos: Int, val dias: Int, val diasHabiles: Int) {
    val porcentaje: Int get() = if (diasHabiles == 0) 0 else minOf(100, dias * 100 / diasHabiles)
}

object Resumen {
    /** Resumen de la semana en curso (lunes a hoy) a partir del historial del trabajador. */
    fun semana(registros: List<Asistencia>, hoy: String): ResumenSemana {
        val lunes = Fechas.inicioSemana(hoy)
        val deLaSemana = registros.filter { it.fecha >= lunes && it.fecha <= hoy }
        var minutos = 0
        for (a in deLaSemana) {
            val ini = Fechas.minutos(a.entrada) ?: continue
            val fin = Fechas.minutos(a.salida ?: if (a.fecha == hoy) Fechas.ahora() else null) ?: continue
            minutos += maxOf(0, fin - ini)
        }
        return ResumenSemana(minutos, deLaSemana.size, minOf(5, Fechas.diaSemana(hoy)))
    }
}
