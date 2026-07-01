package com.lembra.app.data

import java.util.Calendar

/**
 * Calcula las fechas de las distintas ocurrencias de una ficha y cuál es
 * la próxima pendiente, teniendo en cuenta la antelación configurada.
 */
object CalculadoraOcurrencias {

    /** Devuelve la lista de fechas (epoch millis) de todas las ocurrencias de la ficha. */
    fun calcularOcurrencias(ficha: FichaAlerta): List<Long> {
        val unidad = UnidadRepeticion.fromNombre(ficha.unidadRepeticion)
        val resultado = mutableListOf<Long>()
        val cal = Calendar.getInstance().apply { timeInMillis = ficha.fechaInicioMillis }

        repeat(ficha.numeroRepeticiones.coerceAtLeast(1)) { indice ->
            if (indice > 0) {
                cal.add(unidad.campoCalendar, ficha.repetirCada.coerceAtLeast(1))
            }
            resultado.add(cal.timeInMillis)
        }
        return resultado
    }

    /**
     * Próxima ocurrencia futura (a partir de ahora) junto con cuántas quedan en total,
     * o null si ya han pasado todas.
     */
    fun proximaOcurrencia(ficha: FichaAlerta, ahora: Long = System.currentTimeMillis()): Pair<Long, Int>? {
        val ocurrencias = calcularOcurrencias(ficha)
        val restantes = ocurrencias.filter { it >= ahora }
        val proxima = restantes.minOrNull() ?: return null
        return proxima to restantes.size
    }

    /** Fecha (epoch millis) en la que debe dispararse el aviso para una ocurrencia dada. */
    fun fechaAviso(fechaOcurrencia: Long, diasAviso: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = fechaOcurrencia
            add(Calendar.DAY_OF_MONTH, -diasAviso)
        }
        return cal.timeInMillis
    }
}
