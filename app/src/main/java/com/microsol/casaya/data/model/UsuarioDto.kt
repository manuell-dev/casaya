// DTO de un usuario tal como se guarda en Firestore.
package com.microsol.casaya.data.model

data class UsuarioDto(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val foto: String = "",
    val rol: String = "CLIENTE"
)