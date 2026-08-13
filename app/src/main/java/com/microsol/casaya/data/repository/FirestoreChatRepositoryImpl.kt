// Repository de Chat: administra conversaciones y mensajes en Firestore.
package com.microsol.casaya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.microsol.casaya.domain.model.Conversacion
import com.microsol.casaya.domain.model.Mensaje
import com.microsol.casaya.domain.model.Propiedad
import com.microsol.casaya.domain.repository.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

class FirestoreChatRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ChatRepository {

    private val conversaciones = db.collection("conversaciones")
    private val mensajes = db.collection("mensajes")

    // ID predecible (propiedad + comprador) para no tener que consultar antes de crear.
    override suspend fun obtenerOCrearConversacion(propiedad: Propiedad, compradorId: String): String {
        val conversacionId = "${propiedad.id}_$compradorId"
        val documento = conversaciones.document(conversacionId).get().await()
        if (!documento.exists()) {
            val nueva = Conversacion(
                id = conversacionId,
                compradorId = compradorId,
                propietarioId = propiedad.idUsuarioPublicador,
                propiedadId = propiedad.id,
                propiedadTitulo = propiedad.titulo
            )
            conversaciones.document(conversacionId).set(nueva).await()
        }
        return conversacionId
    }

    // Firestore no soporta "OR" en queries, así que se combinan dos consultas en vivo.
    override fun obtenerConversaciones(uid: String): Flow<List<Conversacion>> {
        val comoComprador = escucharConversaciones("compradorId", uid)
        val comoPropietario = escucharConversaciones("propietarioId", uid)
        return comoComprador.combine(comoPropietario) { a, b ->
            (a + b).sortedByDescending { it.fechaUltimoMensaje }
        }
    }

    private fun escucharConversaciones(campo: String, uid: String): Flow<List<Conversacion>> = callbackFlow {
        val listener = conversaciones.whereEqualTo(campo, uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(Conversacion::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    override fun obtenerMensajes(conversacionId: String): Flow<List<Mensaje>> = callbackFlow {
        val listener = mensajes
            .whereEqualTo("conversacionId", conversacionId)
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Mensaje::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun enviarMensaje(conversacionId: String, remitenteId: String, texto: String) {
        val documento = mensajes.document()
        val mensaje = Mensaje(
            id = documento.id,
            conversacionId = conversacionId,
            remitenteId = remitenteId,
            texto = texto,
            fecha = System.currentTimeMillis()
        )
        documento.set(mensaje).await()

        conversaciones.document(conversacionId).update(
            mapOf("ultimoMensaje" to texto, "fechaUltimoMensaje" to mensaje.fecha)
        ).await()
    }
}