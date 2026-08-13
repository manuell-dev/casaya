// Caso de uso: publica una nueva propiedad.
package com.microsol.casaya.domain.usecase

import com.microsol.casaya.domain.model.Propiedad
import com.microsol.casaya.domain.repository.PropiedadRepository

class PublicarPropiedadUseCase(
    private val propiedadRepository: PropiedadRepository
) {
    suspend operator fun invoke(propiedad: Propiedad) {
        propiedadRepository.publicarPropiedad(propiedad)
    }
}