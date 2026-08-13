// Fragment de la pantalla de Conversaciones: lista de chats del usuario.
package com.microsol.casaya.presentation.conversaciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.microsol.casaya.R
import com.microsol.casaya.data.repository.FirestoreChatRepositoryImpl
import com.microsol.casaya.databinding.FragmentConversacionesBinding
import kotlinx.coroutines.launch

class ConversacionesFragment : Fragment() {

    private var _binding: FragmentConversacionesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConversacionesViewModel by viewModels {
        ConversacionesViewModelFactory(FirestoreChatRepositoryImpl())
    }

    private lateinit var adapter: ConversacionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversacionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ConversacionAdapter(onClick = { conversacion ->
            val bundle = Bundle().apply {
                putString("conversacionId", conversacion.id)
                putString("propiedadTitulo", conversacion.propiedadTitulo)
            }
            findNavController().navigate(R.id.action_conversacionesFragment_to_chatFragment, bundle)
        })
        binding.rvConversaciones.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConversaciones.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        viewModel.cargarConversaciones(uid)

        binding.bottomNav.selectedItemId = R.id.menuMensajes
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuInicio -> {
                    findNavController().popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.menuMensajes -> true
                R.id.menuFavoritos -> {
                    findNavController().navigate(R.id.action_conversacionesFragment_to_favoritosFragment)
                    true
                }
                R.id.menuPerfil -> {
                    findNavController().navigate(R.id.action_conversacionesFragment_to_perfilFragment)
                    true
                }
                else -> false
            }
        }

        observarEstado()
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.tvVacio.visibility =
                        if (!state.cargando && state.conversaciones.isEmpty()) View.VISIBLE else View.GONE
                    adapter.submitList(state.conversaciones)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}