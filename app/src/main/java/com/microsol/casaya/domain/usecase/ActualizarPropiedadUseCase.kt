// Caso de uso: actualiza una propiedad existente.
package com.microsol.casaya.domain.usecase

import com.microsol.casaya.domain.model.Propiedad
import com.microsol.casaya.domain.repository.PropiedadRepository

class ActualizarPropiedadUseCase(
    private val propiedadRepository: PropiedadRepository
) {
    suspend operator fun invoke(propiedad: Propiedad) {
        propiedadRepository.actualizarPropiedad(propiedad)
    }
}