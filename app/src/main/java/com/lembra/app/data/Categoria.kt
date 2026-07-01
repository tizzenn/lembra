package com.lembra.app.data

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.lembra.app.R

/**
 * Categorías disponibles para clasificar las fichas.
 */
enum class Categoria(@StringRes val nombreRes: Int, @ColorRes val colorRes: Int) {
    COCHE(R.string.cat_coche, R.color.cat_coche),
    MOTO(R.string.cat_moto, R.color.cat_moto),
    CASA(R.string.cat_casa, R.color.cat_casa),
    MASCOTAS(R.string.cat_mascotas, R.color.cat_mascotas),
    MISC(R.string.cat_misc, R.color.cat_misc);

    companion object {
        fun fromNombre(nombre: String): Categoria =
            entries.firstOrNull { it.name == nombre } ?: MISC
    }
}
