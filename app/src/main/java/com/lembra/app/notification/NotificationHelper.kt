package com.lembra.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lembra.app.R

object NotificationHelper {

    const val CANAL_ID = "lembra_avisos"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val canalExistente = manager.getNotificationChannel(CANAL_ID)
            if (canalExistente == null) {
                val canal = NotificationChannel(
                    CANAL_ID,
                    context.getString(R.string.canal_notificaciones_nombre),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.canal_notificaciones_desc)
                }
                manager.createNotificationChannel(canal)
            }
        }
    }

    fun construirNotificacion(
        context: Context,
        titulo: String,
        texto: String
    ): androidx.core.app.NotificationCompat.Builder =
        NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
}
