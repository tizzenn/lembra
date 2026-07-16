package com.lembra.app

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lembra.app.config.Ajustes
import com.lembra.app.config.PaletaColor
import com.lembra.app.config.Tema
import com.lembra.app.data.AppDatabase
import com.lembra.app.data.CatalogoCategorias
import com.lembra.app.data.Categoria
import com.lembra.app.data.CategoriaPersonalizada
import com.lembra.app.data.Respaldo
import com.lembra.app.databinding.ActivitySettingsBinding
import com.lembra.app.databinding.DialogCategoriaBinding
import com.lembra.app.databinding.ItemOrdenCategoriaBinding
import com.lembra.app.notification.NotificationScheduler
import com.lembra.app.ui.crearChipColor
import com.lembra.app.ui.crearChipIcono
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val dao by lazy { AppDatabase.getInstance(this).fichaAlertaDao() }

    private val exportarRespaldo = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) escribirRespaldo(uri) }

    private val importarRespaldo = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) leerRespaldo(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        configurarSelectorTema()
        configurarSelectorColor(
            binding.chipsColorPrimario,
            actual = { Ajustes.colorPrimario(this) }
        ) { color -> Ajustes.guardarColorPrimario(this, color) }
        configurarSelectorColor(
            binding.chipsColorAcento,
            actual = { Ajustes.colorAcento(this) }
        ) { color -> Ajustes.guardarColorAcento(this, color) }

        binding.btnNuevaCategoria.setOnClickListener { abrirDialogoCategoria(null) }
        recargarCategorias()

        binding.btnExportar.setOnClickListener {
            exportarRespaldo.launch("lembra-respaldo.json")
        }
        binding.btnImportar.setOnClickListener {
            importarRespaldo.launch(
                arrayOf("application/json", "application/octet-stream", "text/plain")
            )
        }
    }

    // ── Categorías: orden y personalizadas ────────────────────────

    private var personalizadas: List<CategoriaPersonalizada> = emptyList()

    private fun recargarCategorias() {
        lifecycleScope.launch {
            personalizadas = dao.obtenerCategoriasSuspend()
            pintarListaCategorias()
        }
    }

    private fun pintarListaCategorias() {
        binding.listaOrdenCategorias.removeAllViews()
        val catalogo = CatalogoCategorias.catalogo(this, personalizadas)
        val orden = catalogo.map { it.clave }.toMutableList()
        catalogo.forEachIndexed { indice, categoria ->
            val fila = ItemOrdenCategoriaBinding.inflate(
                layoutInflater, binding.listaOrdenCategorias, false
            )
            fila.icono.setImageResource(categoria.iconoRes)
            fila.icono.setColorFilter(categoria.color)
            fila.nombre.text = categoria.nombre
            fila.btnSubir.isEnabled = indice > 0
            fila.btnBajar.isEnabled = indice < catalogo.size - 1
            fila.btnSubir.setOnClickListener { mover(orden, indice, -1) }
            fila.btnBajar.setOnClickListener { mover(orden, indice, +1) }
            if (categoria.clave.startsWith(CatalogoCategorias.PREFIJO)) {
                fila.btnEditar.visibility = android.view.View.VISIBLE
                val id = categoria.clave.removePrefix(CatalogoCategorias.PREFIJO).toLongOrNull()
                fila.btnEditar.setOnClickListener {
                    personalizadas.firstOrNull { it.id == id }?.let { abrirDialogoCategoria(it) }
                }
            }
            binding.listaOrdenCategorias.addView(fila.root)
        }
    }

    private fun mover(orden: MutableList<String>, indice: Int, delta: Int) {
        val destino = indice + delta
        if (destino < 0 || destino >= orden.size) return
        val tmp = orden[indice]
        orden[indice] = orden[destino]
        orden[destino] = tmp
        Ajustes.guardarOrdenClaves(this, orden)
        pintarListaCategorias()
    }

    /** Diálogo de creación (existente == null) o edición de una categoría. */
    private fun abrirDialogoCategoria(existente: CategoriaPersonalizada?) {
        val vista = DialogCategoriaBinding.inflate(layoutInflater)
        vista.campoNombreCategoria.setText(existente?.nombre.orEmpty())

        var iconoElegido = existente?.icono ?: CatalogoCategorias.ICONOS.keys.first()
        var colorElegido = existente?.color ?: CatalogoCategorias.COLORES.keys.first()

        CatalogoCategorias.ICONOS.forEach { (clave, iconoRes) ->
            val chip = crearChipIcono(
                this, iconoRes, ContextCompat.getColor(this, R.color.text_primary), clave
            ).apply { isChecked = clave == iconoElegido }
            chip.setOnClickListener { iconoElegido = clave }
            vista.chipsIcono.addView(chip)
        }
        CatalogoCategorias.COLORES.forEach { (clave, colorRes) ->
            val chip = crearChipIcono(
                this, R.drawable.ic_circulo, ContextCompat.getColor(this, colorRes), clave
            ).apply { isChecked = clave == colorElegido }
            chip.setOnClickListener { colorElegido = clave }
            vista.chipsColor.addView(chip)
        }

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(if (existente == null) R.string.cat_nueva else R.string.cat_editar)
            .setView(vista.root)
            .setPositiveButton(R.string.boton_guardar, null)
            .setNegativeButton(R.string.cancelar, null)
            .apply {
                if (existente != null) {
                    setNeutralButton(R.string.boton_eliminar) { _, _ ->
                        confirmarEliminarCategoria(existente)
                    }
                }
            }
            .show()

        // Positivo manual para poder validar el nombre sin cerrar el diálogo
        dialogo.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = vista.campoNombreCategoria.text?.toString()?.trim().orEmpty()
            if (nombre.isEmpty()) {
                vista.campoNombreCategoria.error = getString(R.string.cat_error_nombre)
                return@setOnClickListener
            }
            lifecycleScope.launch {
                if (existente == null) {
                    dao.insertarCategoria(
                        CategoriaPersonalizada(nombre = nombre, icono = iconoElegido, color = colorElegido)
                    )
                } else {
                    dao.actualizarCategoria(
                        existente.copy(nombre = nombre, icono = iconoElegido, color = colorElegido)
                    )
                }
                recargarCategorias()
                dialogo.dismiss()
            }
        }
    }

    private fun confirmarEliminarCategoria(categoria: CategoriaPersonalizada) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cat_eliminar_titulo)
            .setMessage(R.string.cat_eliminar_mensaje)
            .setPositiveButton(R.string.boton_eliminar) { _, _ ->
                lifecycleScope.launch {
                    dao.reasignarCategoria(categoria.clave, Categoria.MISC.name)
                    dao.eliminarCategoria(categoria)
                    Ajustes.guardarOrdenClaves(
                        this@SettingsActivity,
                        Ajustes.ordenClaves(this@SettingsActivity).filter { it != categoria.clave }
                    )
                    recargarCategorias()
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    // ── Copia de seguridad ────────────────────────────────────────

    private fun escribirRespaldo(uri: Uri) {
        lifecycleScope.launch {
            try {
                val fichas = dao.obtenerTodasSuspend()
                val categorias = dao.obtenerCategoriasSuspend()
                contentResolver.openOutputStream(uri)?.use { salida ->
                    salida.write(
                        Respaldo.exportar(fichas, categorias).toByteArray(Charsets.UTF_8)
                    )
                }
                Toast.makeText(this@SettingsActivity, R.string.respaldo_exportado, Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, R.string.respaldo_error, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun leerRespaldo(uri: Uri) {
        lifecycleScope.launch {
            try {
                val texto = contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(Charsets.UTF_8)
                } ?: throw IllegalStateException("Sin contenido")
                val datos = Respaldo.leer(texto)

                // Categorías personalizadas: se reutiliza la equivalente si ya
                // existe (mismo nombre); si no, se crea. Los ids del respaldo
                // no valen aquí, así que se remapean las claves P:<id>.
                val mapaClaves = mutableMapOf<String, String>()
                for (categoria in datos.categorias) {
                    val actual = dao.obtenerCategoriasSuspend()
                    val equivalente = actual.firstOrNull {
                        it.nombre.equals(categoria.nombre, ignoreCase = true)
                    }
                    val idFinal = equivalente?.id
                        ?: dao.insertarCategoria(categoria.copy(id = 0))
                    mapaClaves[categoria.clave] =
                        "${CatalogoCategorias.PREFIJO}$idFinal"
                }
                val candidatas = datos.fichas.map { ficha ->
                    mapaClaves[ficha.categoria]
                        ?.let { ficha.copy(categoria = it) } ?: ficha
                }

                // No se duplican fichas idénticas ya presentes
                val existentes = dao.obtenerTodasSuspend()
                    .map { Triple(it.titulo, it.fechaInicioMillis, it.categoria) }
                    .toHashSet()
                var importadas = 0
                for (ficha in candidatas) {
                    if (existentes.contains(
                            Triple(ficha.titulo, ficha.fechaInicioMillis, ficha.categoria)
                        )
                    ) continue
                    val id = dao.insertar(ficha)
                    NotificationScheduler.reprogramar(this@SettingsActivity, ficha.copy(id = id))
                    importadas++
                }
                recargarCategorias()
                Toast.makeText(
                    this@SettingsActivity,
                    getString(R.string.respaldo_importado, importadas),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, R.string.respaldo_error, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    // ── Tema y colores ────────────────────────────────────────────

    private fun configurarSelectorTema() {
        val botonActual = when (Ajustes.tema(this)) {
            Tema.CLARO -> binding.botonTemaClaro
            Tema.OSCURO -> binding.botonTemaOscuro
            Tema.SISTEMA -> binding.botonTemaSistema
        }
        binding.grupoTema.check(botonActual.id)

        binding.grupoTema.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val tema = when (checkedId) {
                binding.botonTemaClaro.id -> Tema.CLARO
                binding.botonTemaOscuro.id -> Tema.OSCURO
                else -> Tema.SISTEMA
            }
            if (tema != Ajustes.tema(this)) {
                // setDefaultNightMode recrea las actividades por sí solo
                Ajustes.guardarTema(this, tema)
            }
        }
    }

    private fun configurarSelectorColor(
        grupo: ChipGroup,
        actual: () -> PaletaColor,
        alElegir: (PaletaColor) -> Unit
    ) {
        PaletaColor.entries.forEach { color ->
            val chip = crearChipColor(this, color.colorRes, getString(color.nombreRes)).apply {
                isChecked = color == actual()
            }
            chip.setOnClickListener {
                if (color != actual()) {
                    alElegir(color)
                    recreate() // repinta esta pantalla con el color nuevo
                }
            }
            grupo.addView(chip)
        }
    }
}
