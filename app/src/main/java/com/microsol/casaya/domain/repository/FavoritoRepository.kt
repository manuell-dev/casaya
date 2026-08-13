// Contrato de Favoritos: solo guarda ids, no las propiedades completas.
package com.microsol.casaya.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoritoRepository {

    fun obtenerIdsFavoritos(uid: String): Flow<List<String>>

    suspend fun alternarFavorito(uid: String, propiedadId: String)
}