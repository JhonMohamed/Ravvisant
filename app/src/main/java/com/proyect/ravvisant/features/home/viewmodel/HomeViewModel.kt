package com.proyect.ravvisant.features.home.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.domain.model.Category
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.domain.model.CartItem
import com.proyect.ravvisant.domain.repository.FavoriteRepository
import com.proyect.ravvisant.domain.repository.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products
    private var allProducts = emptyList<Product>()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val firestore = FirebaseFirestore.getInstance()
    private val favoriteRepository = FavoriteRepository()
    private val cartRepository = CartRepository()

    // Cargar categorías desde Firebase al iniciar
    init {
        loadCategoriesFromFirebase()
        loadProductsWithFavorites()
    }

    fun filterProductsByCategory(categoryId: String) {
        viewModelScope.launch {
            // Si categoryId es vacío o "all", mostramos todos los productos
            if (categoryId.isEmpty() || categoryId == "all") {
                _products.value = allProducts
            } else {
                // Filtramos los productos según el categoryId
                val filteredProducts = allProducts.filter { it.categoryId == categoryId }
                _products.value = filteredProducts
            }
        }
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

    // Cargar productos y verificar su estado de favoritos
    private fun loadProductsWithFavorites() {
        viewModelScope.launch {
            val firestoreProducts = loadProductsFromFirebase()
            val productsWithFavorites = favoriteRepository.updateProductFavoriteStatus(firestoreProducts)
            _products.value = productsWithFavorites
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            try {
                val success = favoriteRepository.toggleFavorite(product)
                if (success) {
                    // Actualizar la lista local
                    val currentProducts = _products.value.toMutableList()
                    val index = currentProducts.indexOfFirst { it.id == product.id }
                    if (index != -1) {
                        val updatedProduct = product.copy(isFavorite = !product.isFavorite)
                        currentProducts[index] = updatedProduct
                        _products.value = currentProducts
                    }
                    // El contador se actualiza automáticamente con el listener
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al actualizar favoritos", e)
            }
        }
    }

    fun addToCart(product: Product, context: Context) {
        viewModelScope.launch {
            try {
                val cartItem = CartItem(
                    id = product.id,
                    name = product.name,
                    imageUrl = product.imageUrls.firstOrNull() ?: "",
                    price = product.price,
                    quantity = 1
                )

                val success = cartRepository.addToCart(cartItem)
                if (success) {
                    Toast.makeText(
                        context,
                        "${product.name} agregado al carrito",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Error al agregar al carrito", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al agregar al carrito", e)
                Toast.makeText(context, "Error al agregar al carrito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Recargar productos con estado de favoritos actualizado
    fun refreshProducts() {
        loadProductsWithFavorites()
    }


    private suspend fun loadProductsFromFirebase(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("products").get().await()
            snapshot.toObjects(Product::class.java)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error al cargar productos desde Firebase", e)
            emptyList()
        }
    }
}