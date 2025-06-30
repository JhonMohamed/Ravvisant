package com.proyect.ravvisant.features.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputEditText
import com.proyect.ravvisant.R
import com.proyect.ravvisant.core.utils.UbigeoPeru
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class LocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var spinnerDepartamento: Spinner
    private lateinit var spinnerDistrito: Spinner
    private lateinit var edtDireccion: TextInputEditText
    private lateinit var edtPhoneFull: TextInputEditText
    private lateinit var edtNotsAditionals: TextInputEditText
    private lateinit var btnGuardarUbicacion: Button

    private var googleMap: GoogleMap? = null
    private var selectedLatLng: LatLng? = null

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    googleMap?.isMyLocationEnabled = true
                } catch (e: SecurityException) {
                    Toast.makeText(requireContext(), "Error al habilitar la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_location, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerDepartamento = view.findViewById(R.id.spinnerDepartamento)
        spinnerDistrito = view.findViewById(R.id.spinnerDistrito)
        edtDireccion = view.findViewById(R.id.edtDireccion)
        edtPhoneFull = view.findViewById(R.id.edtPhoneFull)
        edtNotsAditionals = view.findViewById(R.id.edtNotsAditionals)
        btnGuardarUbicacion = view.findViewById(R.id.btnGuardarUbicacion)

        val depAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, UbigeoPeru.departamentos)
        depAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDepartamento.adapter = depAdapter

        spinnerDepartamento.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val departamentoSeleccionado = UbigeoPeru.departamentos[position]
                val distritos = UbigeoPeru.distritosPorDepartamento[departamentoSeleccionado] ?: listOf("Selecciona un distrito")
                val distAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, distritos)
                distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDistrito.adapter = distAdapter
                cargarDistritoDePrefsSiCorresponde()
                actualizarMapaSegunDireccion()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerDistrito.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                actualizarMapaSegunDireccion()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        edtDireccion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                actualizarMapaSegunDireccion()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        btnGuardarUbicacion.setOnClickListener {
            if (validarCampos()) {
                guardarCamposEnPrefs()
                Toast.makeText(requireContext(), "Ubicación guardada correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        cargarCamposDePrefs()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val lima = LatLng(-12.0464, -77.0428)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 12f))

        googleMap?.setOnMapClickListener { latLng ->
            googleMap?.clear()
            googleMap?.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
            selectedLatLng = latLng
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                googleMap?.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Error al habilitar la ubicación", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun actualizarMapaSegunDireccion() {
        val departamento = spinnerDepartamento.selectedItem?.toString()?.trim() ?: ""
        val distrito = spinnerDistrito.selectedItem?.toString()?.trim() ?: ""
        val direccion = edtDireccion.text?.toString()?.trim() ?: ""

        if (departamento.isNotEmpty() && departamento != "Selecciona un departamento"
            && distrito.isNotEmpty() && distrito != "Selecciona un distrito"
            && direccion.isNotEmpty()
        ) {
            val direccionCompleta = "$direccion, $distrito, $departamento, Perú"
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            try {
                val direcciones = geocoder.getFromLocationName(direccionCompleta, 1)
                if (!direcciones.isNullOrEmpty()) {
                    val location = direcciones[0]
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap?.clear()
                    googleMap?.addMarker(MarkerOptions().position(latLng).title("Ubicación ingresada"))
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    selectedLatLng = latLng
                }
            } catch (e: Exception) {
                // si no hay conexión o no encuentra la dirección
            }
        }
    }

    private fun validarCampos(): Boolean {
        val departamento = spinnerDepartamento.selectedItem?.toString()?.trim() ?: ""
        val distrito = spinnerDistrito.selectedItem?.toString()?.trim() ?: ""
        val direccion = edtDireccion.text?.toString()?.trim() ?: ""
        val telefono = edtPhoneFull.text?.toString()?.trim() ?: ""

        if (departamento == "Selecciona un departamento") {
            Toast.makeText(requireContext(), "Seleccione un departamento", Toast.LENGTH_SHORT).show()
            return false
        }
        if (distrito == "Selecciona un distrito") {
            Toast.makeText(requireContext(), "Seleccione un distrito", Toast.LENGTH_SHORT).show()
            return false
        }
        if (direccion.length < 5) {
            edtDireccion.error = "Dirección muy corta"
            return false
        }
        if (!telefono.matches(Regex("^9\\d{8}\$"))) {
            edtPhoneFull.error = "Teléfono inválido (debe empezar con 9 y tener 9 dígitos)"
            return false
        }
        return true
    }

    private fun guardarCamposEnPrefs() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) return
        val uid = user.uid
        val prefs = requireContext().getSharedPreferences("location_prefs", 0)
        prefs.edit().apply {
            putString("departamento_$uid", spinnerDepartamento.selectedItem?.toString())
            putString("distrito_$uid", spinnerDistrito.selectedItem?.toString())
            putString("direccion_$uid", edtDireccion.text?.toString())
            putString("telefono_$uid", edtPhoneFull.text?.toString())
            putString("notas_$uid", edtNotsAditionals.text?.toString())
            apply()
        }
    }

    private fun cargarCamposDePrefs() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) return
        val uid = user.uid
        val prefs = requireContext().getSharedPreferences("location_prefs", 0)
        val departamento = prefs.getString("departamento_$uid", null)
        val direccion = prefs.getString("direccion_$uid", null)
        val telefono = prefs.getString("telefono_$uid", null)
        val notas = prefs.getString("notas_$uid", null)

        departamento?.let {
            val depIndex = UbigeoPeru.departamentos.indexOf(it)
            if (depIndex >= 0) spinnerDepartamento.setSelection(depIndex)
        }
        // El distrito se carga después de que el departamento esté seleccionado
        edtDireccion.setText(direccion)
        edtPhoneFull.setText(telefono)
        edtNotsAditionals.setText(notas)
    }

    // CAMBIO: usa la clave distrito_$uid
    private fun cargarDistritoDePrefsSiCorresponde() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) return
        val uid = user.uid
        val prefs = requireContext().getSharedPreferences("location_prefs", 0)
        val distrito = prefs.getString("distrito_$uid", null)
        distrito?.let {
            val departamentoActual = spinnerDepartamento.selectedItem?.toString()
            val distritos = UbigeoPeru.distritosPorDepartamento[departamentoActual] ?: emptyList()
            val distIndex = distritos.indexOf(it)
            if (distIndex >= 0) spinnerDistrito.setSelection(distIndex)
        }
    }
}