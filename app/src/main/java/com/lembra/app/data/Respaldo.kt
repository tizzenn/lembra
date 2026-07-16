package com.lembra.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Copia de seguridad de las fichas (y categorías personalizadas) en JSON
 * plano. No incluye la vinculación al calendario (los ids de calendario no
 * valen en otro dispositivo); al restaurar, esa opción queda desactivada.
 */
object Respaldo {

    private const val FORMATO = 2

    /** Contenido leído de un respaldo; las fichas aún llevan las claves
     *  P:<id> del dispositivo de origen (remapear al importar). */
    data class Datos(
        val fichas: List<FichaAlerta>,
        val categorias: List<CategoriaPersonalizada>
    )

    fun exportar(
        fichas: List<FichaAlerta>,
        categorias: List<CategoriaPersonalizada>
    ): String {
        val raiz = JSONObject()
        raiz.put("app", "lembra")
        raiz.put("formato", FORMATO)

        val jCategorias = JSONArray()
        for (c in categorias) {
            val j = JSONObject()
            j.put("id", c.id)
            j.put("nombre", c.nombre)
            j.put("icono", c.icono)
            j.put("color", c.color)
            jCategorias.put(j)
        }
        raiz.put("categorias", jCategorias)

        val jFichas = JSONArray()
        for (f in fichas) {
            val j = JSONObject()
            j.put("titulo", f.titulo)
            j.put("categoria", f.categoria)
            j.put("fechaInicioMillis", f.fechaInicioMillis)
            j.put("repetirCada", f.repetirCada)
            j.put("unidadRepeticion", f.unidadRepeticion)
            j.put("numeroRepeticiones", f.numeroRepeticiones)
            j.put("diasAviso", f.diasAviso)
            j.put("notas", f.notas)
            j.put("ubicacion", f.ubicacion)
            j.put("horaMinutos", f.horaMinutos)
            jFichas.put(j)
        }
        raiz.put("fichas", jFichas)
        return raiz.toString(2)
    }

    /** Lee un respaldo (formato 1 o 2); las fichas van sin id, listas para insertar. */
    fun leer(texto: String): Datos {
        val raiz = JSONObject(texto)
        require(raiz.optString("app") == "lembra") { "No es un respaldo de Lembra" }

        val categorias = mutableListOf<CategoriaPersonalizada>()
        val jCategorias = raiz.optJSONArray("categorias") ?: JSONArray()
        for (i in 0 until jCategorias.length()) {
            val j = jCategorias.getJSONObject(i)
            categorias.add(
                CategoriaPersonalizada(
                    id = j.optLong("id"),
                    nombre = j.optString("nombre"),
                    icono = j.optString("icono"),
                    color = j.optString("color")
                )
            )
        }

        val fichas = mutableListOf<FichaAlerta>()
        val jFichas = raiz.getJSONArray("fichas")
        for (i in 0 until jFichas.length()) {
            val j = jFichas.getJSONObject(i)
            fichas.add(
                FichaAlerta(
                    titulo = j.optString("titulo"),
                    categoria = j.optString("categoria", Categoria.MISC.name),
                    fechaInicioMillis = j.optLong("fechaInicioMillis"),
                    repetirCada = j.optInt("repetirCada", 1),
                    unidadRepeticion = j.optString("unidadRepeticion"),
                    numeroRepeticiones = j.optInt("numeroRepeticiones", 1),
                    diasAviso = j.optInt("diasAviso", 0),
                    notas = j.optString("notas"),
                    ubicacion = j.optString("ubicacion"),
                    horaMinutos = j.optInt("horaMinutos", -1),
                    sincronizarCalendario = false,
                    calendarioId = -1L
                )
            )
        }
        return Datos(fichas, categorias)
    }
}
