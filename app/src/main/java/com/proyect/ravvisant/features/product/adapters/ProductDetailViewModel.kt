package com.proyect.ravvisant.features.product.adapters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyect.ravvisant.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product


    init {
        loadProduct()
    }

    private fun loadProduct() {
        val productId = savedStateHandle.get<String>("productId") ?: return
        viewModelScope.launch {
            val sampleProducts = listOf(
                Product(
                    id = "1",
                    name = "Chanel No. 5",
                    brand = "Chanel",
                    price = 149.99,
                    rating = 4.7f,
                    stock = 18,
                    imageUrls = listOf(
                        "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                        "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_lateral_pwlkku.jpg",
                        "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_frente_jk3zef.jpg"
                    ),
                    description = "Este elegante reloj Patek Philippe Nautilus está fabricado con los mejores materiales."
                ),
            )
            _product.value = sampleProducts.find { it.id == productId }
        }
    }


}