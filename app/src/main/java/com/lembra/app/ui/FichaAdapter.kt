package com.lembra.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lembra.app.data.CalculadoraOcurrencias
import com.lembra.app.data.Categoria
import com.lembra.app.data.FichaAlerta
import com.lembra.app.databinding.ItemFichaBinding
import java.text.SimpleDateFormat
import java.util.Locale

class FichaAdapter(
    private val onClick: (FichaAlerta) -> Unit
) : ListAdapter<FichaAlerta, FichaAdapter.FichaViewHolder>(DIFF_CALLBACK) {

    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

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
            val categoria = Categoria.fromNombre(ficha.categoria)
            val contexto = binding.root.context

            binding.textoTitulo.text = ficha.titulo
            binding.textoCategoria.text = contexto.getString(categoria.nombreRes)
            binding.indicadorCategoria.setBackgroundColor(
                ContextCompat.getColor(contexto, categoria.colorRes)
            )

            val proxima = CalculadoraOcurrencias.proximaOcurrencia(ficha)
            if (proxima != null) {
                val (fecha, restantes) = proxima
                binding.textoProximoAviso.text =
                    contexto.getString(com.lembra.app.R.string.proximo_aviso, formatoFecha.format(fecha))
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
