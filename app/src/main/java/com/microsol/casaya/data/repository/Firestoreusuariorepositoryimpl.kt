// Repository de Usuario que usa Firestore para leer y actualizar el perfil.
package com.microsol.casaya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.microsol.casaya.data.mapper.toDomain
import com.microsol.casaya.data.mapper.toDto
import com.microsol.casaya.data.model.UsuarioDto
import com.microsol.casaya.domain.model.RolUsuario
import com.microsol.casaya.domain.model.Usuario
import com.microsol.casaya.domain.repository.UsuarioRepository
import kotlinx.coroutines.tasks.await

class FirestoreUsuarioRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UsuarioRepository {

    private val coleccion = db.collection("usuarios")

    override suspend fun crearUsuarioSiNoExiste(usuario: Usuario) {
        val documento = coleccion.document(usuario.uid).get().await()
        if (!documento.exists()) {
            coleccion.document(usuario.uid).set(usuario.toDto()).await()
        }
    }

    override suspend fun obtenerUsuario(uid: String): Usuario? {
        val documento = coleccion.document(uid).get().await()
        return documento.toObject(UsuarioDto::class.java)?.toDomain()
    }

    override suspend fun convertirEnPropietario(uid: String) {
        coleccion.document(uid).update("rol", RolUsuario.PROPIETARIO.name).await()
    }

    override suspend fun guardarTokenNotificacion(uid: String, token: String) {
        coleccion.document(uid).update("fcmToken", token).await()
    }

    override suspend fun actualizarPerfil(uid: String, nombre: String, foto: String?) {
        val cambios = mutableMapOf<String, Any>("nombre" to nombre)
        if (foto != null) cambios["foto"] = foto
        coleccion.document(uid).update(cambios).await()
    }
}