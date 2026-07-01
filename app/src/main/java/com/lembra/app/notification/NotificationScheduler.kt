package com.lembra.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lembra.app.data.CalculadoraOcurrencias
import com.lembra.app.data.FichaAlerta

/**
 * Programa y cancela las alarmas del sistema que disparan los avisos de cada ficha.
 *
 * Cada ocurrencia de una ficha tiene su propia alarma, identificada por un
 * requestCode único = id de la ficha * 1000 + índice de la ocurrencia.
 */
object NotificationScheduler {

    private const val EXTRA_FICHA_ID = "extra_ficha_id"
    private const val EXTRA_TITULO = "extra_titulo"
    private const val EXTRA_FECHA_OCURRENCIA = "extra_fecha_ocurrencia"
    private const val EXTRA_DIAS_AVISO = "extra_dias_aviso"

    /** Cancela y vuelve a crear todas las alarmas futuras de una ficha. */
    fun reprogramar(context: Context, ficha: FichaAlerta) {
        cancelar(context, ficha)
        if (ficha.diasAviso < 0) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val ahora = System.currentTimeMillis()
        val ocurrencias = CalculadoraOcurrencias.calcularOcurrencias(ficha)

        ocurrencias.forEachIndexed { indice, fechaOcurrencia ->
            val fechaAviso = CalculadoraOcurrencias.fechaAviso(fechaOcurrencia, ficha.diasAviso)
            if (fechaAviso <= ahora) return@forEachIndexed

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_FICHA_ID, ficha.id)
                putExtra(EXTRA_TITULO, ficha.titulo)
                putExtra(EXTRA_FECHA_OCURRENCIA, fechaOcurrencia)
                putExtra(EXTRA_DIAS_AVISO, ficha.diasAviso)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode(ficha.id, indice),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fechaAviso, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fechaAviso, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fechaAviso, pendingIntent)
            }
        }
    }

    /** Cancela todas las alarmas pendientes de una ficha (usar antes de editar/eliminar). */
    fun cancelar(context: Context, ficha: FichaAlerta) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Cancelamos un rango amplio de índices por si el número de repeticiones cambió.
        for (indice in 0 until MAX_OCURRENCIAS_CANCELACION) {
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode(ficha.id, indice),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun requestCode(fichaId: Long, indiceOcurrencia: Int): Int =
        (fichaId * 1000 + indiceOcurrencia).toInt()

    private const val MAX_OCURRENCIAS_CANCELACION = 500
}
