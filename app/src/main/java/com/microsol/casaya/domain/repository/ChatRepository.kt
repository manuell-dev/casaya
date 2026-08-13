// Contrato del repositorio de Chat: conversaciones y mensajes.
package com.microsol.casaya.domain.repository

import com.microsol.casaya.domain.model.Conversacion
import com.microsol.casaya.domain.model.Mensaje
import com.microsol.casaya.domain.model.Propiedad
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun obtenerOCrearConversacion(propiedad: Propiedad, compradorId: String): String

    // Incluye las conversaciones donde el usuario es comprador o propietario.
    fun obtenerConversaciones(uid: String): Flow<List<Conversacion>>

    fun obtenerMensajes(conversacionId: String): Flow<List<Mensaje>>

    suspend fun enviarMensaje(conversacionId: String, remitenteId: String, texto: String)
}