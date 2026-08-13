// Modelo de dominio de un mensaje de chat.
package com.microsol.casaya.domain.model

data class Mensaje(
    val id: String = "",
    val conversacionId: String = "",
    val remitenteId: String = "",
    val texto: String = "",
    val fecha: Long = 0L
)