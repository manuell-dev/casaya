package com.microsol.casaya.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.microsol.casaya.domain.repository.FavoritoRepository
import com.microsol.casaya.domain.repository.PropiedadRepository
import com.microsol.casaya.domain.repository.UsuarioRepository

// Factory para crear PerfilViewModel inyectándole UsuarioRepository, PropiedadRepository y FavoritoRepository
class PerfilViewModelFactory(
    private val usuarioRepository: UsuarioRepository,
    private val propiedadRepository: PropiedadRepository,
    private val favoritoRepository: FavoritoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PerfilViewModel(usuarioRepository, propiedadRepository, favoritoRepository) as T
    }
}