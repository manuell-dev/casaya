// ViewModel de la pantalla de Chat: maneja el envío y la carga de mensajes.
package com.microsol.casaya.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.casaya.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun cargarMensajes(conversacionId: String) {
        viewModelScope.launch {
            chatRepository.obtenerMensajes(conversacionId).collect { lista ->
                _state.value = _state.value.copy(mensajes = lista)
            }
        }
    }

    fun enviarMensaje(conversacionId: String, remitenteId: String, texto: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            chatRepository.enviarMensaje(conversacionId, remitenteId, texto)
        }
    }
}