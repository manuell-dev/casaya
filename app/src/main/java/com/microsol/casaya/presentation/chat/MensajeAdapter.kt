// Adapter de RecyclerView para la lista de mensajes del chat.
package com.microsol.casaya.presentation.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microsol.casaya.R
import com.microsol.casaya.databinding.ItemMensajeBinding
import com.microsol.casaya.domain.model.Mensaje

class MensajeAdapter(
    private val uidActual: String
) : ListAdapter<Mensaje, MensajeAdapter.MensajeViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val binding = ItemMensajeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MensajeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MensajeViewHolder(private val binding: ItemMensajeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mensaje: Mensaje) {
            binding.tvTextoMensaje.text = mensaje.texto
            val esPropio = mensaje.remitenteId == uidActual
            binding.root.gravity = if (esPropio) Gravity.END else Gravity.START
            binding.tvTextoMensaje.setBackgroundResource(
                if (esPropio) R.drawable.bg_burbuja_mensaje else R.drawable.bg_burbuja_mensaje_otro
            )
            binding.tvTextoMensaje.setTextColor(
                if (esPropio) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#1A1A1A")
            )
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Mensaje>() {
        override fun areItemsTheSame(oldItem: Mensaje, newItem: Mensaje) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Mensaje, newItem: Mensaje) = oldItem == newItem
    }
}