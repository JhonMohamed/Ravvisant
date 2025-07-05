package com.proyect.ravvisant.features.product.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.proyect.ravvisant.core.common.loadImage
import com.proyect.ravvisant.databinding.FragmentProductDetailBinding
import com.proyect.ravvisant.features.product.viewmodel.ProductDetailViewModel
import com.proyect.ravvisant.features.product.adapters.ProductThumbnailAdapter
import com.proyect.ravvisant.domain.model.CartItem
import com.proyect.ravvisant.features.cart.viewmodel.CartViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()
    private lateinit var thumbnailAdapter: ProductThumbnailAdapter
    private val cartViewModel: CartViewModel by activityViewModels()

    companion object {
        private const val ARG_PRODUCT_ID = "productId"
        
        fun newInstance(productId: String): ProductDetailFragment {
            return ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRODUCT_ID, productId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(ARG_PRODUCT_ID)?.let { productId ->
            viewModel.loadProduct(productId)
        }

        setupProductImagesRecyclerView()
        observeViewModel()

        binding.btnAddToCart.setOnClickListener {
            val product = viewModel.product.value
            if (product != null) {
                val cartItem = CartItem(
                    id = product.id,
                    name = product.name,
                    imageUrl = product.imageUrl,
                    price = product.price,
                    quantity = 1
                )
                cartViewModel.addToCart(cartItem)
                Toast.makeText(requireContext(), "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No se pudo agregar el producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupProductImagesRecyclerView() {
        thumbnailAdapter = ProductThumbnailAdapter { position ->
            viewModel.product.value?.let { product ->
                if (product.imageUrls.size > position) {
                    // Accedemos directamente a imgProductMain
                    binding.imgProductMain.loadImage(product.imageUrls[position])
                    thumbnailAdapter.selectImage(position)
                }
            }
        }

        // Accedemos directamente al RecyclerView
        binding.rvProductImages.adapter = thumbnailAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.product.collectLatest { product ->
                product?.let {
                    binding.product = it
                    thumbnailAdapter.setImages(it.imageUrls)
                    binding.executePendingBindings()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}