
// Adapter de RecyclerView para la lista de propiedades (Home y Favoritos).
package com.microsol.casaya.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.microsol.casaya.databinding.ItemPropiedadBinding
import com.microsol.casaya.domain.model.Propiedad

class PropiedadAdapter(
    private val onClickPropiedad: (Propiedad) -> Unit,
    private val onLongClickPropiedad: ((Propiedad) -> Unit)? = null
) : ListAdapter<Propiedad, PropiedadAdapter.PropiedadViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropiedadViewHolder {
        val binding = ItemPropiedadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PropiedadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropiedadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PropiedadViewHolder(private val binding: ItemPropiedadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(propiedad: Propiedad) {
            binding.tvPrecio.text = "S/ ${"%,.0f".format(propiedad.precio)}"
            binding.tvTitulo.text = propiedad.titulo
            binding.tvZonaTipo.text = "${propiedad.zona} · ${propiedad.tipo.name.lowercase().replaceFirstChar { it.uppercase() }}"
            binding.ivFoto.load(propiedad.fotos.firstOrNull())
            binding.root.setOnClickListener { onClickPropiedad(propiedad) }
            binding.root.setOnLongClickListener {
                onLongClickPropiedad?.invoke(propiedad)
                onLongClickPropiedad != null
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Propiedad>() {
        override fun areItemsTheSame(oldItem: Propiedad, newItem: Propiedad) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Propiedad, newItem: Propiedad) = oldItem == newItem
    }
}