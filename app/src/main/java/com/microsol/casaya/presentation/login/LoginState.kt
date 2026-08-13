package com.microsol.casaya.presentation.login

// Representa todo lo que la pantalla necesita saber para dibujarse.
// modoRegistro decide si se muestra el formulario de login o el de registro.
data class LoginState(
    val modoRegistro: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val mensajeInfo: String? = null,
    val loginExitoso: Boolean = false
)