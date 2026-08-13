package com.microsol.casaya.presentation.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.microsol.casaya.domain.model.Usuario
import com.microsol.casaya.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    // Solo letras (con tildes/ñ) y espacios — nada de números ni símbolos
    private val regexNombre = Regex("^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ ]+$")

    // Al menos 8 caracteres, con mayúscula, minúscula, número y símbolo
    private val regexContrasenaSegura = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")

    fun cambiarModo(modoRegistro: Boolean) {
        _state.value = _state.value.copy(modoRegistro = modoRegistro, error = null)
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _state.value = _state.value.copy(error = "Completa correo y contraseña")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            try {
                auth.signInWithEmailAndPassword(correo, contrasena).await()
                onLoginExitoso()
            } catch (e: Exception) {
                _state.value = _state.value.copy(cargando = false, error = traducirError(e))
            }
        }
    }

    fun registrarse(nombre: String, correo: String, contrasena: String) {
        val errorValidacion = validarRegistro(nombre, correo, contrasena)
        if (errorValidacion != null) {
            _state.value = _state.value.copy(error = errorValidacion)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            try {
                auth.createUserWithEmailAndPassword(correo, contrasena).await()
                onLoginExitoso(nombreRegistro = nombre)
            } catch (e: Exception) {
                _state.value = _state.value.copy(cargando = false, error = traducirError(e))
            }
        }
    }

    // Revisa los 3 campos del registro y devuelve el primer error que encuentra,
    // o null si todo está bien
    private fun validarRegistro(nombre: String, correo: String, contrasena: String): String? {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            return "Completa todos los campos"
        }
        if (!regexNombre.matches(nombre)) {
            return "El nombre solo puede tener letras y espacios"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            return "Ingresa un correo válido (ejemplo@correo.com)"
        }
        if (!regexContrasenaSegura.matches(contrasena)) {
            return "La contraseña necesita mínimo 8 caracteres, con mayúscula, minúscula, número y símbolo"
        }
        return null
    }

    // idToken viene del Credential Manager (se obtiene en el Fragment)
    fun iniciarSesionConGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            try {
                val credencial = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credencial).await()
                onLoginExitoso()
            } catch (e: Exception) {
                _state.value = _state.value.copy(cargando = false, error = traducirError(e))
            }
        }
    }

    // Se llama después de cualquier login exitoso: crea el documento del
    // usuario en Firestore si es la primera vez que entra (rol CLIENTE por defecto)
    private suspend fun onLoginExitoso(nombreRegistro: String? = null) {
        val usuarioFirebase = auth.currentUser
        if (usuarioFirebase != null) {
            val usuario = Usuario(
                uid = usuarioFirebase.uid,
                nombre = nombreRegistro ?: usuarioFirebase.displayName.orEmpty(),
                correo = usuarioFirebase.email.orEmpty(),
                foto = usuarioFirebase.photoUrl?.toString().orEmpty()
            )
            usuarioRepository.crearUsuarioSiNoExiste(usuario)
        }
        _state.value = _state.value.copy(cargando = false, loginExitoso = true)
    }

    fun recuperarContrasena(correo: String) {
        if (correo.isBlank()) {
            _state.value = _state.value.copy(error = "Escribe tu correo para poder enviarte el enlace")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null, mensajeInfo = null)
            try {
                auth.sendPasswordResetEmail(correo).await()
                _state.value = _state.value.copy(
                    cargando = false,
                    mensajeInfo = "Te enviamos un enlace a $correo para restablecer tu contraseña"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(cargando = false, error = traducirError(e))
            }
        }
    }

    // Firebase devuelve los mensajes de error en inglés; los traducimos
    // según el tipo de excepción para que el usuario entienda qué pasó.
    private fun traducirError(e: Exception): String {
        return when (e) {
            is FirebaseAuthUserCollisionException -> "Ya existe una cuenta registrada con ese correo"
            is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil, revisa los requisitos"
            is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
            is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo"
            else -> "Ocurrió un error, inténtalo de nuevo"
        }
    }
}