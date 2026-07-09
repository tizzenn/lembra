package com.lembra.app.config

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import com.lembra.app.R

/** Tema de la app: blanco (claro), negro (oscuro) o el que dicte el sistema. */
enum class Tema(@StringRes val nombreRes: Int, val modoNoche: Int) {
    CLARO(R.string.tema_blanco, AppCompatDelegate.MODE_NIGHT_NO),
    OSCURO(R.string.tema_negro, AppCompatDelegate.MODE_NIGHT_YES),
    SISTEMA(R.string.tema_sistema, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    companion object {
        fun fromNombre(nombre: String?): Tema =
            entries.firstOrNull { it.name == nombre } ?: SISTEMA
    }
}

/**
 * Paleta de colores elegible en Ajustes, tanto para el color principal
 * (toolbar, botón de guardar) como para el de acento (FAB, controles).
 * Cada entrada lleva sus dos overlays de tema (ver themes.xml).
 */
enum class PaletaColor(
    @StringRes val nombreRes: Int,
    @ColorRes val colorRes: Int,
    @StyleRes val overlayPrimario: Int,
    @StyleRes val overlayAcento: Int
) {
    NEGRO(R.string.color_negro, R.color.paleta_negro, R.style.Overlay_Lembra_Primario_Negro, R.style.Overlay_Lembra_Acento_Negro),
    ROJO(R.string.color_rojo, R.color.paleta_rojo, R.style.Overlay_Lembra_Primario_Rojo, R.style.Overlay_Lembra_Acento_Rojo),
    AZUL(R.string.color_azul, R.color.paleta_azul, R.style.Overlay_Lembra_Primario_Azul, R.style.Overlay_Lembra_Acento_Azul),
    VERDE(R.string.color_verde, R.color.paleta_verde, R.style.Overlay_Lembra_Primario_Verde, R.style.Overlay_Lembra_Acento_Verde),
    MORADO(R.string.color_morado, R.color.paleta_morado, R.style.Overlay_Lembra_Primario_Morado, R.style.Overlay_Lembra_Acento_Morado),
    TEAL(R.string.color_teal, R.color.paleta_teal, R.style.Overlay_Lembra_Primario_Teal, R.style.Overlay_Lembra_Acento_Teal),
    NARANJA(R.string.color_naranja, R.color.paleta_naranja, R.style.Overlay_Lembra_Primario_Naranja, R.style.Overlay_Lembra_Acento_Naranja),
    ROSA(R.string.color_rosa, R.color.paleta_rosa, R.style.Overlay_Lembra_Primario_Rosa, R.style.Overlay_Lembra_Acento_Rosa);

    companion object {
        fun fromNombre(nombre: String?, porDefecto: PaletaColor): PaletaColor =
            entries.firstOrNull { it.name == nombre } ?: porDefecto
    }
}

/** Preferencias persistentes de apariencia (tema y colores corporativos). */
object Ajustes {

    private const val PREFS = "lembra_ajustes"
    private const val CLAVE_TEMA = "tema"
    private const val CLAVE_COLOR_PRIMARIO = "color_primario"
    private const val CLAVE_COLOR_ACENTO = "color_acento"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun tema(context: Context): Tema =
        Tema.fromNombre(prefs(context).getString(CLAVE_TEMA, null))

    fun guardarTema(context: Context, tema: Tema) {
        prefs(context).edit().putString(CLAVE_TEMA, tema.name).apply()
        AppCompatDelegate.setDefaultNightMode(tema.modoNoche)
    }

    /** Aplica el modo noche guardado; llamar al arrancar la app. */
    fun aplicarTema(context: Context) {
        AppCompatDelegate.setDefaultNightMode(tema(context).modoNoche)
    }

    fun colorPrimario(context: Context): PaletaColor =
        PaletaColor.fromNombre(
            prefs(context).getString(CLAVE_COLOR_PRIMARIO, null), PaletaColor.NEGRO
        )

    fun guardarColorPrimario(context: Context, color: PaletaColor) {
        prefs(context).edit().putString(CLAVE_COLOR_PRIMARIO, color.name).apply()
    }

    fun colorAcento(context: Context): PaletaColor =
        PaletaColor.fromNombre(
            prefs(context).getString(CLAVE_COLOR_ACENTO, null), PaletaColor.ROJO
        )

    fun guardarColorAcento(context: Context, color: PaletaColor) {
        prefs(context).edit().putString(CLAVE_COLOR_ACENTO, color.name).apply()
    }

    /** Huella de los colores elegidos, para detectar cambios y recrear actividades. */
    fun firmaColores(context: Context): String =
        "${colorPrimario(context).name}/${colorAcento(context).name}"

    // ── Orden de las pestañas de categorías ───────────────────────

    private const val CLAVE_ORDEN_CATEGORIAS = "orden_categorias"

    fun ordenCategorias(context: Context): List<com.lembra.app.data.Categoria> {
        val guardado = prefs(context).getString(CLAVE_ORDEN_CATEGORIAS, null)
        val nombres = guardado?.split(",").orEmpty()
        val ordenadas = nombres.mapNotNull { nombre ->
            com.lembra.app.data.Categoria.entries.firstOrNull { it.name == nombre }
        }
        // Las categorías que no estén en la preferencia (nuevas) van al final
        return ordenadas + com.lembra.app.data.Categoria.entries.filter { !ordenadas.contains(it) }
    }

    fun guardarOrdenCategorias(context: Context, orden: List<com.lembra.app.data.Categoria>) {
        prefs(context).edit()
            .putString(CLAVE_ORDEN_CATEGORIAS, orden.joinToString(",") { it.name })
            .apply()
    }

    // ── Orden de las fichas por fecha en la lista ─────────────────

    private const val CLAVE_ORDEN_FECHA = "orden_fecha"

    fun ordenFecha(context: Context): OrdenFecha =
        OrdenFecha.fromNombre(prefs(context).getString(CLAVE_ORDEN_FECHA, null))

    fun guardarOrdenFecha(context: Context, orden: OrdenFecha) {
        prefs(context).edit().putString(CLAVE_ORDEN_FECHA, orden.name).apply()
    }
}

/** Sentido de ordenación de las fichas por su próxima fecha. */
enum class OrdenFecha {
    ASCENDENTE, DESCENDENTE;

    companion object {
        fun fromNombre(nombre: String?): OrdenFecha =
            entries.firstOrNull { it.name == nombre } ?: ASCENDENTE
    }
}
