package com.proyect.ravvisant.features.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.proyect.ravvisant.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LocationFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val spinner = view.findViewById<Spinner>(R.id.spinnerCapitales)

        val capitalesPeru = arrayOf(
            "Selecciona una capital",
            "Lima",
            "Arequipa",
            "Cusco",
            "Trujillo",
            "Chiclayo",
            "Piura",
            "Iquitos",
            "Tacna",
            "Puno",
            "Huancayo"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, capitalesPeru)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val seleccion = parent.getItemAtPosition(position).toString()
                Toast.makeText(requireContext(), "Seleccionaste: $seleccion", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nada seleccionado
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LocationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}