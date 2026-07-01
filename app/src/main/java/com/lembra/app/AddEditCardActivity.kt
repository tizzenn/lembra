package com.lembra.app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.lembra.app.data.AppDatabase
import com.lembra.app.data.Categoria
import com.lembra.app.data.FichaAlerta
import com.lembra.app.data.UnidadRepeticion
import com.lembra.app.databinding.ActivityAddEditCardBinding
import com.lembra.app.notification.NotificationScheduler
import com.lembra.app.ui.crearChipIcono
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditCardBinding
    private val dao by lazy { AppDatabase.getInstance(this).fichaAlertaDao() }

    private var fichaId: Long = -1L
    private var fichaExistente: FichaAlerta? = null
    private var categoriaSeleccionada: Categoria = Categoria.MISC
    private var fechaInicioMillis: Long? = null
    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        fichaId = intent.getLongExtra(EXTRA_FICHA_ID, -1L)
        binding.toolbar.title = getString(
            if (fichaId == -1L) R.string.titulo_nueva_ficha else R.string.titulo_editar_ficha
        )

        configurarChipsCategoria()
        configurarSpinnerUnidad()
        configurarBotonFecha()

        binding.botonGuardar.setOnClickListener { guardar() }
        binding.botonEliminar.setOnClickListener { confirmarEliminar() }

        if (fichaId != -1L) {
            binding.botonEliminar.visibility = android.view.View.VISIBLE
            cargarFicha()
        }
    }

    private fun configurarChipsCategoria() {
        Categoria.entries.forEach { categoria ->
            val chip = crearChipIcono(
                this, categoria.iconoRes, categoria.colorRes, getString(categoria.nombreRes)
            ).apply {
                isChecked = categoria == categoriaSeleccionada
            }
            chip.setOnClickListener { categoriaSeleccionada = categoria }
            binding.chipGroupCategoria.addView(chip)
        }
    }

    private fun configurarSpinnerUnidad() {
        val nombres = UnidadRepeticion.entries.map { getString(it.nombreRes) }
        binding.spinnerUnidad.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, nombres
        )
    }

    private fun configurarBotonFecha() {
        binding.botonFecha.setOnClickListener {
            val cal = Calendar.getInstance()
            fechaInicioMillis?.let { cal.timeInMillis = it }

            DatePickerDialog(
                this,
                { _, anio, mes, dia ->
                    val seleccionada = Calendar.getInstance().apply {
                        set(anio, mes, dia, 9, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    fechaInicioMillis = seleccionada.timeInMillis
                    binding.textoFechaSeleccionada.text = formatoFecha.format(seleccionada.timeInMillis)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun cargarFicha() {
        lifecycleScope.launch {
            val ficha = dao.obtenerPorId(fichaId) ?: return@launch
            fichaExistente = ficha

            binding.campoTitulo.setText(ficha.titulo)
            binding.campoRepetirCada.setText(ficha.repetirCada.toString())
            binding.campoNumeroRepeticiones.setText(ficha.numeroRepeticiones.toString())
            binding.campoDiasAviso.setText(ficha.diasAviso.toString())
            binding.campoNotas.setText(ficha.notas)

            fechaInicioMillis = ficha.fechaInicioMillis
            binding.textoFechaSeleccionada.text = formatoFecha.format(ficha.fechaInicioMillis)

            categoriaSeleccionada = Categoria.fromNombre(ficha.categoria)
            for (i in 0 until binding.chipGroupCategoria.childCount) {
                val chip = binding.chipGroupCategoria.getChildAt(i) as Chip
                chip.isChecked = Categoria.entries[i] == categoriaSeleccionada
            }

            val unidad = UnidadRepeticion.fromNombre(ficha.unidadRepeticion)
            binding.spinnerUnidad.setSelection(UnidadRepeticion.entries.indexOf(unidad))
        }
    }

    private fun guardar() {
        val titulo = binding.campoTitulo.text?.toString()?.trim().orEmpty()
        if (titulo.isEmpty()) {
            binding.campoTitulo.error = getString(R.string.error_titulo_vacio)
            return
        }
        val fecha = fechaInicioMillis
        if (fecha == null) {
            android.widget.Toast.makeText(this, R.string.error_fecha_vacia, android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val repetirCada = binding.campoRepetirCada.text?.toString()?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val numeroRepeticiones = binding.campoNumeroRepeticiones.text?.toString()?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val diasAviso = binding.campoDiasAviso.text?.toString()?.toIntOrNull()?.coerceAtLeast(0) ?: 7
        val unidad = UnidadRepeticion.entries[binding.spinnerUnidad.selectedItemPosition]
        val notas = binding.campoNotas.text?.toString().orEmpty()

        val ficha = FichaAlerta(
            id = if (fichaId == -1L) 0 else fichaId,
            titulo = titulo,
            categoria = categoriaSeleccionada.name,
            fechaInicioMillis = fecha,
            repetirCada = repetirCada,
            unidadRepeticion = unidad.name,
            numeroRepeticiones = numeroRepeticiones,
            diasAviso = diasAviso,
            notas = notas
        )

        lifecycleScope.launch {
            val idFinal = if (fichaId == -1L) {
                dao.insertar(ficha)
            } else {
                dao.actualizar(ficha)
                ficha.id
            }
            NotificationScheduler.reprogramar(this@AddEditCardActivity, ficha.copy(id = idFinal))
            finish()
        }
    }

    private fun confirmarEliminar() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialogo_eliminar_titulo)
            .setMessage(R.string.dialogo_eliminar_mensaje)
            .setPositiveButton(R.string.boton_eliminar) { _, _ -> eliminar() }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun eliminar() {
        val ficha = fichaExistente ?: return
        lifecycleScope.launch {
            NotificationScheduler.cancelar(this@AddEditCardActivity, ficha)
            dao.eliminar(ficha)
            finish()
        }
    }

    companion object {
        const val EXTRA_FICHA_ID = "extra_ficha_id"
    }
}
