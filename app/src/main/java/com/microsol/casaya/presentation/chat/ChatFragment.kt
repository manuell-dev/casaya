// Fragment de la pantalla de Chat: entrada de texto y lista de mensajes.
package com.microsol.casaya.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.microsol.casaya.data.repository.FirestoreChatRepositoryImpl
import com.microsol.casaya.databinding.FragmentChatBinding
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(FirestoreChatRepositoryImpl())
    }

    private lateinit var adapter: MensajeAdapter
    private var conversacionId: String = ""
    private var uid: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        conversacionId = requireArguments().getString("conversacionId").orEmpty()
        uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        binding.tvTituloChat.text = requireArguments().getString("propiedadTitulo").orEmpty()

        adapter = MensajeAdapter(uid)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvMensajes.layoutManager = layoutManager
        binding.rvMensajes.adapter = adapter

        binding.btnEnviar.setOnClickListener {
            val texto = binding.etMensaje.text.toString()
            viewModel.enviarMensaje(conversacionId, uid, texto)
            binding.etMensaje.text?.clear()
        }

        viewModel.cargarMensajes(conversacionId)
        observarEstado()
    }

    // Además de refrescar la lista, baja el scroll al último mensaje cuando llega uno nuevo.
    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    adapter.submitList(state.mensajes) {
                        if (state.mensajes.isNotEmpty()) {
                            binding.rvMensajes.scrollToPosition(state.mensajes.size - 1)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}