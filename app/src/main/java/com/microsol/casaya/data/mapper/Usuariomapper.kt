// Mappers entre UsuarioDto (Firestore) y Usuario (dominio).
package com.microsol.casaya.data.mapper

import com.microsol.casaya.data.model.UsuarioDto
import com.microsol.casaya.domain.model.RolUsuario
import com.microsol.casaya.domain.model.Usuario

fun UsuarioDto.toDomain(): Usuario {
    return Usuario(
        uid = uid,
        nombre = nombre,
        correo = correo,
        foto = foto,
        rol = RolUsuario.valueOf(rol)
    )
}

fun Usuario.toDto(): UsuarioDto {
    return UsuarioDto(
        uid = uid,
        nombre = nombre,
        correo = correo,
        foto = foto,
        rol = rol.name
    )
}