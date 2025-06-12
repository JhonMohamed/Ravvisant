package com.proyect.ravvisant.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyect.ravvisant.R
import com.proyect.ravvisant.databinding.FragmentHomeBinding
import com.proyect.ravvisant.domain.Product
import com.proyect.ravvisant.features.categories.CategoryAdapter
import com.proyect.ravvisant.features.home.adapters.HomeViewModel
import com.proyect.ravvisant.features.home.adapters.ProductAdapter
import com.proyect.ravvisant.features.home.adapters.ProductClickCallback
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: ProductAdapter
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
        }

        adapter = ProductAdapter(productCallback)
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