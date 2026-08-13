// Factory para crear FavoritosViewModel inyectándole FavoritoRepository y PropiedadRepository.
package com.microsol.casaya.presentation.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.microsol.casaya.domain.repository.FavoritoRepository
import com.microsol.casaya.domain.repository.PropiedadRepository

class FavoritosViewModelFactory(
    private val favoritoRepository: FavoritoRepository,
    private val propiedadRepository: PropiedadRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FavoritosViewModel(favoritoRepository, propiedadRepository) as T
    }
}