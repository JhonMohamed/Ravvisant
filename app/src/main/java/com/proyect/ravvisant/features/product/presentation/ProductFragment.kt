package com.proyect.ravvisant.features.product.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyect.ravvisant.R
import com.proyect.ravvisant.core.common.ProductClickCallback
import com.proyect.ravvisant.databinding.FragmentProductBinding
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.features.categories.adapter.CategoryAdapter
import com.proyect.ravvisant.features.home.viewmodel.HomeViewModel
import com.proyect.ravvisant.features.product.adapters.ProductAdapter
import com.proyect.ravvisant.features.product.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductFragment : Fragment(), ProductClickCallback {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        setupRecyclerView()
        setupCategoryRecyclerView()
        binding.btnShowAll.setOnClickListener {
            viewModel.filterProductsByCategory("all")
        }
        observeViewModel()
//                binding.btnUploadCategories.setOnClickListener {
//            viewModel.uploadSampleCategoriesToFirebase(requireContext())
//        }
//        binding.btnUploadProducts.setOnClickListener {
//            viewModel.uploadSampleProductsToFirebase(requireContext())
//        }
    }

    private fun setupRecyclerView() { productAdapter = ProductAdapter(this)
        binding.recyclerProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }
    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            // Aquí recibes la categoría seleccionada
            viewModel.filterProductsByCategory(category.id)
        }
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
                        productAdapter.submitList(products)
                    }
                }

                launch {
                    viewModel.categories.collect { categories ->
                        categoryAdapter.submitList(categories)
                    }
                }
            }
        }


//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.isLoading.collectLatest { isLoading ->
//                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//            }
//        }
    }


    override fun onFavoriteClick(product: Product) {
        viewModel.toggleFavorite(product)
    }

    override fun onAddToCartClick(product: Product) {
        viewModel.addToCart(product, requireContext())
    }


    override fun onProductClick(product: Product) {
        val bundle = Bundle()
        bundle.putString("productId", product.id)
        findNavController().navigate(R.id.productDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}