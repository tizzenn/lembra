package com.lembra.app.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lembra.app.data.CalculadoraOcurrencias
import com.lembra.app.data.CatalogoCategorias
import com.lembra.app.data.CategoriaPersonalizada
import com.lembra.app.data.FichaAlerta
import com.lembra.app.databinding.ItemFichaBinding
import java.text.DateFormat

class FichaAdapter(
    private val onClick: (FichaAlerta) -> Unit
) : ListAdapter<FichaAlerta, FichaAdapter.FichaViewHolder>(DIFF_CALLBACK) {

    private val formatoFecha = DateFormat.getDateInstance()
    private val formatoHora = DateFormat.getTimeInstance(DateFormat.SHORT)

    /** Categorías personalizadas vigentes, para resolver las claves P:<id>. */
    private var personalizadas: List<CategoriaPersonalizada> = emptyList()

    @Suppress("NotifyDataSetChanged") // cambian potencialmente todas las filas
    fun actualizarCategorias(nuevas: List<CategoriaPersonalizada>) {
        if (nuevas == personalizadas) return
        personalizadas = nuevas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FichaViewHolder {
        val binding = ItemFichaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FichaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FichaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FichaViewHolder(private val binding: ItemFichaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ficha: FichaAlerta) {
            val contexto = binding.root.context
            val categoria = CatalogoCategorias.resolver(contexto, ficha.categoria, personalizadas)

            binding.textoTitulo.text = ficha.titulo
            binding.iconoCategoria.setImageResource(categoria.iconoRes)
            binding.iconoCategoria.imageTintList = ColorStateList.valueOf(categoria.color)
            binding.iconoCategoria.contentDescription = categoria.nombre
            binding.indicadorCategoria.setBackgroundColor(categoria.color)

            val proxima = CalculadoraOcurrencias.proximaOcurrencia(ficha)
            if (proxima != null) {
                val (fecha, restantes) = proxima
                // Con hora elegida se muestra "fecha - hora"; si no, solo la fecha.
                val textoFecha = if (ficha.horaMinutos >= 0) {
                    "${formatoFecha.format(fecha)} - ${formatoHora.format(fecha)}"
                } else {
                    formatoFecha.format(fecha)
                }
                binding.textoProximoAviso.text =
                    contexto.getString(com.lembra.app.R.string.proximo_aviso, textoFecha)
                binding.textoRepeticiones.text = contexto.getString(
                    com.lembra.app.R.string.repeticiones_restantes, restantes, ficha.numeroRepeticiones
                )
            } else {
                binding.textoProximoAviso.text = ""
                binding.textoRepeticiones.text = contexto.getString(com.lembra.app.R.string.sin_repeticiones_restantes)
            }

            binding.root.setOnClickListener { onClick(ficha) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FichaAlerta>() {
            override fun areItemsTheSame(oldItem: FichaAlerta, newItem: FichaAlerta) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: FichaAlerta, newItem: FichaAlerta) =
                oldItem == newItem
        }
    }
}
