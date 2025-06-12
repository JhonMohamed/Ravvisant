package com.proyect.ravvisant.features.product.adapters

import androidx.lifecycle.ViewModel
import com.proyect.ravvisant.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadProducts()
    }

    private fun loadProducts() {
        _isLoading.value = true

        val sampleProducts = listOf(
            Product(
                id = "1",
                name = "Chanel No. 5",
                brand = "Chanel",
                price = 149.99,
                rating = 4.7f,
                stock = 18,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg"
            ),
            Product(
                id = "2",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609106/reloj_cronos_verde_ad2ejo.webp"
            ),
            // Añade más productos aquí
            Product(
                id = "3",
                name = "Rolex Submariner",
                brand = "Rolex",
                price = 14500.0,
                rating = 4.9f,
                stock = 5,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615153/rolex_starbucks3_djf9vt.jpg"
            ),
            Product(
                id = "4",
                name = "Audemars Piguet Royal Oak",
                brand = "AP",
                price = 31000.0,
                rating = 4.8f,
                stock = 3,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615156/reloj_ap_royal3_je9qhm.jpg"
            ),
            Product(
                id = "5",
                name = "Patek Philippe Nautilus",
                brand = "Patek Philippe",
                price = 70000.0,
                rating = 5.0f,
                stock = 2,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg"
            )
        )
        _products.value = sampleProducts
        _isLoading.value = false
    }


    fun toggleFavorite(product: Product) {
        val currentProducts = _products.value.toMutableList()
        val index = currentProducts.indexOfFirst { it.id == product.id }

        if (index != -1) {
            val updateProduct = product.copy(isFavorite = !product.isFavorite)

            currentProducts[index] = updateProduct
            _products.value = currentProducts

            //Logica para guardar cambios en firebase

        }

    }


    fun addToCart(product: Product) {
        //Firebase
    }


}