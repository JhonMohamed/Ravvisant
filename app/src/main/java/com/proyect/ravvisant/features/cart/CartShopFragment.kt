package com.proyect.ravvisant.features.cart

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.proyect.ravvisant.R
import com.proyect.ravvisant.features.cart.adapter.CartAdapter
import com.proyect.ravvisant.features.cart.viewmodel.CartViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartShopFragment : Fragment() {
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart_shop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        setupRecyclerView(view)
        viewModel.startListeningCart()
        observeCartItems()
        setupBottomBar(view)
    }

    private fun setupToolbar(view: View) {
        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        val btnAction = view.findViewById<ImageButton>(R.id.btnAction)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        toolbarTitle?.text = "Carrito"

        btnAction?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_trash)
            setOnClickListener {
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

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        
        cartAdapter = CartAdapter(
            onQuantityChanged = { productId, quantity ->
                viewModel.updateQuantity(productId, quantity)
            },
            onRemoveItem = { productId ->
                viewModel.removeFromCart(productId)
            }
        )

        recyclerView.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeCartItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cartItems.collectLatest { items ->
                cartAdapter.submitList(items)
                updateEmptyState(items.isEmpty())
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        view?.findViewById<View>(R.id.emptyState)?.visibility = 
            if (isEmpty) View.VISIBLE else View.GONE
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.visibility = 
            if (isEmpty) View.GONE else View.VISIBLE
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
                viewModel.clearCart()
            }
            .show()
    }

    private fun setupBottomBar(view: View) {
        val totalAmountText = view.findViewById<TextView>(R.id.totalAmount)
        val totalProductsText = view.findViewById<TextView>(R.id.totalProducts)
        val btnProceedToPay = view.findViewById<Button>(R.id.btnProceedToPay)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalAmount.collectLatest { total ->
                totalAmountText.text = "$${"%.2f".format(total)}"
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalProducts.collectLatest { count ->
                totalProductsText.text = "$count productos"
            }
        }
        btnProceedToPay.setOnClickListener {
            findNavController().navigate(R.id.payFragment)
        }
    }
}