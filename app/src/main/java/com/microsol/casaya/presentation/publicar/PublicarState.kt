package com.microsol.casaya.presentation.publicar

data class PublicarState(
    val modo: ModoPublicar = ModoPublicar.Crear,
    val lat: Double = -12.0464,
    val lng: Double = -77.0428, // arranca centrado en Lima, como en GoogleMapsStarter
    val cargando: Boolean = false,
    val error: String? = null,
    val publicadoExitoso: Boolean = false
)