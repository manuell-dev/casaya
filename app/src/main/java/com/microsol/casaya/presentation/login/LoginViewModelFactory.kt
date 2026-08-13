package com.microsol.casaya.presentation.login

// Factory para crear LoginViewModel inyectándole UsuarioRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.microsol.casaya.domain.repository.UsuarioRepository

class LoginViewModelFactory(
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LoginViewModel(usuarioRepository) as T
    }
}