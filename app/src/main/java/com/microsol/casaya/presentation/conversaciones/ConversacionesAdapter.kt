// Adapter de RecyclerView para la lista de conversaciones.
package com.microsol.casaya.presentation.conversaciones

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microsol.casaya.databinding.ItemConversacionBinding
import com.microsol.casaya.domain.model.Conversacion

class ConversacionAdapter(
    private val onClick: (Conversacion) -> Unit
) : ListAdapter<Conversacion, ConversacionAdapter.ConversacionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversacionViewHolder {
        val binding = ItemConversacionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversacionViewHolder(private val binding: ItemConversacionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversacion: Conversacion) {
            binding.tvPropiedadTitulo.text = conversacion.propiedadTitulo
            binding.tvUltimoMensaje.text = conversacion.ultimoMensaje.ifBlank { "Aún no hay mensajes" }
            binding.root.setOnClickListener { onClick(conversacion) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Conversacion>() {
        override fun areItemsTheSame(oldItem: Conversacion, newItem: Conversacion) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Conversacion, newItem: Conversacion) = oldItem == newItem
    }
}