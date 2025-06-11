package com.proyect.ravvisant.features.profile

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.proyect.ravvisant.R


class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar la barra superior
        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        val btnAction = view.findViewById<ImageButton>(R.id.btnAction)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        // Configurar título
        toolbarTitle?.text = "Perfil"

        // Ocultar botón de acción
        btnAction?.visibility = View.GONE

        // Si estamos en navegación principal (bottom nav), ocultar botón atrás
        if (isInBottomNavigation()) {
            toolbar?.navigationIcon = null
        } else {
            // Configurar navegación hacia atrás
            toolbar?.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun isInBottomNavigation(): Boolean {
        val navController = findNavController()
        return navController.currentDestination?.id in setOf(
            R.id.homeFragment,
            R.id.searchFragment,
            R.id.cartFragment,
            R.id.favoriteFragment,
            R.id.profileFragment
        )
    }
}