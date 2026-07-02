package com.lembra.app

import android.os.Bundle
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.lembra.app.config.Ajustes
import com.lembra.app.config.PaletaColor
import com.lembra.app.config.Tema
import com.lembra.app.databinding.ActivitySettingsBinding
import com.lembra.app.ui.crearChipColor

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

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
    }

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
