package com.microsol.casaya.presentation.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.microsol.casaya.domain.model.RolUsuario
import com.microsol.casaya.domain.repository.FavoritoRepository
import com.microsol.casaya.domain.repository.PropiedadRepository
import com.microsol.casaya.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PerfilViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val propiedadRepository: PropiedadRepository,
    private val favoritoRepository: FavoritoRepository,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow(PerfilState())
    val state: StateFlow<PerfilState> = _state.asStateFlow()

    fun cargarPerfil(uid: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true)
            val usuario = usuarioRepository.obtenerUsuario(uid)

            // Solo pedimos las publicaciones si ya es propietario, para no
            // hacer una consulta a Firestore que no se va a usar
            val publicaciones = if (usuario?.rol == RolUsuario.PROPIETARIO) {
                propiedadRepository.obtenerPropiedadesDeUsuario(uid)
            } else {
                emptyList()
            }

            // first(): solo tomamos la primera lista que llega, no nos quedamos
            // escuchando cambios en tiempo real aquí (eso ya lo hace FavoritosFragment)
            val cantidadFavoritos = favoritoRepository.obtenerIdsFavoritos(uid).first().size

            _state.value = _state.value.copy(
                usuario = usuario,
                misPublicaciones = publicaciones,
                cantidadFavoritos = cantidadFavoritos,
                cargando = false
            )
        }
    }

    fun convertirEnPropietario(uid: String) {
        viewModelScope.launch {
            usuarioRepository.convertirEnPropietario(uid)
            cargarPerfil(uid) // recarga el perfil para reflejar el nuevo rol
        }
    }

    fun eliminarPropiedad(uid: String, propiedadId: String) {
        viewModelScope.launch {
            propiedadRepository.eliminarPropiedad(propiedadId)
            cargarPerfil(uid) // recarga "Mis publicaciones" sin la que se borró
        }
    }

    fun actualizarPerfil(uid: String, nombre: String, fotoUri: Uri?) {
        viewModelScope.launch {
            val fotoUrl = if (fotoUri != null) subirFotoPerfil(uid, fotoUri) else null
            usuarioRepository.actualizarPerfil(uid, nombre, fotoUrl)
            cargarPerfil(uid) // recarga para reflejar el nombre/foto nuevos
        }
    }

    private suspend fun subirFotoPerfil(uid: String, uri: Uri): String {
        val referencia = storage.reference.child("perfiles/$uid-${UUID.randomUUID()}.jpg")
        referencia.putFile(uri).await()
        return referencia.downloadUrl.await().toString()
    }
}