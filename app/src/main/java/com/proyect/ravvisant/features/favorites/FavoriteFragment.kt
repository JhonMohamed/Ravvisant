package com.proyect.ravvisant.features.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.proyect.ravvisant.R
import com.proyect.ravvisant.core.common.ProductClickCallback
import com.proyect.ravvisant.databinding.FragmentFavoriteBinding
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.domain.model.CartItem
import com.proyect.ravvisant.features.favorites.adapter.FavoriteAdapter
import com.proyect.ravvisant.features.favorites.viewmodel.FavoriteViewModel
import com.proyect.ravvisant.features.cart.viewmodel.CartViewModel
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FavoriteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoriteFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FavoriteViewModel by viewModels()
    private lateinit var adapter: FavoriteAdapter
    private val cartViewModel: CartViewModel by activityViewModels()

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
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        // Cargar favoritos al crear la vista
        viewModel.loadFavorites()
    }

    private fun setupRecyclerView() {
        val productCallback = object : ProductClickCallback {
            override fun onFavoriteClick(product: Product) {
                // En la pantalla de favoritos, el botón de favorito elimina el producto
                viewModel.removeFromFavorites(product)
            }

            override fun onAddToCartClick(product: Product) {
                val cartItem = CartItem(
                    id = product.id,
                    name = product.name,
                    imageUrl = product.imageUrl,
                    price = product.price,
                    quantity = 1
                )
                cartViewModel.addToCart(cartItem)
                Toast.makeText(requireContext(), "${product.name} agregado al carrito", Toast.LENGTH_SHORT).show()
            }

            override fun onProductClick(product: Product) {
                val bundle = Bundle()
                bundle.putString("productId", product.id)
                findNavController().navigate(R.id.productDetailFragment, bundle)
            }
        }

        adapter = FavoriteAdapter(
            callback = productCallback,
            onRemoveFavorite = { product ->
                viewModel.removeFromFavorites(product)
            }
        )

        binding.rvFavorites.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@FavoriteFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.favorites.collect { favorites ->
                        adapter.submitList(favorites)
                        updateEmptyState(favorites.isEmpty())
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvFavorites.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FavoriteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = FavoriteFragment()
    }
}