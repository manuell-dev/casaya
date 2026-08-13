// Estado de la pantalla de detalle de una propiedad.
package com.microsol.casaya.presentation.detalle

import com.microsol.casaya.domain.model.Propiedad

data class DetalleState(
    val propiedad: Propiedad? = null,
    val cargando: Boolean = true,
    val esFavorito: Boolean = false
)