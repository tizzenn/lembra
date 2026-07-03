package com.lembra.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.lembra.app.config.Ajustes
import com.lembra.app.data.AppDatabase
import com.lembra.app.data.Categoria
import com.lembra.app.data.FichaAlerta
import com.lembra.app.databinding.ActivityMainBinding
import com.lembra.app.ui.FichaAdapter
import com.lembra.app.ui.crearChipIcono
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.accionAjustes) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        // Se reconstruyen por si el orden cambió en Ajustes
        configurarFiltros()
    }

    private fun configurarFiltros() {
        binding.chipGroupFiltro.removeAllViews()
        val chipTodas = crearChipIcono(
            this, R.drawable.ic_cat_todas, R.color.text_secondary, getString(R.string.filtro_todas)
        ).apply { isChecked = categoriaSeleccionada == null }
        binding.chipGroupFiltro.addView(chipTodas)
        chipTodas.setOnClickListener {
            categoriaSeleccionada = null
            aplicarFiltro()
        }

        Ajustes.ordenCategorias(this).forEach { categoria ->
            val chip = crearChipIcono(
                this, categoria.iconoRes, categoria.colorRes, getString(categoria.nombreRes)
            ).apply { isChecked = categoriaSeleccionada == categoria }
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
