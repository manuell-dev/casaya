// DTO de una propiedad tal como se guarda en Firestore.
package com.microsol.casaya.data.model

data class PropiedadDto(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val operacion: String = "",
    val precio: Double = 0.0,
    val zona: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val habitaciones: Int = 0,
    val banos: Int = 0,
    val areaM2: Double = 0.0,
    val fotos: List<String> = emptyList(),
    val idUsuarioPublicador: String = "",
    val fechaPublicacion: Long = 0L
)