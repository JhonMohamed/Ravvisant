package com.proyect.ravvisant.features.home.adapters

import androidx.lifecycle.ViewModel
import com.proyect.ravvisant.data.model.Category
import com.proyect.ravvisant.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories
    fun toggleFavorite(product: Product) {
        val currentProducts = _products.value.toMutableList()
        val index = currentProducts.indexOfFirst { it.id == product.id }
        if (index != -1) {
            val updatedProduct = product.copy(isFavorite = !product.isFavorite)
            currentProducts[index] = updatedProduct
            _products.value = currentProducts
        }
    }

    init {
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
            Product(
                id = "3",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615153/rolex_starbucks3_djf9vt.jpg"
            ),
            Product(
                id = "4",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609106/reloj_cronos_verde_ad2ejo.webp"
            ),
            Product(
                id = "5",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615155/rolex_starbucks_w0f2co.webp"
            ),
            Product(
                id = "6",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609106/reloj_cronos_verde_ad2ejo.webp"
            ),
            Product(
                id = "7",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_crono_verde_3_xl8s6w.jpg"
            ),
            Product(
                id = "8",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_crono_verde_3_xl8s6w.jpg"
            ),
            Product(
                id = "9",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615155/rolex_starbucks_w0f2co.webp"
            ),
            Product(
                id = "10",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615156/reloj_ap_royal3_je9qhm.jpg"
            )
        )
        _products.value = sampleProducts

        // Datos de prueba para categorías
        val sampleCategories = listOf(
            Category(
                id = "1",
                name = "Joyería",
                itemCount = 156,
                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg"
            ),
            Category(
                id = "2",
                name = "Relojes",
                itemCount = 89,
                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg"
            ),
            Category(
                id = "3",
                name = "Perfumes",
                itemCount = 210,
                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg"
            ),
            Category(
                id = "4",
                name = "Perfumes",
                itemCount = 210,
                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg"
            ),
            Category(
                id = "5",
                name = "Perfumes",
                itemCount = 210,
                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg"
            ),
            Category(
                id = "6",
                name = "Perfumes",
                itemCount = 210,
                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg"
            )
        )
        _categories.value = sampleCategories

    }

    fun addToCart(product: Product) {
        //Logica con firebase
    }
}