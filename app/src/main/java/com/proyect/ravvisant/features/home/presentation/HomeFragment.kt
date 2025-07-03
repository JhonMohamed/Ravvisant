package com.proyect.ravvisant.features.home.presentation

import BannerAdapter
import android.os.Bundle
import android.util.Log
import android.os.Handler
import android.os.Looper
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
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.proyect.ravvisant.R
import com.proyect.ravvisant.core.common.ProductClickCallback
import com.proyect.ravvisant.databinding.FragmentHomeBinding
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.features.categories.adapter.CategoryAdapter
import com.proyect.ravvisant.features.home.adapters.HomeProductAdapter
import com.proyect.ravvisant.features.home.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var bannerAdapter: BannerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

            //Configuracion de adaptadores
            setupProductRecyclerView()
            setupBannerCarousel()
            //Observa datos
            observeViewModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
        }
    }
    private var currentPage = 0
    private lateinit var handler: Handler
    private val updateRunnable = Runnable {
        if (currentPage == bannerAdapter.itemCount) {
            currentPage = 0
        }
        binding.viewPagerBanner.setCurrentItem(currentPage++, true)
    }

    private fun startAutoScroll() {
        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (currentPage == bannerAdapter.itemCount) {
                    currentPage = 0
                }
                binding.viewPagerBanner.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, 3000) // Cada 3 segundos
            }
        }, 3000)
    }


    private fun setupBannerCarousel() {
        bannerAdapter = BannerAdapter(requireContext()) { product ->
            val bundle = Bundle()
            bundle.putString("productId", product.id)
            findNavController().navigate(R.id.productDetailFragment, bundle)
        }

        binding.viewPagerBanner.adapter = bannerAdapter
        binding.viewPagerBanner.orientation = ViewPager2.ORIENTATION_HORIZONTAL


        // Navegación manual con flechas
        binding.btnNext.setOnClickListener {
            val nextItem = binding.viewPagerBanner.currentItem + 1
            if (nextItem < bannerAdapter.itemCount) {
                binding.viewPagerBanner.setCurrentItem(nextItem, true)
            } else {
                binding.viewPagerBanner.setCurrentItem(0, true)
            }
        }

        binding.btnPrev.setOnClickListener {
            val prevItem = binding.viewPagerBanner.currentItem - 1
            if (prevItem >= 0) {
                binding.viewPagerBanner.setCurrentItem(prevItem, true)
            } else {
                binding.viewPagerBanner.setCurrentItem(bannerAdapter.itemCount - 1, true)
            }
        }
        binding.viewPagerBanner.setPageTransformer { page, position ->
            val alpha = 0.3f.coerceAtLeast(1 - Math.abs(position))
            page.alpha = alpha
        }

        // Mantiene cargada solo una página adicional a cada lado
        binding.viewPagerBanner.offscreenPageLimit = 1

        // Añade padding para mostrar parte de las páginas lateral
        binding.viewPagerBanner.clipToPadding = false
        binding.viewPagerBanner.setPadding(16, 0, 16, 0)

        // Si ya tienes auto-scroll:
        startAutoScroll()
    }

    private fun setupProductRecyclerView() {
        try {
            val productCallback = object : ProductClickCallback {
                override fun onFavoriteClick(product: Product) {
                    try {
                        viewModel.toggleFavorite(product)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onFavoriteClick", e)
                    }
                }

                override fun onAddToCartClick(product: Product) {
                    try {
                        viewModel.addToCart(product, requireContext())
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onAddToCartClick", e)
                    }
                }

                override fun onProductClick(product: Product) {
                    try {
                        val bundle = Bundle()
                        bundle.putString("productId", product.id)
                        findNavController().navigate(R.id.productDetailFragment, bundle)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onProductClick", e)
                    }
                }
            }

            adapter = HomeProductAdapter(productCallback)
            binding.rvProducts.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = this@HomeFragment.adapter
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupProductRecyclerView", e)
        }
    }


    private fun observeViewModel() {
        try {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        try {
                            viewModel.products.collect { products ->
                                adapter.submitList(products)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error collecting products", e)
                        }
                    }

                    launch {
                        try {
                            viewModel.categories.collect { categories ->
                                categoryAdapter.submitList(categories)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error collecting categories", e)
                        }
                    }

                    launch {
                        try {
                            viewModel.bannerProducts.collect { bannerProducts ->
                                bannerAdapter.submitList(bannerProducts)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error collecting banner products", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in observeViewModel", e)
        }
    }
}