package com.lembra.app.ui

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip

/** Chip con círculo de color e identificado por su nombre; para elegir colores en Ajustes. */
fun crearChipColor(
    context: Context,
    @ColorRes colorRes: Int,
    nombre: String
): Chip = Chip(context).apply {
    text = nombre
    isCheckable = true
    isCheckedIconVisible = false
    setChipIconResource(com.lembra.app.R.drawable.ic_circulo)
    isChipIconVisible = true
    chipIconTint = ContextCompat.getColorStateList(context, colorRes)
}

/** Crea un chip solo con icono (sin texto); el nombre queda como contentDescription. */
fun crearChipIcono(
    context: Context,
    @DrawableRes iconoRes: Int,
    @ColorRes colorRes: Int,
    nombre: String
): Chip {
    val densidad = context.resources.displayMetrics.density
    return Chip(context).apply {
        text = ""
        isCheckable = true
        isCheckedIconVisible = false
        setChipIconResource(iconoRes)
        isChipIconVisible = true
        chipIconTint = ContextCompat.getColorStateList(context, colorRes)
        contentDescription = nombre
        textStartPadding = 0f
        textEndPadding = 0f
        chipStartPadding = 10 * densidad
        chipEndPadding = 10 * densidad
    }
}
