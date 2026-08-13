// Factory para crear DetalleViewModel inyectándole PropiedadRepository y FavoritoRepository.
package com.microsol.casaya.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.microsol.casaya.domain.repository.FavoritoRepository
import com.microsol.casaya.domain.repository.PropiedadRepository

class DetalleViewModelFactory(
    private val propiedadRepository: PropiedadRepository,
    private val favoritoRepository: FavoritoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DetalleViewModel(propiedadRepository, favoritoRepository) as T
    }
}