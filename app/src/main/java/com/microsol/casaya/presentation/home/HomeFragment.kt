





// Fragment de Home: lista de propiedades con búsqueda, filtro y paginación.
package com.microsol.casaya.presentation.home

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.microsol.casaya.data.repository.FirestorePropiedadRepositoryImpl
import com.microsol.casaya.data.repository.FirestoreUsuarioRepositoryImpl
import com.microsol.casaya.databinding.FragmentHomeBinding
import com.microsol.casaya.domain.model.FiltroPropiedad
import com.microsol.casaya.domain.model.Propiedad
import com.microsol.casaya.domain.model.TipoPropiedad
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(FirestorePropiedadRepositoryImpl(), FirestoreUsuarioRepositoryImpl())
    }

    private lateinit var adapter: PropiedadAdapter

    // Cache local de la última lista de Firestore, para filtrar por texto sin repetir la consulta.
    private var propiedadesActuales: List<Propiedad> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PropiedadAdapter(onClickPropiedad = { propiedad ->
            val bundle = Bundle().apply { putString("propiedadId", propiedad.id) }
            findNavController().navigate(R.id.action_homeFragment_to_detalleFragment, bundle)
        })
        binding.rvPropiedades.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPropiedades.adapter = adapter

        binding.rvPropiedades.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val ultimoVisible = layoutManager.findLastVisibleItemPosition()
                val totalItems = layoutManager.itemCount
                if (dy > 0 && ultimoVisible >= totalItems - 3) {
                    viewModel.cargarMas()
                }
            }
        })

        // Se fija explícito para que no quede seleccionado el ícono de la pantalla anterior.
        binding.bottomNav.selectedItemId = R.id.menuInicio

        binding.btnFiltros.setOnClickListener { mostrarDialogoFiltro() }

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrarPorTexto(s?.toString().orEmpty())
            }
        })

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuInicio -> true
                R.id.menuPublicar -> {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                    viewModel.puedePublicar(uid) { puede ->
                        if (puede) {
                            findNavController().navigate(R.id.action_homeFragment_to_publicarFragment)
                        } else {
                            android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Todavía eres cliente")
                                .setMessage("Para publicar una propiedad primero debes convertirte en propietario desde tu perfil.")
                                .setPositiveButton("Ir a mi perfil") { _, _ ->
                                    findNavController().navigate(R.id.action_homeFragment_to_perfilFragment)
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }
                    }
                    // false: la validación es asíncrona, así que aún no sabemos si vamos a navegar.
                    false
                }
                R.id.menuPerfil -> {
                    findNavController().navigate(R.id.action_homeFragment_to_perfilFragment)
                    true
                }
                R.id.menuFavoritos -> {
                    findNavController().navigate(R.id.action_homeFragment_to_favoritosFragment)
                    true
                }
                R.id.menuMensajes -> {
                    findNavController().navigate(R.id.action_homeFragment_to_conversacionesFragment)
                    true
                }
                else -> false
            }
        }

        observarEstado()
    }

    private fun observarEstado() {
        // Los datos ya llegan en vivo con el Flow; esto solo reinicia la escucha por sensación de refresco.
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.aplicarFiltro(viewModel.state.value.filtro)
        }

        binding.btnCargarMas.setOnClickListener { viewModel.cargarMas() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.visibility = if (state.cargando) View.VISIBLE else View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    propiedadesActuales = state.propiedades + state.propiedadesAdicionales
                    filtrarPorTexto(binding.etBuscar.text?.toString().orEmpty())

                    binding.btnCargarMas.visibility =
                        if (state.hayMasPaginas && !state.cargando) View.VISIBLE else View.GONE
                    binding.btnCargarMas.text = if (state.cargandoMas) "Cargando..." else "Cargar más"
                    binding.btnCargarMas.isEnabled = !state.cargandoMas
                }
            }
        }
    }

    // Filtra en memoria por título/zona (sin distinguir mayúsculas); evita una consulta a Firestore por cada letra.
    private fun filtrarPorTexto(texto: String) {
        val lista = if (texto.isBlank()) {
            propiedadesActuales
        } else {
            propiedadesActuales.filter {
                it.titulo.contains(texto, ignoreCase = true) || it.zona.contains(texto, ignoreCase = true)
            }
        }
        adapter.submitList(lista)
        binding.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarDialogoFiltro() {
        val vistaDialogo = layoutInflater.inflate(R.layout.dialog_filtro, null)
        val spinnerTipo = vistaDialogo.findViewById<android.widget.Spinner>(R.id.spinnerFiltroTipo)
        val etPrecioMin = vistaDialogo.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etFiltroPrecioMin)
        val etPrecioMax = vistaDialogo.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etFiltroPrecioMax)

        val opciones = arrayOf("Todos", "Casa", "Departamento", "Terreno")
        spinnerTipo.adapter = android.widget.ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, opciones
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar propiedades")
            .setView(vistaDialogo)
            .setPositiveButton("Aplicar") { _, _ ->
                val tipo = when (spinnerTipo.selectedItemPosition) {
                    1 -> TipoPropiedad.CASA
                    2 -> TipoPropiedad.DEPARTAMENTO
                    3 -> TipoPropiedad.TERRENO
                    else -> null
                }
                val precioMin = etPrecioMin.text.toString().toDoubleOrNull()
                val precioMax = etPrecioMax.text.toString().toDoubleOrNull()
                viewModel.aplicarFiltro(FiltroPropiedad(tipo = tipo, precioMin = precioMin, precioMax = precioMax))
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}