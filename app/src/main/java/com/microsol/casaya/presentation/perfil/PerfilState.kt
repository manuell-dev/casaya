package com.microsol.casaya.presentation.perfil

// Estado de la pantalla de perfil
import com.microsol.casaya.domain.model.Propiedad
import com.microsol.casaya.domain.model.Usuario

data class PerfilState(
    val usuario: Usuario? = null,
    val misPublicaciones: List<Propiedad> = emptyList(),
    val cantidadFavoritos: Int = 0,
    val cargando: Boolean = true
)