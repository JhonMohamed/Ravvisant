package com.proyect.ravvisant.features.product.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyect.ravvisant.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    init {
        // Intentar cargar el producto desde SavedStateHandle si está disponible
        savedStateHandle.get<String>("productId")?.let { productId ->
            loadProduct(productId)
        }
    }

    fun loadProduct(productId: String) {
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
                Product(
                    id = "2",
                    name = "Dior Sauvage",
                    brand = "Dior",
                    price = 120.0,
                    rating = 4.5f,
                    stock = 10,
                    imageUrls = listOf(
                        "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                        "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_lateral_pwlkku.jpg",
                        "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_frente_jk3zef.jpg"
                    ),
                    description = "Un perfume masculino moderno y sofisticado."
                ),
                Product(
                    id = "3",
                    name = "Nautilus Cara Azul",
                    brand = "Patek Philippe",
                    price = 79999.99,
                    rating = 4.9f,
                    stock = 5,
                    imageUrls = listOf(
                        "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                        "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_lateral_pwlkku.jpg",
                        "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_frente_jk3zef.jpg"
                    ),
                    description = "Reloj Patek Philippe Nautilus con carátula azul y correa de acero. Elegancia suiza atemporal."
                )
            )
            _product.value = sampleProducts.find { it.id == productId }
        }
    }
}