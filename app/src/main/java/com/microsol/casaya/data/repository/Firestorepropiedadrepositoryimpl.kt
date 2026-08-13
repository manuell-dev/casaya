// Repository de Propiedades que usa Firestore, con filtros, orden y paginación.
package com.microsol.casaya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.microsol.casaya.data.mapper.toDomain
import com.microsol.casaya.data.mapper.toDto
import com.microsol.casaya.data.model.PropiedadDto
import com.microsol.casaya.domain.model.FiltroPropiedad
import com.microsol.casaya.domain.model.Propiedad
import com.microsol.casaya.domain.repository.PropiedadRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestorePropiedadRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PropiedadRepository {

    private val coleccion = db.collection("propiedades")

    private fun aplicarFiltros(filtro: FiltroPropiedad): Query {
        var query: Query = coleccion
        if (filtro.tipo != null) {
            query = query.whereEqualTo("tipo", filtro.tipo.name)
        }
        if (filtro.precioMin != null) {
            query = query.whereGreaterThanOrEqualTo("precio", filtro.precioMin)
        }
        if (filtro.precioMax != null) {
            query = query.whereLessThanOrEqualTo("precio", filtro.precioMax)
        }
        return query
    }

    // Firestore exige que el primer orderBy sea igual al campo del filtro de rango.
    private fun hayFiltroDePrecio(filtro: FiltroPropiedad) = filtro.precioMin != null || filtro.precioMax != null

    // Escucha en vivo: cualquier cambio en Firestore reemite la lista completa.
    override fun obtenerPropiedades(filtro: FiltroPropiedad): Flow<List<Propiedad>> = callbackFlow {
        var query = aplicarFiltros(filtro)
        query = if (hayFiltroDePrecio(filtro)) {
            query.orderBy("precio", Query.Direction.DESCENDING)
        } else {
            query.orderBy("fechaPublicacion", Query.Direction.DESCENDING)
        }
        query = query.limit(5)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val lista = snapshot?.toObjects(PropiedadDto::class.java)?.map { it.toDomain() } ?: emptyList()
            trySend(lista)
        }

        awaitClose { listener.remove() }
    }

    // Paginación puntual que continúa después de la última propiedad, usando el mismo orden como cursor.
    override suspend fun obtenerMasPropiedades(
        filtro: FiltroPropiedad,
        ultimaPropiedad: Propiedad,
        tamanoPagina: Int
    ): List<Propiedad> {
        var query = aplicarFiltros(filtro)
        query = if (hayFiltroDePrecio(filtro)) {
            query.orderBy("precio", Query.Direction.DESCENDING).startAfter(ultimaPropiedad.precio)
        } else {
            query.orderBy("fechaPublicacion", Query.Direction.DESCENDING).startAfter(ultimaPropiedad.fechaPublicacion)
        }
        query = query.limit(tamanoPagina.toLong())

        val snapshot = query.get().await()
        return snapshot.toObjects(PropiedadDto::class.java).map { it.toDomain() }
    }

    override suspend fun obtenerPropiedadPorId(id: String): Propiedad? {
        val documento = coleccion.document(id).get().await()
        return documento.toObject(PropiedadDto::class.java)?.toDomain()
    }

    override suspend fun obtenerPropiedadesDeUsuario(uid: String): List<Propiedad> {
        val snapshot = coleccion.whereEqualTo("idUsuarioPublicador", uid).get().await()
        return snapshot.toObjects(PropiedadDto::class.java).map { it.toDomain() }
    }

    override suspend fun publicarPropiedad(propiedad: Propiedad) {
        val documento = coleccion.document()
        val propiedadConId = propiedad.copy(id = documento.id)
        documento.set(propiedadConId.toDto()).await()
    }

    override suspend fun actualizarPropiedad(propiedad: Propiedad) {
        coleccion.document(propiedad.id).set(propiedad.toDto()).await()
    }

    override suspend fun eliminarPropiedad(id: String) {
        coleccion.document(id).delete().await()
    }
}