// ViewModel de la pantalla de favoritos: resuelve las propiedades a partir de los ids favoritos.
package com.microsol.casaya.presentation.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.casaya.domain.repository.FavoritoRepository
import com.microsol.casaya.domain.repository.PropiedadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritosViewModel(
    private val favoritoRepository: FavoritoRepository,
    private val propiedadRepository: PropiedadRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritosState())
    val state: StateFlow<FavoritosState> = _state.asStateFlow()

    fun cargarFavoritos(uid: String) {
        viewModelScope.launch {
            favoritoRepository.obtenerIdsFavoritos(uid).collect { ids ->
                _state.value = _state.value.copy(cargando = true)
                val propiedades = ids.mapNotNull { id -> propiedadRepository.obtenerPropiedadPorId(id) }
                _state.value = _state.value.copy(propiedades = propiedades, cargando = false)
            }
        }
    }
}