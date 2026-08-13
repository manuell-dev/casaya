// ViewModel de la pantalla de detalle: carga la propiedad y su estado de favorito.
package com.microsol.casaya.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.casaya.domain.repository.FavoritoRepository
import com.microsol.casaya.domain.repository.PropiedadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val propiedadRepository: PropiedadRepository,
    private val favoritoRepository: FavoritoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetalleState())
    val state: StateFlow<DetalleState> = _state.asStateFlow()

    private lateinit var propiedadId: String
    private lateinit var uid: String

    fun cargarPropiedad(id: String, uidUsuario: String) {
        propiedadId = id
        uid = uidUsuario
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true)
            val propiedad = propiedadRepository.obtenerPropiedadPorId(id)
            _state.value = _state.value.copy(propiedad = propiedad, cargando = false)

            favoritoRepository.obtenerIdsFavoritos(uidUsuario).collect { ids ->
                _state.value = _state.value.copy(esFavorito = ids.contains(id))
            }
        }
    }

    fun alternarFavorito() {
        viewModelScope.launch {
            favoritoRepository.alternarFavorito(uid, propiedadId)
        }
    }
}