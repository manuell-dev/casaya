// Contrato de Usuario: acceso a datos y estado de rol.
package com.microsol.casaya.domain.repository

import com.microsol.casaya.domain.model.Usuario

interface UsuarioRepository {

    suspend fun crearUsuarioSiNoExiste(usuario: Usuario)

    suspend fun obtenerUsuario(uid: String): Usuario?

    suspend fun convertirEnPropietario(uid: String)

    suspend fun guardarTokenNotificacion(uid: String, token: String)

    suspend fun actualizarPerfil(uid: String, nombre: String, foto: String?)
}