package com.proyect.ravvisant.features.cart

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.proyect.ravvisant.R


class CartShopFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart_shop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar la barra superior
        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        val btnAction = view.findViewById<ImageButton>(R.id.btnAction)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        toolbarTitle?.text = "Carrito"

        btnAction?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_trash)
            setOnClickListener {
                // Acción para vaciar el carrito
                mostrarDialogoConfirmacion()
            }
        }

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

    private fun mostrarDialogoConfirmacion() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Vaciar carrito")
            .setMessage("¿Estás seguro de que deseas vaciar el carrito?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Vaciar") { _, _ ->
                // Lógica para vaciar el carrito
                // viewModel.vaciarCarrito()
            }
            .show()
    }
}