package com.microsol.casaya.presentation.perfil

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.microsol.casaya.R
import com.microsol.casaya.data.repository.FirestoreFavoritoRepositoryImpl
import com.microsol.casaya.data.repository.FirestorePropiedadRepositoryImpl
import com.microsol.casaya.data.repository.FirestoreUsuarioRepositoryImpl
import com.microsol.casaya.databinding.FragmentPerfilBinding
import com.microsol.casaya.domain.model.RolUsuario
import com.microsol.casaya.presentation.home.PropiedadAdapter
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PerfilViewModel by viewModels {
        PerfilViewModelFactory(
            FirestoreUsuarioRepositoryImpl(),
            FirestorePropiedadRepositoryImpl(),
            FirestoreFavoritoRepositoryImpl()
        )
    }

    private lateinit var adapter: PropiedadAdapter
    private var fotoPerfilSeleccionada: Uri? = null
    private var ivFotoDialogoActual: android.widget.ImageView? = null

    // Selector de fotos moderno de Android, igual al de Publicar
    private val selectorDeFotoPerfil = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            fotoPerfilSeleccionada = uri
            ivFotoDialogoActual?.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        adapter = PropiedadAdapter(
            onClickPropiedad = { propiedad ->
                val bundle = Bundle().apply { putString("propiedadId", propiedad.id) }
                findNavController().navigate(R.id.action_perfilFragment_to_detalleFragment, bundle)
            },
            onLongClickPropiedad = { propiedad -> mostrarOpcionesPublicacion(propiedad.id, uid) }
        )
        binding.rvMisPublicaciones.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMisPublicaciones.adapter = adapter

        viewModel.cargarPerfil(uid)

        binding.btnConvertirsePropietario.setOnClickListener {
            viewModel.convertirEnPropietario(uid)
        }

        binding.btnEditarPerfil.setOnClickListener { mostrarDialogoEditarPerfil(uid) }

        binding.btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // popUpTo con inclusive=true en el grafo raíz: borra TODA la pila de
            // navegación (Home, Detalle, Perfil, etc.), así al presionar "atrás"
            // desde Login no se puede volver a las pantallas de un usuario ya deslogueado
            val opciones = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.loginFragment, null, opciones)
        }

        binding.bottomNav.selectedItemId = R.id.menuPerfil
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuInicio -> {
                    findNavController().popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.menuPublicar -> {
                    val esPropietario = viewModel.state.value.usuario?.rol == RolUsuario.PROPIETARIO
                    if (esPropietario) {
                        findNavController().navigate(R.id.action_perfilFragment_to_publicarFragment)
                    } else {
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Todavía eres cliente")
                            .setMessage("Para publicar una propiedad primero debes convertirte en propietario.")
                            .setPositiveButton("Entendido", null)
                            .show()
                    }
                    // false: si no es propietario nos quedamos en Perfil, y el ícono
                    // de Perfil debe seguir marcado, no el de Publicar
                    false
                }
                R.id.menuPerfil -> true
                R.id.menuFavoritos -> {
                    findNavController().navigate(R.id.action_perfilFragment_to_favoritosFragment)
                    true
                }
                R.id.menuMensajes -> {
                    findNavController().navigate(R.id.action_perfilFragment_to_conversacionesFragment)
                    true
                }
                else -> false
            }
        }

        observarEstado()
    }

    private fun mostrarDialogoEditarPerfil(uid: String) {
        fotoPerfilSeleccionada = null
        val vistaDialogo = layoutInflater.inflate(R.layout.dialog_editar_perfil, null)
        val ivFoto = vistaDialogo.findViewById<android.widget.ImageView>(R.id.ivFotoDialogo)
        val tvCambiarFoto = vistaDialogo.findViewById<android.widget.TextView>(R.id.tvCambiarFoto)
        val etNombre = vistaDialogo.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNombreDialogo)

        ivFotoDialogoActual = ivFoto
        val usuarioActual = viewModel.state.value.usuario
        etNombre.setText(usuarioActual?.nombre.orEmpty())
        if (!usuarioActual?.foto.isNullOrBlank()) {
            ivFoto.load(usuarioActual?.foto)
        }

        tvCambiarFoto.setOnClickListener {
            selectorDeFotoPerfil.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Editar perfil")
            .setView(vistaDialogo)
            .setPositiveButton("Guardar") { _, _ ->
                val nombreNuevo = etNombre.text.toString()
                viewModel.actualizarPerfil(uid, nombreNuevo, fotoPerfilSeleccionada)
                ivFotoDialogoActual = null
            }
            .setNegativeButton("Cancelar") { _, _ -> ivFotoDialogoActual = null }
            .show()
    }

    // Se llama al mantener presionada una propiedad en "Mis publicaciones"
    private fun mostrarOpcionesPublicacion(propiedadId: String, uid: String) {
        val opciones = arrayOf("Editar", "Eliminar")
        android.app.AlertDialog.Builder(requireContext())
            .setItems(opciones) { _, cual ->
                when (cual) {
                    0 -> {
                        val bundle = Bundle().apply { putString("propiedadIdEditar", propiedadId) }
                        findNavController().navigate(R.id.action_perfilFragment_to_publicarFragment, bundle)
                    }
                    1 -> confirmarEliminar(propiedadId, uid)
                }
            }
            .show()
    }

    private fun confirmarEliminar(propiedadId: String, uid: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar propiedad")
            .setMessage("Esta acción no se puede deshacer. ¿Seguro que quieres eliminarla?")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminarPropiedad(uid, propiedadId) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> actualizarUi(state) }
            }
        }
    }

    private fun actualizarUi(state: PerfilState) {
        val usuario = state.usuario ?: return

        binding.tvNombre.text = usuario.nombre.ifBlank { "Sin nombre" }
        binding.tvCorreo.text = usuario.correo
        binding.tvIniciales.text = usuario.nombre.take(2).uppercase()
        binding.tvFavoritos.text = "❤ ${state.cantidadFavoritos} propiedades favoritas"

        if (usuario.foto.isNotBlank()) {
            binding.ivFotoPerfil.load(usuario.foto)
            binding.ivFotoPerfil.visibility = View.VISIBLE
            binding.tvIniciales.visibility = View.GONE
        } else {
            binding.ivFotoPerfil.visibility = View.GONE
            binding.tvIniciales.visibility = View.VISIBLE
        }

        val esPropietario = usuario.rol == RolUsuario.PROPIETARIO
        binding.btnConvertirsePropietario.visibility = if (esPropietario) View.GONE else View.VISIBLE
        binding.layoutMisPublicaciones.visibility = if (esPropietario) View.VISIBLE else View.GONE

        adapter.submitList(state.misPublicaciones)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}