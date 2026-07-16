package com.lembra.app.data

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lembra.app.R

/**
 * Categorías disponibles para clasificar las fichas.
 */
enum class Categoria(
    @StringRes val nombreRes: Int,
    @ColorRes val colorRes: Int,
    @DrawableRes val iconoRes: Int
) {
    COCHE(R.string.cat_coche, R.color.cat_coche, R.drawable.ic_cat_coche),
    // Constante MOTO conservada por compatibilidad con fichas/backups previos;
    // ahora representa "Celebraciones" (icono de tarta).
    MOTO(R.string.cat_celebraciones, R.color.cat_moto, R.drawable.ic_cat_tarta),
    CASA(R.string.cat_casa, R.color.cat_casa, R.drawable.ic_cat_casa),
    MASCOTAS(R.string.cat_mascotas, R.color.cat_mascotas, R.drawable.ic_cat_mascotas),
    NINOS(R.string.cat_ninos, R.color.cat_ninos, R.drawable.ic_cat_ninos),
    DOCUMENTACION(R.string.cat_documentacion, R.color.cat_documentacion, R.drawable.ic_cat_documentacion),
    LICENCIAS(R.string.cat_licencias, R.color.cat_licencias, R.drawable.ic_cat_licencias),
    MISC(R.string.cat_misc, R.color.cat_misc, R.drawable.ic_cat_misc);

    companion object {
        fun fromNombre(nombre: String): Categoria =
            entries.firstOrNull { it.name == nombre } ?: MISC
    }
}
