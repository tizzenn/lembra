package com.lembra.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Copia de seguridad de las fichas en JSON plano. No incluye la
 * vinculación al calendario (los ids de calendario no valen en otro
 * dispositivo); al restaurar, esa opción queda desactivada.
 */
object Respaldo {

    private const val FORMATO = 1

    fun exportar(fichas: List<FichaAlerta>): String {
        val raiz = JSONObject()
        raiz.put("app", "lembra")
        raiz.put("formato", FORMATO)
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

    /** Lee las fichas de un respaldo (sin id, listas para insertar). */
    fun leer(texto: String): List<FichaAlerta> {
        val raiz = JSONObject(texto)
        require(raiz.optString("app") == "lembra") { "No es un respaldo de Lembra" }
        val jFichas = raiz.getJSONArray("fichas")
        val resultado = mutableListOf<FichaAlerta>()
        for (i in 0 until jFichas.length()) {
            val j = jFichas.getJSONObject(i)
            resultado.add(
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
        return resultado
    }
}
