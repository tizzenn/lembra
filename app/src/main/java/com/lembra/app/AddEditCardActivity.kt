package com.lembra.app

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.lembra.app.calendario.CalendarioInfo
import com.lembra.app.calendario.CalendarioSync
import com.lembra.app.data.AppDatabase
import com.lembra.app.data.Categoria
import com.lembra.app.data.FichaAlerta
import com.lembra.app.data.UnidadRepeticion
import com.lembra.app.databinding.ActivityAddEditCardBinding
import com.lembra.app.notification.NotificationScheduler
import com.lembra.app.ui.crearChipIcono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Calendar

class AddEditCardActivity : BaseActivity() {

    private lateinit var binding: ActivityAddEditCardBinding
    private val dao by lazy { AppDatabase.getInstance(this).fichaAlertaDao() }

    private var fichaId: Long = -1L
    private var fichaExistente: FichaAlerta? = null
    private var categoriaSeleccionada: Categoria = Categoria.MISC
    private var fechaInicioMillis: Long? = null
    private var horaMinutos: Int = -1
    private var unidadIndice: Int = 0
    private val formatoFecha = DateFormat.getDateInstance()

    private var calendarios: List<CalendarioInfo> = emptyList()
    private var calendarioIndice: Int = 0
    private var calendarioIdPendiente: Long = -1L

    private val permisoCalendario = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        if (resultados.values.all { it }) {
            mostrarSelectorCalendario()
        } else {
            binding.switchCalendario.isChecked = false
            Toast.makeText(this, R.string.permiso_calendario_denegado, Toast.LENGTH_SHORT).show()
        }
    }

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
        configurarDesplegableUnidad()
        configurarCampoFecha()
        configurarCampoHora()
        configurarCalendario()

        binding.botonGuardar.setOnClickListener { guardar() }
        binding.botonEliminar.setOnClickListener { confirmarEliminar() }

        if (fichaId != -1L) {
            binding.botonEliminar.visibility = View.VISIBLE
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

    private fun configurarDesplegableUnidad() {
        val nombres = UnidadRepeticion.entries.map { getString(it.nombreRes) }
        binding.autoCompleteUnidad.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        )
        binding.autoCompleteUnidad.setText(nombres[unidadIndice], false)
        binding.autoCompleteUnidad.setOnItemClickListener { _, _, posicion, _ ->
            unidadIndice = posicion
        }
    }

    private fun configurarCalendario() {
        binding.switchCalendario.setOnCheckedChangeListener { _, activado ->
            if (activado) {
                if (tienePermisoCalendario()) {
                    mostrarSelectorCalendario()
                } else {
                    permisoCalendario.launch(
                        arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                    )
                }
            } else {
                binding.contenedorCalendario.visibility = View.GONE
            }
        }
    }

    private fun tienePermisoCalendario(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    private fun mostrarSelectorCalendario() {
        calendarios = CalendarioSync.listarCalendariosEscribibles(this)
        if (calendarios.isEmpty()) {
            binding.switchCalendario.isChecked = false
            Toast.makeText(this, R.string.error_sin_calendarios, Toast.LENGTH_SHORT).show()
            return
        }
        val etiquetas = calendarios.map { it.etiqueta() }
        binding.autoCompleteCalendario.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, etiquetas)
        )
        binding.autoCompleteCalendario.setOnItemClickListener { _, _, posicion, _ ->
            calendarioIndice = posicion
        }
        val indiceGuardado = calendarios.indexOfFirst { it.id == calendarioIdPendiente }
        calendarioIndice = if (indiceGuardado >= 0) indiceGuardado else 0
        binding.autoCompleteCalendario.setText(etiquetas[calendarioIndice], false)
        binding.contenedorCalendario.visibility = View.VISIBLE
    }

    private fun configurarCampoFecha() {
        binding.campoFecha.setOnClickListener {
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
                    binding.campoFecha.setText(formatoFecha.format(seleccionada.timeInMillis))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun configurarCampoHora() {
        binding.campoHora.setOnClickListener {
            val horaInicial = if (horaMinutos >= 0) horaMinutos / 60 else 9
            val minutoInicial = if (horaMinutos >= 0) horaMinutos % 60 else 0

            val dialogo = TimePickerDialog(
                this,
                { _, hora, minuto ->
                    horaMinutos = hora * 60 + minuto
                    binding.campoHora.setText(textoHora(horaMinutos))
                },
                horaInicial,
                minutoInicial,
                android.text.format.DateFormat.is24HourFormat(this)
            )
            dialogo.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.boton_quitar_hora)) { _, _ ->
                horaMinutos = -1
                binding.campoHora.setText("")
            }
            dialogo.show()
        }
    }

    private fun textoHora(minutos: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutos / 60)
            set(Calendar.MINUTE, minutos % 60)
        }
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.time)
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
            binding.campoUbicacion.setText(ficha.ubicacion)

            fechaInicioMillis = ficha.fechaInicioMillis
            binding.campoFecha.setText(formatoFecha.format(ficha.fechaInicioMillis))

            horaMinutos = ficha.horaMinutos
            if (horaMinutos >= 0) {
                binding.campoHora.setText(textoHora(horaMinutos))
            }

            categoriaSeleccionada = Categoria.fromNombre(ficha.categoria)
            for (i in 0 until binding.chipGroupCategoria.childCount) {
                val chip = binding.chipGroupCategoria.getChildAt(i) as Chip
                chip.isChecked = Categoria.entries[i] == categoriaSeleccionada
            }

            val unidad = UnidadRepeticion.fromNombre(ficha.unidadRepeticion)
            unidadIndice = UnidadRepeticion.entries.indexOf(unidad)
            binding.autoCompleteUnidad.setText(getString(unidad.nombreRes), false)

            calendarioIdPendiente = ficha.calendarioId
            binding.switchCalendario.isChecked = ficha.sincronizarCalendario
        }
    }

    private fun guardar() {
        val titulo = binding.campoTitulo.text?.toString()?.trim().orEmpty()
        if (titulo.isEmpty()) {
            binding.campoTitulo.error = getString(R.string.error_titulo_vacio)
            return
        }
        val fechaBase = fechaInicioMillis
        if (fechaBase == null) {
            Toast.makeText(this, R.string.error_fecha_vacia, Toast.LENGTH_SHORT).show()
            return
        }

        // La hora elegida (o las 9:00 por defecto) va incrustada en la fecha de inicio,
        // así todas las ocurrencias heredan la misma hora del día.
        val fecha = Calendar.getInstance().apply {
            timeInMillis = fechaBase
            val minutosDia = if (horaMinutos >= 0) horaMinutos else 9 * 60
            set(Calendar.HOUR_OF_DAY, minutosDia / 60)
            set(Calendar.MINUTE, minutosDia % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val repetirCada = binding.campoRepetirCada.text?.toString()?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val numeroRepeticiones = binding.campoNumeroRepeticiones.text?.toString()?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val diasAviso = binding.campoDiasAviso.text?.toString()?.toIntOrNull()?.coerceAtLeast(0) ?: 7
        val unidad = UnidadRepeticion.entries[unidadIndice]
        val notas = binding.campoNotas.text?.toString().orEmpty()
        val ubicacion = binding.campoUbicacion.text?.toString()?.trim().orEmpty()

        val sincronizar = binding.switchCalendario.isChecked &&
            calendarios.isNotEmpty() && calendarioIndice in calendarios.indices
        val calendarioId = if (sincronizar) calendarios[calendarioIndice].id else -1L

        val ficha = FichaAlerta(
            id = if (fichaId == -1L) 0 else fichaId,
            titulo = titulo,
            categoria = categoriaSeleccionada.name,
            fechaInicioMillis = fecha,
            repetirCada = repetirCada,
            unidadRepeticion = unidad.name,
            numeroRepeticiones = numeroRepeticiones,
            diasAviso = diasAviso,
            notas = notas,
            ubicacion = ubicacion,
            horaMinutos = horaMinutos,
            sincronizarCalendario = sincronizar,
            calendarioId = calendarioId
        )

        lifecycleScope.launch {
            val idFinal = if (fichaId == -1L) {
                dao.insertar(ficha)
            } else {
                dao.actualizar(ficha)
                ficha.id
            }
            NotificationScheduler.reprogramar(this@AddEditCardActivity, ficha.copy(id = idFinal))
            if (tienePermisoCalendario()) {
                withContext(Dispatchers.IO) {
                    CalendarioSync.sincronizar(this@AddEditCardActivity, ficha.copy(id = idFinal))
                }
            }
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
            if (tienePermisoCalendario()) {
                withContext(Dispatchers.IO) {
                    CalendarioSync.eliminarEventos(this@AddEditCardActivity, ficha.id)
                }
            }
            dao.eliminar(ficha)
            finish()
        }
    }

    companion object {
        const val EXTRA_FICHA_ID = "extra_ficha_id"
    }
}
