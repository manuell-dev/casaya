package com.microsol.casaya.presentation.publicar

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.microsol.casaya.domain.model.Operacion
import com.microsol.casaya.domain.model.Propiedad
import com.microsol.casaya.domain.model.TipoPropiedad
import com.microsol.casaya.domain.repository.PropiedadRepository
import com.microsol.casaya.domain.usecase.ActualizarPropiedadUseCase
import com.microsol.casaya.domain.usecase.PublicarPropiedadUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PublicarViewModel(
    private val propiedadRepository: PropiedadRepository,
    private val publicarPropiedadUseCase: PublicarPropiedadUseCase,
    private val actualizarPropiedadUseCase: ActualizarPropiedadUseCase,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow(PublicarState())
    val state: StateFlow<PublicarState> = _state.asStateFlow()

    fun actualizarUbicacion(lat: Double, lng: Double) {
        _state.value = _state.value.copy(lat = lat, lng = lng)
    }

    // Trae los datos de una propiedad ya publicada y cambia el modo a Editar
    fun cargarPropiedadParaEditar(id: String) {
        viewModelScope.launch {
            val propiedad = propiedadRepository.obtenerPropiedadPorId(id)
            if (propiedad != null) {
                _state.value = _state.value.copy(
                    modo = ModoPublicar.Editar(propiedad),
                    lat = propiedad.lat,
                    lng = propiedad.lng
                )
            }
        }
    }

    fun guardar(
        idUsuario: String,
        titulo: String,
        descripcion: String,
        tipo: TipoPropiedad,
        operacion: Operacion,
        precio: Double,
        zona: String,
        habitaciones: Int,
        banos: Int,
        areaM2: Double,
        fotoUri: Uri?
    ) {
        if (titulo.isBlank() || precio <= 0.0) {
            _state.value = _state.value.copy(error = "Completa al menos el título y el precio")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            try {
                // El compilador obliga a manejar los dos casos, no se puede olvidar ninguno
                when (val modo = _state.value.modo) {
                    is ModoPublicar.Crear -> {
                        val fotos = if (fotoUri != null) listOf(subirFoto(fotoUri)) else emptyList()
                        val propiedad = Propiedad(
                            titulo = titulo,
                            descripcion = descripcion,
                            tipo = tipo,
                            operacion = operacion,
                            precio = precio,
                            zona = zona,
                            lat = _state.value.lat,
                            lng = _state.value.lng,
                            habitaciones = habitaciones,
                            banos = banos,
                            areaM2 = areaM2,
                            fotos = fotos,
                            idUsuarioPublicador = idUsuario,
                            fechaPublicacion = System.currentTimeMillis()
                        )
                        publicarPropiedadUseCase(propiedad)
                    }
                    is ModoPublicar.Editar -> {
                        val fotos = if (fotoUri != null) listOf(subirFoto(fotoUri)) else modo.propiedad.fotos
                        val propiedad = modo.propiedad.copy(
                            titulo = titulo,
                            descripcion = descripcion,
                            tipo = tipo,
                            operacion = operacion,
                            precio = precio,
                            zona = zona,
                            lat = _state.value.lat,
                            lng = _state.value.lng,
                            habitaciones = habitaciones,
                            banos = banos,
                            areaM2 = areaM2,
                            fotos = fotos
                        )
                        actualizarPropiedadUseCase(propiedad)
                    }
                }
                _state.value = _state.value.copy(cargando = false, publicadoExitoso = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(cargando = false, error = e.message)
            }
        }
    }

    // Sube la imagen a Storage y devuelve la URL pública para guardarla en Firestore
    private suspend fun subirFoto(uri: Uri): String {
        val referencia = storage.reference.child("propiedades/${UUID.randomUUID()}.jpg")
        referencia.putFile(uri).await()
        return referencia.downloadUrl.await().toString()
    }
}