package com.proyect.ravvisant.features.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.proyect.ravvisant.R
import com.proyect.ravvisant.features.location.LocationFragment


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

        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        val btnAction = view.findViewById<ImageButton>(R.id.btnAction)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val Ubication = view.findViewById<CardView>(R.id.cvDirecciones)

        Ubication.setOnClickListener {
            findNavController().navigate(R.id.locationFragment)
        }

        toolbarTitle?.text = "Perfil"

        btnAction?.visibility = View.GONE

        if (isInBottomNavigation()) {
            toolbar?.navigationIcon = null
        } else {
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