package com.lembra.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Categoría creada por el usuario (nombre, icono y color a su gusto).
 *
 * Las fichas la referencian con la clave `P:<id>` (ver [CatalogoCategorias]),
 * que no puede colisionar con los nombres del enum [Categoria].
 *
 * @param icono clave del icono dentro de [CatalogoCategorias.ICONOS].
 * @param color clave del color dentro de [CatalogoCategorias.COLORES].
 */
@Entity(tableName = "categorias_personalizadas")
data class CategoriaPersonalizada(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val icono: String,
    val color: String
) {
    val clave: String get() = "${CatalogoCategorias.PREFIJO}$id"
}
