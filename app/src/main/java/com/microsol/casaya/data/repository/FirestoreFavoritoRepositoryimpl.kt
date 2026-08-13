// Repository de Favoritos que usa Firestore para guardar y escuchar cambios.
package com.microsol.casaya.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.microsol.casaya.domain.repository.FavoritoRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreFavoritoRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FavoritoRepository {

    private val coleccion = db.collection("favoritos")

    override fun obtenerIdsFavoritos(uid: String): Flow<List<String>> = callbackFlow {
        val listener = coleccion.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            @Suppress("UNCHECKED_CAST")
            val ids = snapshot?.get("propiedadIds") as? List<String> ?: emptyList()
            trySend(ids)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun alternarFavorito(uid: String, propiedadId: String) {
        val documento = coleccion.document(uid)
        val actual = documento.get().await()

        @Suppress("UNCHECKED_CAST")
        val idsActuales = actual.get("propiedadIds") as? List<String> ?: emptyList()

        if (idsActuales.contains(propiedadId)) {
            documento.update("propiedadIds", FieldValue.arrayRemove(propiedadId)).await()
        } else {
            documento.set(mapOf("propiedadIds" to FieldValue.arrayUnion(propiedadId)), com.google.firebase.firestore.SetOptions.merge()).await()
        }
    }
}