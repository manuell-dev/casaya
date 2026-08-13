package com.microsol.casaya.presentation.publicar

import com.microsol.casaya.domain.model.Propiedad

sealed class ModoPublicar {
    object Crear : ModoPublicar()
    data class Editar(val propiedad: Propiedad) : ModoPublicar()
}