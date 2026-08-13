// Modelo de dominio del usuario, complementa lo que ya maneja Firebase Auth.
package com.microsol.casaya.domain.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val foto: String = "",
    val rol: RolUsuario = RolUsuario.CLIENTE
)