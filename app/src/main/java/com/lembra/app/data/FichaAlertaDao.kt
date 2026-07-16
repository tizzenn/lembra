package com.lembra.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FichaAlertaDao {

    @Query("SELECT * FROM fichas_alerta ORDER BY fechaInicioMillis ASC")
    fun observarTodas(): Flow<List<FichaAlerta>>

    @Query("SELECT * FROM fichas_alerta WHERE id = :id")
    suspend fun obtenerPorId(id: Long): FichaAlerta?

    @Query("SELECT * FROM fichas_alerta")
    suspend fun obtenerTodasSuspend(): List<FichaAlerta>

    @Insert
    suspend fun insertar(ficha: FichaAlerta): Long

    @Update
    suspend fun actualizar(ficha: FichaAlerta)

    @Delete
    suspend fun eliminar(ficha: FichaAlerta)

    // ── Categorías personalizadas ─────────────────────────────────

    @Query("SELECT * FROM categorias_personalizadas ORDER BY id ASC")
    fun observarCategorias(): Flow<List<CategoriaPersonalizada>>

    @Query("SELECT * FROM categorias_personalizadas ORDER BY id ASC")
    suspend fun obtenerCategoriasSuspend(): List<CategoriaPersonalizada>

    @Insert
    suspend fun insertarCategoria(categoria: CategoriaPersonalizada): Long

    @Update
    suspend fun actualizarCategoria(categoria: CategoriaPersonalizada)

    @Delete
    suspend fun eliminarCategoria(categoria: CategoriaPersonalizada)

    /** Al borrar una categoría, sus fichas pasan a otra (Varios). */
    @Query("UPDATE fichas_alerta SET categoria = :nueva WHERE categoria = :vieja")
    suspend fun reasignarCategoria(vieja: String, nueva: String)
}
