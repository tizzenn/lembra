package com.lembra.app

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.lembra.app.config.Ajustes
import com.lembra.app.config.PaletaColor
import com.lembra.app.config.Tema
import com.lembra.app.data.AppDatabase
import com.lembra.app.data.Categoria
import com.lembra.app.data.Respaldo
import com.lembra.app.databinding.ActivitySettingsBinding
import com.lembra.app.databinding.ItemOrdenCategoriaBinding
import com.lembra.app.notification.NotificationScheduler
import com.lembra.app.ui.crearChipColor
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

        configurarOrdenCategorias()

        binding.btnExportar.setOnClickListener {
            exportarRespaldo.launch("lembra-respaldo.json")
        }
        binding.btnImportar.setOnClickListener {
            importarRespaldo.launch(
                arrayOf("application/json", "application/octet-stream", "text/plain")
            )
        }
    }

    // ── Orden de las pestañas de categorías ───────────────────────

    private fun configurarOrdenCategorias() {
        binding.listaOrdenCategorias.removeAllViews()
        val orden = Ajustes.ordenCategorias(this).toMutableList()
        orden.forEachIndexed { indice, categoria ->
            val fila = ItemOrdenCategoriaBinding.inflate(
                layoutInflater, binding.listaOrdenCategorias, false
            )
            fila.icono.setImageResource(categoria.iconoRes)
            fila.icono.setColorFilter(ContextCompat.getColor(this, categoria.colorRes))
            fila.nombre.setText(categoria.nombreRes)
            fila.btnSubir.isEnabled = indice > 0
            fila.btnBajar.isEnabled = indice < orden.size - 1
            fila.btnSubir.setOnClickListener { mover(orden, indice, -1) }
            fila.btnBajar.setOnClickListener { mover(orden, indice, +1) }
            binding.listaOrdenCategorias.addView(fila.root)
        }
    }

    private fun mover(orden: MutableList<Categoria>, indice: Int, delta: Int) {
        val destino = indice + delta
        if (destino < 0 || destino >= orden.size) return
        val tmp = orden[indice]
        orden[indice] = orden[destino]
        orden[destino] = tmp
        Ajustes.guardarOrdenCategorias(this, orden)
        configurarOrdenCategorias()
    }

    // ── Copia de seguridad ────────────────────────────────────────

    private fun escribirRespaldo(uri: Uri) {
        lifecycleScope.launch {
            try {
                val fichas = dao.obtenerTodasSuspend()
                contentResolver.openOutputStream(uri)?.use { salida ->
                    salida.write(Respaldo.exportar(fichas).toByteArray(Charsets.UTF_8))
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
                val candidatas = Respaldo.leer(texto)

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
