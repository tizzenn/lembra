package com.lembra.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lembra.app.config.Ajustes

/**
 * Actividad base: aplica sobre el tema los overlays del color principal y
 * de acento elegidos en Ajustes, y se recrea al volver si han cambiado.
 */
abstract class BaseActivity : AppCompatActivity() {

    private var firmaColoresAlCrear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        theme.applyStyle(Ajustes.colorPrimario(this).overlayPrimario, true)
        theme.applyStyle(Ajustes.colorAcento(this).overlayAcento, true)
        firmaColoresAlCrear = Ajustes.firmaColores(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (firmaColoresAlCrear != Ajustes.firmaColores(this)) {
            recreate()
        }
    }
}
