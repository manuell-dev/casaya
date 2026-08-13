package com.microsol.casaya.presentation.publicar

import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.microsol.casaya.data.repository.FirestorePropiedadRepositoryImpl
import com.microsol.casaya.databinding.FragmentPublicarBinding
import com.microsol.casaya.domain.model.Operacion
import com.microsol.casaya.domain.model.TipoPropiedad
import kotlinx.coroutines.launch

class PublicarFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPublicarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PublicarViewModel by viewModels {
        PublicarViewModelFactory(FirestorePropiedadRepositoryImpl())
    }

    private var googleMap: GoogleMap? = null
    private var fotoSeleccionada: Uri? = null
    private var yaPrecargado = false

    // Selector de fotos moderno de Android: no necesita pedir permisos de almacenamiento
    private val selectorDeFoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            fotoSeleccionada = uri
            binding.ivFotoSeleccionada.setImageURI(uri)
            binding.ivFotoSeleccionada.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublicarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.spinnerTipo.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, TipoPropiedad.values()
        )
        binding.spinnerOperacion.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, Operacion.values()
        )

        val mapFragment = childFragmentManager.findFragmentById(binding.mapaPublicar.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnBuscarDireccion.setOnClickListener { buscarDireccion() }
        binding.btnSubirFoto.setOnClickListener {
            selectorDeFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.btnPublicar.setOnClickListener { guardar() }

        // Si llegamos aquí desde "Editar" en Perfil, viene este argumento.
        // Si no viene (caso normal: publicar una propiedad nueva), no cambia nada.
        val idParaEditar = arguments?.getString("propiedadIdEditar")
        if (idParaEditar != null) {
            binding.btnPublicar.text = "Guardar cambios"
            viewModel.cargarPropiedadParaEditar(idParaEditar)
        }

        observarEstado()
    }

    override fun onMapReady(mapa: GoogleMap) {
        googleMap = mapa
        dibujarMarker(viewModel.state.value.lat, viewModel.state.value.lng)

        mapa.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: com.google.android.gms.maps.model.Marker) {}
            override fun onMarkerDrag(marker: com.google.android.gms.maps.model.Marker) {}
            override fun onMarkerDragEnd(marker: com.google.android.gms.maps.model.Marker) {
                viewModel.actualizarUbicacion(marker.position.latitude, marker.position.longitude)
                actualizarCampoDireccion(marker.position.latitude, marker.position.longitude)
            }
        })

        // Mantener presionado cualquier punto del mapa: mueve el marker ahí
        // y además rellena el campo de texto con la dirección encontrada
        mapa.setOnMapLongClickListener { latLng ->
            viewModel.actualizarUbicacion(latLng.latitude, latLng.longitude)
            dibujarMarker(latLng.latitude, latLng.longitude)
            actualizarCampoDireccion(latLng.latitude, latLng.longitude)
        }
    }

    // Geocodificación inversa: de coordenadas a texto de dirección (lo opuesto
    // a buscarDireccion(), que va de texto a coordenadas)
    private fun actualizarCampoDireccion(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(requireContext())
            val resultados = geocoder.getFromLocation(lat, lng, 1)
            val direccion = resultados?.firstOrNull()?.getAddressLine(0)
            if (direccion != null) {
                binding.etDireccion.setText(direccion)
            }
        } catch (e: Exception) {
            // Si falla la geocodificación inversa no es grave: el marker y las
            // coordenadas ya quedaron correctos, solo no se autocompletó el texto
        }
    }

    // Usa el Geocoder nativo de Android para convertir el texto de la dirección en coordenadas
    private fun buscarDireccion() {
        val direccion = binding.etDireccion.text.toString()
        if (direccion.isBlank()) return

        try {
            val geocoder = Geocoder(requireContext())
            val resultados = geocoder.getFromLocationName(direccion, 1)
            val lugar = resultados?.firstOrNull()

            if (lugar != null) {
                viewModel.actualizarUbicacion(lugar.latitude, lugar.longitude)
                dibujarMarker(lugar.latitude, lugar.longitude)
                binding.tvError.visibility = View.GONE
            } else {
                binding.tvError.text = "No se encontró esa dirección, prueba con otra"
                binding.tvError.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            // En el emulador a veces falla si no hay conexión al servicio de geocoding de Google
            binding.tvError.text = "No se pudo buscar la dirección: ${e.message}"
            binding.tvError.visibility = View.VISIBLE
        }
    }

    private fun dibujarMarker(lat: Double, lng: Double) {
        val mapa = googleMap ?: return
        val ubicacion = LatLng(lat, lng)
        mapa.clear()
        mapa.addMarker(MarkerOptions().position(ubicacion).draggable(true))
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f))
    }

    private fun guardar() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        viewModel.guardar(
            idUsuario = uid,
            titulo = binding.etTitulo.text.toString(),
            descripcion = binding.etDescripcion.text.toString(),
            tipo = binding.spinnerTipo.selectedItem as TipoPropiedad,
            operacion = binding.spinnerOperacion.selectedItem as Operacion,
            precio = binding.etPrecio.text.toString().toDoubleOrNull() ?: 0.0,
            zona = binding.etZona.text.toString(),
            habitaciones = binding.etHabitaciones.text.toString().toIntOrNull() ?: 0,
            banos = binding.etBanos.text.toString().toIntOrNull() ?: 0,
            areaM2 = binding.etAreaM2.text.toString().toDoubleOrNull() ?: 0.0,
            fotoUri = fotoSeleccionada
        )
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.visibility = if (state.cargando) View.VISIBLE else View.GONE
                    if (state.error != null) {
                        binding.tvError.text = state.error
                        binding.tvError.visibility = View.VISIBLE
                    }
                    if (state.publicadoExitoso) {
                        val mensaje = when (state.modo) {
                            is ModoPublicar.Crear -> "✅ Propiedad publicada"
                            is ModoPublicar.Editar -> "✅ Cambios guardados"
                        }
                        android.widget.Toast.makeText(requireContext(), mensaje, android.widget.Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    when (val modo = state.modo) {
                        is ModoPublicar.Crear -> Unit
                        is ModoPublicar.Editar -> if (!yaPrecargado) {
                            yaPrecargado = true
                            precargarCampos(modo.propiedad)
                            dibujarMarker(state.lat, state.lng)
                        }
                    }
                }
            }
        }
    }

    // Llena el formulario con los datos de una propiedad ya publicada, para editarla
    private fun precargarCampos(propiedad: com.microsol.casaya.domain.model.Propiedad) {
        binding.etTitulo.setText(propiedad.titulo)
        binding.etDescripcion.setText(propiedad.descripcion)
        binding.etPrecio.setText(propiedad.precio.toString())
        binding.etZona.setText(propiedad.zona)
        binding.etHabitaciones.setText(propiedad.habitaciones.toString())
        binding.etBanos.setText(propiedad.banos.toString())
        binding.etAreaM2.setText(propiedad.areaM2.toString())

        val posicionTipo = TipoPropiedad.values().indexOf(propiedad.tipo)
        if (posicionTipo >= 0) binding.spinnerTipo.setSelection(posicionTipo)
        val posicionOperacion = Operacion.values().indexOf(propiedad.operacion)
        if (posicionOperacion >= 0) binding.spinnerOperacion.setSelection(posicionOperacion)

        val fotoExistente = propiedad.fotos.firstOrNull()
        if (fotoExistente != null) {
            binding.ivFotoSeleccionada.load(fotoExistente)
            binding.ivFotoSeleccionada.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}