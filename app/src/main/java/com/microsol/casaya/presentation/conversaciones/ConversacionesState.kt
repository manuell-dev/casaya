// Estado de UI de la pantalla de Conversaciones.
package com.microsol.casaya.presentation.conversaciones

import com.microsol.casaya.domain.model.Conversacion

data class ConversacionesState(
    val conversaciones: List<Conversacion> = emptyList(),
    val cargando: Boolean = true
)