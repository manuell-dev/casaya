// Modelo de dominio de una conversación de chat.
package com.microsol.casaya.domain.model

// propiedadTitulo va denormalizado a propósito, para no tener que buscar la propiedad solo para mostrarlo en la lista.
data class Conversacion(
    val id: String = "",
    val compradorId: String = "",
    val propietarioId: String = "",
    val propiedadId: String = "",
    val propiedadTitulo: String = "",
    val ultimoMensaje: String = "",
    val fechaUltimoMensaje: Long = 0L
)