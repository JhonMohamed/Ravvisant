package com.proyect.ravvisant.features.product.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.proyect.ravvisant.R
import com.proyect.ravvisant.core.common.ProductClickCallback
import com.proyect.ravvisant.databinding.FragmentProductBinding
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.features.product.adapters.ProductAdapter
import com.proyect.ravvisant.features.product.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductFragment : Fragment(), ProductClickCallback {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        setupRecyclerView()
        observeViewModel()
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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { products ->
                productAdapter.submitList(products)
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
        TODO("Not yet implemented")
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