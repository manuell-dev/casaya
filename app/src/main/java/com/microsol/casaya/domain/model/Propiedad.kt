// Modelo de dominio de una propiedad publicada en la app.
package com.microsol.casaya.domain.model

data class Propiedad(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: TipoPropiedad = TipoPropiedad.DEPARTAMENTO,
    val operacion: Operacion = Operacion.VENTA,
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