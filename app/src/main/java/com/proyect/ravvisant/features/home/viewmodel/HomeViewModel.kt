package com.proyect.ravvisant.features.home.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.domain.model.Category
import com.proyect.ravvisant.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val firestore = FirebaseFirestore.getInstance()

    // Cargar categorías desde Firebase al iniciar
    init {
        loadCategoriesFromFirebase()
    }

    // Cargar categorías desde Firebase
    private fun loadCategoriesFromFirebase() {
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categoryList = result.toObjects(Category::class.java)
                _categories.value = categoryList
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error al cargar categorías", exception)
            }
    }

//    fun uploadSampleCategoriesToFirebase(context: Context) {
//        val batch = firestore.batch()
//
//        // Datos de prueba para categorías
//        val sampleCategories = listOf(
//            Category(
//                id = "1",
//                name = "Joyería",
//                itemCount = 156,
//                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1751089392/categoria_joyas_s0ygjf.png"
//            ),
//            Category(
//                id = "2",
//                name = "Relojes",
//                itemCount = 89,
//                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1751089393/categoria_reloj_cr2pdh.jpg"
//            ),
//            Category(
//                id = "3",
//                name = "Lentes",
//                itemCount = 210,
//                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1751089392/categoria_lentes_ifjrdi.png"
//            ),
//            Category(
//                id = "4",
//                name = "Perfumes",
//                itemCount = 210,
//                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1751089392/categoria_perfume_nn1uix.jpg"
//            ),
//            Category(
//                id = "5",
//                name = "Gorras",
//                itemCount = 210,
//                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1751089392/categoria_gorra_czrlza.jpg"
//            ),
//        )
//        _categories.value = sampleCategories
//
//
//
//        for (category in sampleCategories) {
//            val documentRef = firestore.collection("categories").document(category.id)
//            batch.set(documentRef, category)
//        }
//
//        batch.commit().addOnSuccessListener {
//            Toast.makeText(context, "Categorías subidas correctamente", Toast.LENGTH_SHORT).show()
//        }.addOnFailureListener { exception ->
//            Toast.makeText(context, "Error al subir categorías", Toast.LENGTH_SHORT).show()
//            Log.e("HomeViewModel", "Error al subir categorías", exception)
//        }
//    }

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
                imageUrls = listOf(
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
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
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
            ),
            Product(
                id = "3",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrls = listOf(
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
            ),
            Product(
                id = "4",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrls = listOf(
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
            ),
            Product(
                id = "5",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrls = listOf(
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
            ),
            Product(
                id = "6",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrls = listOf(
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
            ),
            Product(
                id = "7",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrls = listOf(
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
            ),
            Product(
                id = "8",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrls = listOf(
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
            ),
            Product(
                id = "9",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrls = listOf(
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
            ),
            Product(
                id = "10",
                name = "Dior Sauvage",
                brand = "Dior",
                price = 120.0,
                rating = 4.5f,
                stock = 10,
                imageUrls = listOf(
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                    "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
                )
            )
        )
        _products.value = sampleProducts




        fun addToCart(product: Product) {
            //Logica con firebase
        }
    }
}