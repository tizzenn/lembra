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
import com.lembra.app.config.OrdenFecha
import com.lembra.app.data.AppDatabase
import com.lembra.app.data.CalculadoraOcurrencias
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
        // Marca el sentido de orden guardado
        when (Ajustes.ordenFecha(this)) {
            OrdenFecha.ASCENDENTE -> menu.findItem(R.id.ordenFechaAsc)
            OrdenFecha.DESCENDENTE -> menu.findItem(R.id.ordenFechaDesc)
        }.isChecked = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.accionAjustes -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.ordenFechaAsc -> {
                item.isChecked = true
                Ajustes.guardarOrdenFecha(this, OrdenFecha.ASCENDENTE)
                aplicarFiltro()
                return true
            }
            R.id.ordenFechaDesc -> {
                item.isChecked = true
                Ajustes.guardarOrdenFecha(this, OrdenFecha.DESCENDENTE)
                aplicarFiltro()
                return true
            }
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

        val ordenadas = ordenarPorFecha(filtradas)

        adapter.submitList(ordenadas)
        binding.textoVacio.visibility =
            if (ordenadas.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        binding.recyclerView.visibility =
            if (ordenadas.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }

    /**
     * Ordena las fichas por su próxima fecha (asc/desc según Ajustes).
     * Las finalizadas (sin próxima ocurrencia) van siempre al final.
     */
    private fun ordenarPorFecha(fichas: List<FichaAlerta>): List<FichaAlerta> {
        val (conFecha, sinFecha) = fichas
            .map { it to CalculadoraOcurrencias.proximaOcurrencia(it)?.first }
            .partition { it.second != null }
        val ascendentes = conFecha.sortedBy { it.second }
        val dirigidas = when (Ajustes.ordenFecha(this)) {
            OrdenFecha.ASCENDENTE -> ascendentes
            OrdenFecha.DESCENDENTE -> ascendentes.reversed()
        }
        return (dirigidas + sinFecha).map { it.first }
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
