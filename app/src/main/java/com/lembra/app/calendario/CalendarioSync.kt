package com.lembra.app.calendario

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.lembra.app.data.CalculadoraOcurrencias
import com.lembra.app.data.FichaAlerta
import java.util.TimeZone

data class CalendarioInfo(val id: Long, val nombre: String, val cuenta: String) {
    fun etiqueta(): String = if (cuenta.isBlank() || cuenta == nombre) nombre else "$nombre ($cuenta)"
}

/**
 * Vuelca las ocurrencias de una ficha como eventos en el calendario del sistema
 * (CalendarContract). Los eventos se marcan con CUSTOM_APP_URI para poder
 * regenerarlos o borrarlos sin tocar nada que no haya creado Lembra.
 */
object CalendarioSync {

    private const val URI_FICHA_PREFIX = "lembra://ficha/"
    private const val UNA_HORA_MILLIS = 60 * 60 * 1000L

    /** Calendarios del dispositivo en los que se puede escribir. */
    fun listarCalendariosEscribibles(context: Context): List<CalendarioInfo> {
        val calendarios = mutableListOf<CalendarioInfo>()
        val proyeccion = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI, proyeccion, null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val nivelAcceso = cursor.getInt(3)
                if (nivelAcceso >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                    calendarios.add(
                        CalendarioInfo(
                            id = cursor.getLong(0),
                            nombre = cursor.getString(1).orEmpty(),
                            cuenta = cursor.getString(2).orEmpty()
                        )
                    )
                }
            }
        }
        return calendarios
    }

    /**
     * Regenera los eventos de la ficha: borra los existentes y, si la
     * sincronización está activada, crea uno por cada ocurrencia con un
     * recordatorio a los días de antelación configurados.
     */
    fun sincronizar(context: Context, ficha: FichaAlerta) {
        eliminarEventos(context, ficha.id)
        if (!ficha.sincronizarCalendario || ficha.calendarioId <= 0) return

        CalculadoraOcurrencias.calcularOcurrencias(ficha).forEach { fechaOcurrencia ->
            val valores = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, ficha.calendarioId)
                put(CalendarContract.Events.TITLE, ficha.titulo)
                if (ficha.notas.isNotBlank()) {
                    put(CalendarContract.Events.DESCRIPTION, ficha.notas)
                }
                put(CalendarContract.Events.DTSTART, fechaOcurrencia)
                put(CalendarContract.Events.DTEND, fechaOcurrencia + UNA_HORA_MILLIS)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.CUSTOM_APP_PACKAGE, context.packageName)
                put(CalendarContract.Events.CUSTOM_APP_URI, URI_FICHA_PREFIX + ficha.id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }
            val uriEvento = context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI, valores
            ) ?: return@forEach

            val recordatorio = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(uriEvento))
                put(CalendarContract.Reminders.MINUTES, ficha.diasAviso * 24 * 60)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, recordatorio)
        }
    }

    /** Borra todos los eventos que Lembra creó para una ficha. */
    fun eliminarEventos(context: Context, fichaId: Long) {
        context.contentResolver.delete(
            CalendarContract.Events.CONTENT_URI,
            "${CalendarContract.Events.CUSTOM_APP_URI} = ?",
            arrayOf(URI_FICHA_PREFIX + fichaId)
        )
    }
}
