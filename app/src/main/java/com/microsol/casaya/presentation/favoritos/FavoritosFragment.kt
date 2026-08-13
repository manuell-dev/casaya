// Fragment de la pantalla de Favoritos: lista de propiedades guardadas.
package com.microsol.casaya.presentation.favoritos

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
import com.microsol.casaya.data.repository.FirestoreFavoritoRepositoryImpl
import com.microsol.casaya.data.repository.FirestorePropiedadRepositoryImpl
import com.microsol.casaya.databinding.FragmentFavoritosBinding
import com.microsol.casaya.presentation.home.PropiedadAdapter
import kotlinx.coroutines.launch

class FavoritosFragment : Fragment() {

    private var _binding: FragmentFavoritosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritosViewModel by viewModels {
        FavoritosViewModelFactory(FirestoreFavoritoRepositoryImpl(), FirestorePropiedadRepositoryImpl())
    }

    private lateinit var adapter: PropiedadAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PropiedadAdapter(onClickPropiedad = { propiedad ->
            val bundle = Bundle().apply { putString("propiedadId", propiedad.id) }
            findNavController().navigate(R.id.action_favoritosFragment_to_detalleFragment, bundle)
        })
        binding.rvFavoritos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavoritos.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        viewModel.cargarFavoritos(uid)

        binding.bottomNav.selectedItemId = R.id.menuFavoritos
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuInicio -> {
                    findNavController().popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.menuFavoritos -> true
                R.id.menuMensajes -> {
                    findNavController().navigate(R.id.action_favoritosFragment_to_conversacionesFragment)
                    true
                }
                R.id.menuPerfil -> {
                    findNavController().navigate(R.id.action_favoritosFragment_to_perfilFragment)
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
                    binding.progressBar.visibility = if (state.cargando) View.VISIBLE else View.GONE
                    binding.tvVacio.visibility = if (!state.cargando && state.propiedades.isEmpty()) View.VISIBLE else View.GONE
                    adapter.submitList(state.propiedades)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}