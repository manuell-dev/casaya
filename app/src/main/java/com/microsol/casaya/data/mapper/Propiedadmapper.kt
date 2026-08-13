// Mappers entre PropiedadDto (Firestore) y Propiedad (dominio).
package com.microsol.casaya.data.mapper

import com.microsol.casaya.data.model.PropiedadDto
import com.microsol.casaya.domain.model.Operacion
import com.microsol.casaya.domain.model.Propiedad
import com.microsol.casaya.domain.model.TipoPropiedad

fun PropiedadDto.toDomain(): Propiedad {
    return Propiedad(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        tipo = TipoPropiedad.valueOf(tipo),
        operacion = Operacion.valueOf(operacion),
        precio = precio,
        zona = zona,
        lat = lat,
        lng = lng,
        habitaciones = habitaciones,
        banos = banos,
        areaM2 = areaM2,
        fotos = fotos,
        idUsuarioPublicador = idUsuarioPublicador,
        fechaPublicacion = fechaPublicacion
    )
}

fun Propiedad.toDto(): PropiedadDto {
    return PropiedadDto(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        tipo = tipo.name,
        operacion = operacion.name,
        precio = precio,
        zona = zona,
        lat = lat,
        lng = lng,
        habitaciones = habitaciones,
        banos = banos,
        areaM2 = areaM2,
        fotos = fotos,
        idUsuarioPublicador = idUsuarioPublicador,
        fechaPublicacion = fechaPublicacion
    )
}