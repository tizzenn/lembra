package com.lembra.app.data

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.lembra.app.R

/**
 * Una categoría lista para pintar, sea fija (enum [Categoria]) o
 * personalizada ([CategoriaPersonalizada]).
 *
 * @param clave valor que se persiste en [FichaAlerta.categoria]: el nombre
 *        del enum para las fijas, `P:<id>` para las personalizadas.
 */
data class CategoriaInfo(
    val clave: String,
    val nombre: String,
    @DrawableRes val iconoRes: Int,
    @ColorInt val color: Int
)

/**
 * Punto único para resolver categorías: unifica las fijas y las
 * personalizadas, y define los iconos y colores elegibles al crear una.
 */
object CatalogoCategorias {

    /** Prefijo de las claves de categorías personalizadas (`P:<id>`). */
    const val PREFIJO = "P:"

    /** Iconos elegibles para una categoría personalizada, por clave estable. */
    val ICONOS: Map<String, Int> = linkedMapOf(
        "coche" to R.drawable.ic_cat_coche,
        "moto" to R.drawable.ic_cat_moto,
        "tarta" to R.drawable.ic_cat_tarta,
        "casa" to R.drawable.ic_cat_casa,
        "mascotas" to R.drawable.ic_cat_mascotas,
        "ninos" to R.drawable.ic_cat_ninos,
        "documento" to R.drawable.ic_cat_documentacion,
        "licencia" to R.drawable.ic_cat_licencias,
        "corazon" to R.drawable.ic_cat_corazon,
        "salud" to R.drawable.ic_cat_salud,
        "avion" to R.drawable.ic_cat_avion,
        "maletin" to R.drawable.ic_cat_maletin,
        "deporte" to R.drawable.ic_cat_deporte,
        "planta" to R.drawable.ic_cat_planta,
        "estrella" to R.drawable.ic_cat_estrella,
        "puntos" to R.drawable.ic_cat_misc
    )

    /** Colores elegibles para una categoría personalizada, por clave estable. */
    val COLORES: Map<String, Int> = linkedMapOf(
        "azul" to R.color.cat_coche,
        "morado" to R.color.cat_moto,
        "naranja" to R.color.cat_casa,
        "teal" to R.color.cat_mascotas,
        "rosa" to R.color.cat_ninos,
        "indigo" to R.color.cat_documentacion,
        "cian" to R.color.cat_licencias,
        "rojo" to R.color.cat_rojo,
        "verde" to R.color.cat_verde,
        "marron" to R.color.cat_marron,
        "amarillo" to R.color.cat_amarillo,
        "gris" to R.color.cat_misc
    )

    fun desdeEnum(context: Context, categoria: Categoria) = CategoriaInfo(
        clave = categoria.name,
        nombre = context.getString(categoria.nombreRes),
        iconoRes = categoria.iconoRes,
        color = ContextCompat.getColor(context, categoria.colorRes)
    )

    fun desdePersonalizada(context: Context, p: CategoriaPersonalizada) = CategoriaInfo(
        clave = p.clave,
        nombre = p.nombre,
        iconoRes = ICONOS[p.icono] ?: R.drawable.ic_cat_misc,
        color = ContextCompat.getColor(context, COLORES[p.color] ?: R.color.cat_misc)
    )

    /**
     * Todas las categorías (fijas + personalizadas) en el orden guardado en
     * Ajustes; las que no estén en la preferencia (nuevas) van al final.
     */
    fun catalogo(
        context: Context,
        personalizadas: List<CategoriaPersonalizada>
    ): List<CategoriaInfo> {
        val todas = Categoria.entries.map { desdeEnum(context, it) } +
            personalizadas.map { desdePersonalizada(context, it) }
        val porClave = todas.associateBy { it.clave }
        val ordenadas = com.lembra.app.config.Ajustes.ordenClaves(context)
            .mapNotNull { porClave[it] }
        return ordenadas + todas.filter { it !in ordenadas }
    }

    /** Resuelve la clave guardada en una ficha; si no existe, cae a Varios. */
    fun resolver(
        context: Context,
        clave: String,
        personalizadas: List<CategoriaPersonalizada>
    ): CategoriaInfo {
        if (clave.startsWith(PREFIJO)) {
            val id = clave.removePrefix(PREFIJO).toLongOrNull()
            personalizadas.firstOrNull { it.id == id }
                ?.let { return desdePersonalizada(context, it) }
            return desdeEnum(context, Categoria.MISC)
        }
        return desdeEnum(context, Categoria.fromNombre(clave))
    }
}
