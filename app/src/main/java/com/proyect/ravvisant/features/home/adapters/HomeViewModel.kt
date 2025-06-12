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