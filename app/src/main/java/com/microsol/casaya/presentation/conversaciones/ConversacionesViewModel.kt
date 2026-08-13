// ViewModel de la pantalla de Conversaciones: carga la lista de chats del usuario.
package com.microsol.casaya.presentation.conversaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.casaya.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConversacionesViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConversacionesState())
    val state: StateFlow<ConversacionesState> = _state.asStateFlow()

    fun cargarConversaciones(uid: String) {
        viewModelScope.launch {
            chatRepository.obtenerConversaciones(uid).collect { lista ->
                _state.value = _state.value.copy(conversaciones = lista, cargando = false)
            }
        }
    }
}