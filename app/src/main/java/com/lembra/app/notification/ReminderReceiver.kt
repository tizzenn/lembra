package com.lembra.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.lembra.app.R
import java.text.DateFormat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val fichaId = intent.getLongExtra("extra_ficha_id", -1L)
        val titulo = intent.getStringExtra("extra_titulo") ?: return
        val fechaOcurrencia = intent.getLongExtra("extra_fecha_ocurrencia", 0L)
        val diasAviso = intent.getIntExtra("extra_dias_aviso", 0)

        val formato = DateFormat.getDateInstance()
        val fechaTexto = formato.format(fechaOcurrencia)

        val notifTitulo = context.getString(R.string.notificacion_titulo, titulo)
        val notifTexto = context.getString(R.string.notificacion_texto, titulo, fechaTexto, diasAviso)

        NotificationHelper.crearCanal(context)
        val notificacion = NotificationHelper.construirNotificacion(context, notifTitulo, notifTexto).build()

        val idNotificacion = (fichaId * 1000 + (fechaOcurrencia % 1000)).toInt()
        NotificationManagerCompat.from(context).apply {
            notify(idNotificacion, notificacion)
        }
    }
}
