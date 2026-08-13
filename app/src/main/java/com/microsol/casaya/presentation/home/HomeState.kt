
// Estado de UI de la pantalla de Home (lista, filtro y paginación).
package com.microsol.casaya.presentation.home

import com.microsol.casaya.domain.model.FiltroPropiedad
import com.microsol.casaya.domain.model.Propiedad

data class HomeState(
    val propiedades: List<Propiedad> = emptyList(),
    val propiedadesAdicionales: List<Propiedad> = emptyList(),
    val filtro: FiltroPropiedad = FiltroPropiedad(),
    val cargando: Boolean = true,
    val cargandoMas: Boolean = false,
    val hayMasPaginas: Boolean = true
)