




// ViewModel de Home: carga y filtra propiedades, con paginación.
package com.microsol.casaya.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.casaya.domain.model.FiltroPropiedad
import com.microsol.casaya.domain.model.RolUsuario
import com.microsol.casaya.domain.repository.PropiedadRepository
import com.microsol.casaya.domain.repository.UsuarioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val propiedadRepository: PropiedadRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Cancela la escucha anterior al cambiar de filtro, para no quedar escuchando dos listas a la vez.
    private var jobPropiedades: Job? = null

    init {
        cargarPropiedades(FiltroPropiedad())
    }

    fun aplicarFiltro(filtro: FiltroPropiedad) {
        cargarPropiedades(filtro)
    }

    // Al cambiar el filtro se descarta cualquier página adicional ya cargada.
    private fun cargarPropiedades(filtro: FiltroPropiedad) {
        jobPropiedades?.cancel()
        _state.value = _state.value.copy(
            filtro = filtro,
            cargando = true,
            propiedadesAdicionales = emptyList(),
            hayMasPaginas = true
        )

        jobPropiedades = viewModelScope.launch {
            propiedadRepository.obtenerPropiedades(filtro).collect { lista ->
                _state.value = _state.value.copy(propiedades = lista, cargando = false)
            }
        }
    }

    fun cargarMas() {
        val actual = _state.value
        val todasLasCargadas = actual.propiedades + actual.propiedadesAdicionales
        val ultima = todasLasCargadas.lastOrNull() ?: return
        if (actual.cargandoMas || !actual.hayMasPaginas) return

        viewModelScope.launch {
            _state.value = _state.value.copy(cargandoMas = true)
            val pagina = propiedadRepository.obtenerMasPropiedades(actual.filtro, ultima)
            _state.value = _state.value.copy(
                propiedadesAdicionales = _state.value.propiedadesAdicionales + pagina,
                cargandoMas = false,
                hayMasPaginas = pagina.size == 5
            )
        }
    }

    // callback(true) si el usuario es PROPIETARIO y puede publicar; false si sigue siendo CLIENTE.
    fun puedePublicar(uid: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuario(uid)
            callback(usuario?.rol == RolUsuario.PROPIETARIO)
        }
    }
}