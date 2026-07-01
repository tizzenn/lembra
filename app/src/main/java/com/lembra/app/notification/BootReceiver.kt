package com.lembra.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lembra.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context.applicationContext).fichaAlertaDao()
                val fichas = dao.obtenerTodasSuspend()
                fichas.forEach { ficha ->
                    NotificationScheduler.reprogramar(context.applicationContext, ficha)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
