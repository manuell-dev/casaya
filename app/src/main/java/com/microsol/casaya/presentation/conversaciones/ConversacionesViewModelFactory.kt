// Factory para crear ConversacionesViewModel con su repositorio.
package com.microsol.casaya.presentation.conversaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.microsol.casaya.domain.repository.ChatRepository

class ConversacionesViewModelFactory(
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ConversacionesViewModel(chatRepository) as T
    }
}