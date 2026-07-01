package com.lembra.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.lembra.app.data.AppDatabase
import com.lembra.app.data.Categoria
import com.lembra.app.data.FichaAlerta
import com.lembra.app.databinding.ActivityMainBinding
import com.lembra.app.ui.FichaAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: FichaAdapter
    private val dao by lazy { AppDatabase.getInstance(this).fichaAlertaDao() }

    private var todasLasFichas: List<FichaAlerta> = emptyList()
    private var categoriaSeleccionada: Categoria? = null

    private val permisoNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* si se deniega, simplemente no se muestran avisos; no bloqueamos la app */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = FichaAdapter { ficha ->
            val intent = Intent(this, AddEditCardActivity::class.java)
            intent.putExtra(AddEditCardActivity.EXTRA_FICHA_ID, ficha.id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        configurarFiltros()

        binding.fabAgregar.setOnClickListener {
            startActivity(Intent(this, AddEditCardActivity::class.java))
        }

        pedirPermisoNotificacionesSiHaceFalta()

        lifecycleScope.launch {
            dao.observarTodas().collectLatest { fichas ->
                todasLasFichas = fichas
                aplicarFiltro()
            }
        }
    }

    private fun configurarFiltros() {
        val chipTodas = Chip(this).apply {
            text = getString(R.string.filtro_todas)
            isCheckable = true
            isChecked = true
        }
        binding.chipGroupFiltro.addView(chipTodas)
        chipTodas.setOnClickListener {
            categoriaSeleccionada = null
            aplicarFiltro()
        }

        Categoria.entries.forEach { categoria ->
            val chip = Chip(this).apply {
                text = getString(categoria.nombreRes)
                isCheckable = true
            }
            binding.chipGroupFiltro.addView(chip)
            chip.setOnClickListener {
                categoriaSeleccionada = categoria
                aplicarFiltro()
            }
        }
    }

    private fun aplicarFiltro() {
        val filtradas = categoriaSeleccionada?.let { cat ->
            todasLasFichas.filter { it.categoria == cat.name }
        } ?: todasLasFichas

        adapter.submitList(filtradas)
        binding.textoVacio.visibility =
            if (filtradas.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        binding.recyclerView.visibility =
            if (filtradas.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun pedirPermisoNotificacionesSiHaceFalta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedido) {
                permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
