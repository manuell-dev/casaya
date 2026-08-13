
// Factory para crear HomeViewModel inyectándole PropiedadRepository y UsuarioRepository.
package com.microsol.casaya.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.microsol.casaya.domain.repository.PropiedadRepository
import com.microsol.casaya.domain.repository.UsuarioRepository

class HomeViewModelFactory(
    private val propiedadRepository: PropiedadRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(propiedadRepository, usuarioRepository) as T
    }
}