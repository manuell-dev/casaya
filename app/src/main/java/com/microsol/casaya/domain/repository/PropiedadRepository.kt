// Contrato de Propiedades: domain no sabe dónde se persisten los datos (lo decide la capa data).
package com.microsol.casaya.domain.repository

import com.microsol.casaya.domain.model.FiltroPropiedad
import com.microsol.casaya.domain.model.Propiedad
import kotlinx.coroutines.flow.Flow

interface PropiedadRepository {

    fun obtenerPropiedades(filtro: FiltroPropiedad = FiltroPropiedad()): Flow<List<Propiedad>>

    suspend fun obtenerPropiedadPorId(id: String): Propiedad?

    suspend fun obtenerPropiedadesDeUsuario(uid: String): List<Propiedad>

    // Consulta puntual (no reactiva), pensada para usarse junto con obtenerPropiedades().
    suspend fun obtenerMasPropiedades(
        filtro: FiltroPropiedad,
        ultimaPropiedad: Propiedad,
        tamanoPagina: Int = 5
    ): List<Propiedad>

    suspend fun publicarPropiedad(propiedad: Propiedad)

    suspend fun actualizarPropiedad(propiedad: Propiedad)

    suspend fun eliminarPropiedad(id: String)
}