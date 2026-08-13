// Fragment de la pantalla de Detalle: datos de la propiedad, mapa, favorito y contacto.
package com.microsol.casaya.presentation.detalle

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
import coil.load
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.microsol.casaya.R
import com.microsol.casaya.data.repository.FirestoreChatRepositoryImpl
import com.microsol.casaya.data.repository.FirestoreFavoritoRepositoryImpl
import com.microsol.casaya.data.repository.FirestorePropiedadRepositoryImpl
import com.microsol.casaya.databinding.FragmentDetalleBinding
import com.microsol.casaya.domain.model.Propiedad
import kotlinx.coroutines.launch

class DetalleFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetalleViewModel by viewModels {
        DetalleViewModelFactory(FirestorePropiedadRepositoryImpl(), FirestoreFavoritoRepositoryImpl())
    }

    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val propiedadId = requireArguments().getString("propiedadId").orEmpty()
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        viewModel.cargarPropiedad(propiedadId, uid)

        val mapFragment = childFragmentManager.findFragmentById(binding.mapaUbicacion.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnFavorito.setOnClickListener { viewModel.alternarFavorito() }
        binding.btnContactar.setOnClickListener { contactarPropietario() }
        binding.btnCompartir.setOnClickListener { compartirPropiedad() }

        observarEstado()
    }

    private val chatRepository = FirestoreChatRepositoryImpl()

    private fun contactarPropietario() {
        val propiedad = viewModel.state.value.propiedad ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        viewLifecycleOwner.lifecycleScope.launch {
            val conversacionId = chatRepository.obtenerOCrearConversacion(propiedad, uid)
            val bundle = android.os.Bundle().apply {
                putString("conversacionId", conversacionId)
                putString("propiedadTitulo", propiedad.titulo)
            }
            findNavController().navigate(R.id.action_detalleFragment_to_chatFragment, bundle)
        }
    }

    private fun compartirPropiedad() {
        val propiedad = viewModel.state.value.propiedad ?: return
        val texto = "Mira esta propiedad en CasaYa: ${propiedad.titulo} — " +
                "S/ ${"%,.0f".format(propiedad.precio)} en ${propiedad.zona}"
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, texto)
        }
        startActivity(android.content.Intent.createChooser(intent, "Compartir propiedad"))
    }

    // Mapa de solo lectura: gestos desactivados y sincronizado si la propiedad ya cargó antes.
    override fun onMapReady(mapa: GoogleMap) {
        googleMap = mapa
        mapa.uiSettings.setAllGesturesEnabled(false)
        viewModel.state.value.propiedad?.let { mostrarUbicacion(it) }
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.btnFavorito.text = if (state.esFavorito) "En favoritos" else "Favorito"
                    state.propiedad?.let { propiedad ->
                        mostrarDatos(propiedad)
                        mostrarUbicacion(propiedad)
                    }
                }
            }
        }
    }

    // Oculta favorito/contactar cuando la propiedad es del propio usuario.
    private fun mostrarDatos(propiedad: Propiedad) {
        binding.ivFotoPrincipal.load(propiedad.fotos.firstOrNull())
        binding.tvPrecio.text = "S/ ${"%,.0f".format(propiedad.precio)}"
        binding.tvOperacion.text = if (propiedad.operacion == com.microsol.casaya.domain.model.Operacion.VENTA) "VENTA" else "ALQUILER"
        binding.tvTitulo.text = propiedad.titulo
        binding.tvZona.text = "Ubicación: ${propiedad.zona}"
        binding.tvCaracteristicas.text =
            "${propiedad.habitaciones} dorm · ${propiedad.banos} baños · ${propiedad.areaM2} m²"
        binding.tvDescripcion.text = propiedad.descripcion

        val esPropia = propiedad.idUsuarioPublicador == FirebaseAuth.getInstance().currentUser?.uid
        binding.btnFavorito.visibility = if (esPropia) View.GONE else View.VISIBLE
        binding.btnContactar.visibility = if (esPropia) View.GONE else View.VISIBLE
    }

    private fun mostrarUbicacion(propiedad: Propiedad) {
        val mapa = googleMap ?: return
        val ubicacion = LatLng(propiedad.lat, propiedad.lng)
        mapa.clear()
        mapa.addMarker(MarkerOptions().position(ubicacion).title(propiedad.titulo))
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}