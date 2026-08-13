// Estado de UI de la pantalla de Chat.
package com.microsol.casaya.presentation.chat

import com.microsol.casaya.domain.model.Mensaje

data class ChatState(
    val mensajes: List<Mensaje> = emptyList(),
    val enviando: Boolean = false
)