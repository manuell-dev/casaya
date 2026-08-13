package com.microsol.casaya.presentation.publicar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.microsol.casaya.domain.repository.PropiedadRepository
import com.microsol.casaya.domain.usecase.ActualizarPropiedadUseCase
import com.microsol.casaya.domain.usecase.PublicarPropiedadUseCase

class PublicarViewModelFactory(
    private val propiedadRepository: PropiedadRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PublicarViewModel(
            propiedadRepository = propiedadRepository,
            publicarPropiedadUseCase = PublicarPropiedadUseCase(propiedadRepository),
            actualizarPropiedadUseCase = ActualizarPropiedadUseCase(propiedadRepository)
        ) as T
    }
}