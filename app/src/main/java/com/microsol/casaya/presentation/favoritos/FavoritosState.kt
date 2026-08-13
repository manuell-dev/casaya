// Estado de la pantalla de favoritos.
package com.microsol.casaya.presentation.favoritos

import com.microsol.casaya.domain.model.Propiedad

data class FavoritosState(
    val propiedades: List<Propiedad> = emptyList(),
    val cargando: Boolean = true
)