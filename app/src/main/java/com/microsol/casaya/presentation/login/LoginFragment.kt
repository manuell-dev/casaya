package com.microsol.casaya.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.microsol.casaya.R
import com.microsol.casaya.data.repository.FirestoreUsuarioRepositoryImpl
import com.microsol.casaya.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Factory manual: le pasamos la implementación real del repositorio
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(FirestoreUsuarioRepositoryImpl())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabIniciarSesion.setOnClickListener {
            limpiarCampos()
            viewModel.cambiarModo(modoRegistro = false)
        }
        binding.tabRegistrarme.setOnClickListener {
            limpiarCampos()
            viewModel.cambiarModo(modoRegistro = true)
        }

        binding.btnIniciarSesion.setOnClickListener {
            viewModel.iniciarSesion(
                binding.etCorreoLogin.text.toString(),
                binding.etContrasenaLogin.text.toString()
            )
        }

        binding.tvOlvideContrasena.setOnClickListener {
            viewModel.recuperarContrasena(binding.etCorreoLogin.text.toString())
        }

        binding.btnRegistrarse.setOnClickListener {
            viewModel.registrarse(
                binding.etNombreRegistro.text.toString(),
                binding.etCorreoRegistro.text.toString(),
                binding.etContrasenaRegistro.text.toString()
            )
        }

        binding.btnGoogle.setOnClickListener { iniciarSesionConGoogle() }

        observarEstado()
    }

    // Vacía los dos formularios cada vez que se cambia de pestaña,
    // así no queda texto viejo escondido del otro modo
    private fun limpiarCampos() {
        binding.etCorreoLogin.text?.clear()
        binding.etContrasenaLogin.text?.clear()
        binding.etNombreRegistro.text?.clear()
        binding.etCorreoRegistro.text?.clear()
        binding.etContrasenaRegistro.text?.clear()
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> actualizarUi(state) }
            }
        }
    }

    private fun actualizarUi(state: LoginState) {
        // Muestra el formulario correcto según la pestaña activa
        binding.layoutFormLogin.visibility = if (state.modoRegistro) View.GONE else View.VISIBLE
        binding.layoutFormRegistro.visibility = if (state.modoRegistro) View.VISIBLE else View.GONE
        binding.tabIniciarSesion.isSelected = !state.modoRegistro
        binding.tabRegistrarme.isSelected = state.modoRegistro

        binding.progressBar.visibility = if (state.cargando) View.VISIBLE else View.GONE

        // Evita que el usuario toque los botones dos veces mientras se procesa la petición
        binding.btnIniciarSesion.isEnabled = !state.cargando
        binding.btnRegistrarse.isEnabled = !state.cargando
        binding.btnGoogle.isEnabled = !state.cargando

        if (state.error != null) {
            binding.tvError.text = state.error
            binding.tvError.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
            binding.tvError.visibility = View.VISIBLE
        } else if (state.mensajeInfo != null) {
            binding.tvError.text = state.mensajeInfo
            binding.tvError.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            binding.tvError.visibility = View.VISIBLE
        } else {
            binding.tvError.visibility = View.GONE
        }

        if (state.loginExitoso) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

    // Flujo con Credential Manager, igual al usado en AppAutenticacionFirebase
    private fun iniciarSesionConGoogle() {
        val opcionGoogle = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val credentialManager = CredentialManager.create(requireContext())
                val resultado = credentialManager.getCredential(requireContext(), request)
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(resultado.credential.data)
                viewModel.iniciarSesionConGoogle(googleIdTokenCredential.idToken)
            } catch (e: GetCredentialCancellationException) {
                // El usuario cerró la ventana de selección de cuenta, no es un error real
                binding.tvError.visibility = View.GONE
            } catch (e: NoCredentialException) {
                binding.tvError.text = "No hay ninguna cuenta de Google configurada en este dispositivo"
                binding.tvError.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.tvError.text = "No se pudo iniciar sesión con Google, inténtalo de nuevo"
                binding.tvError.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}