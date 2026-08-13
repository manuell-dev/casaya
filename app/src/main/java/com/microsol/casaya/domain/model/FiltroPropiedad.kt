// Filtros de búsqueda de propiedades: tipo y rango de precio.
package com.microsol.casaya.domain.model

data class FiltroPropiedad(
    val tipo: TipoPropiedad? = null,
    val precioMin: Double? = null,
    val precioMax: Double? = null
)