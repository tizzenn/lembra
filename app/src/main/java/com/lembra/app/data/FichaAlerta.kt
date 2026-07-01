package com.lembra.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una ficha de alerta/recordatorio.
 *
 * @param fechaInicioMillis fecha de la primera ocurrencia (epoch millis, medianoche local).
 * @param repetirCada cada cuántas unidades se repite (p.ej. 6 si son "cada 6 meses").
 * @param unidadRepeticion unidad de [repetirCada] (días, semanas, meses, años).
 * @param numeroRepeticiones número total de ocurrencias de esta ficha (mínimo 1).
 * @param diasAviso días de antelación con los que se notifica antes de cada ocurrencia.
 * @param sincronizarCalendario si las ocurrencias se vuelcan al calendario del sistema.
 * @param calendarioId id del calendario destino (CalendarContract), -1 si ninguno.
 */
@Entity(tableName = "fichas_alerta")
data class FichaAlerta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val categoria: String,
    val fechaInicioMillis: Long,
    val repetirCada: Int,
    val unidadRepeticion: String,
    val numeroRepeticiones: Int,
    val diasAviso: Int,
    val notas: String = "",
    @ColumnInfo(defaultValue = "0") val sincronizarCalendario: Boolean = false,
    @ColumnInfo(defaultValue = "-1") val calendarioId: Long = -1L
)
