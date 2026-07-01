package com.lembra.app.data

import androidx.annotation.StringRes
import com.lembra.app.R
import java.util.Calendar

/**
 * Unidad de tiempo para la repetición de una ficha (cada X días/semanas/meses/años).
 */
enum class UnidadRepeticion(@StringRes val nombreRes: Int, val campoCalendar: Int) {
    DIAS(R.string.unidad_dias, Calendar.DAY_OF_MONTH),
    SEMANAS(R.string.unidad_semanas, Calendar.WEEK_OF_YEAR),
    MESES(R.string.unidad_meses, Calendar.MONTH),
    ANIOS(R.string.unidad_anios, Calendar.YEAR);

    companion object {
        fun fromNombre(nombre: String): UnidadRepeticion =
            entries.firstOrNull { it.name == nombre } ?: DIAS
    }
}
