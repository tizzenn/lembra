package com.lembra.app

import android.app.Application
import com.lembra.app.notification.NotificationHelper

class LembraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearCanal(this)
    }
}
