package com.proyect.ravvisant.features.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyect.ravvisant.R
import com.proyect.ravvisant.core.common.ProductClickCallback
import com.proyect.ravvisant.databinding.FragmentHomeBinding
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.features.categories.adapter.CategoryAdapter
import com.proyect.ravvisant.features.home.adapters.HomeProductAdapter
import com.proyect.ravvisant.features.home.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        //Configuracion de adaptadores
        setupProductRecyclerView()
        setupCategoryRecyclerView()
        //Observa datos
        observeViewModel()
    }

    private fun setupProductRecyclerView() {
        val productCallback = object : ProductClickCallback {
            override fun onFavoriteClick(product: Product) {
                viewModel.toggleFavorite(product)
            }

            override fun onAddToCartClick(product: Product) {
                viewModel.addToCart(product)
            }

            override fun onProductClick(product: Product) {
                val bundle = Bundle()
                bundle.putString("productId", product.id)
                findNavController().navigate(R.id.productDetailFragment, bundle)
            }
        }

        adapter = HomeProductAdapter(productCallback)
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeFragment.adapter
        }
    }
    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter()
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.products.collect { products ->
                        adapter.submitList(products)
                    }
                }

                launch {
                    viewModel.categories.collect { categories ->
                        categoryAdapter.submitList(categories)
                    }
                }
            }
        }
    }
}