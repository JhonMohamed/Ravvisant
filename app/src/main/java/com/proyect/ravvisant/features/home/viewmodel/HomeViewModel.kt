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
import com.proyect.ravvisant.core.firebase.FavoriteService
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

    private val _bannerProducts = MutableStateFlow<List<Product>>(emptyList())
    val bannerProducts: StateFlow<List<Product>> = _bannerProducts


    private val firestore = FirebaseFirestore.getInstance()
    private val favoriteRepository = FavoriteRepository()
    private val cartRepository = CartRepository()
    
    // Callback para cambios en favoritos
    private val favoriteChangeCallback: (String, Boolean) -> Unit = { productId, isFavorite ->
        updateProductFavoriteStatus(productId, isFavorite)
    }

    // Cargar categorías desde Firebase al iniciar
    init {
        loadProductsWithFavorites()
        loadBannerProducts()
        // Registrar listener para cambios en favoritos
        FavoriteService.addFavoriteChangeListener(favoriteChangeCallback)
    }
    private fun loadBannerProducts() {
        // Paso 1: Cargar todas las categorías
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { categoriesSnapshot ->
                val categoryIds = categoriesSnapshot.documents.mapNotNull { it.getString("id") }

                // Paso 2: Para cada categoría, cargar un producto
                val productRef = firestore.collection("products")

                categoryIds.forEach { categoryId ->
                    productRef.whereEqualTo("categoryId", categoryId)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { productSnapshot ->
                            if (!productSnapshot.isEmpty) {
                                val product = productSnapshot.documents.first().toObject(Product::class.java)
                                updateBannerProducts(product)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("HomeViewModel", "Error al cargar producto de $categoryId", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error al cargar categorías", exception)
            }
    }

    private var bannerProductList = mutableListOf<Product>()

    private fun updateBannerProducts(product: Product?) {
        product?.let {
            bannerProductList.add(it)
            _bannerProducts.value = bannerProductList
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Remover listener cuando el ViewModel se destruye
        FavoriteService.removeFavoriteChangeListener(favoriteChangeCallback)
    }
    
    // Función para actualizar el estado de favoritos de un producto específico
    private fun updateProductFavoriteStatus(productId: String, isFavorite: Boolean) {
        val currentProducts = _products.value.toMutableList()
        val index = currentProducts.indexOfFirst { it.id == productId }
        if (index != -1) {
            val product = currentProducts[index]
            val updatedProduct = product.copy(isFavorite = isFavorite)
            currentProducts[index] = updatedProduct
            _products.value = currentProducts
        }
        
        // También actualizar en allProducts para mantener consistencia
        val allProductsIndex = allProducts.indexOfFirst { it.id == productId }
        if (allProductsIndex != -1) {
            val product = allProducts[allProductsIndex]
            val updatedProduct = product.copy(isFavorite = isFavorite)
            allProducts = allProducts.toMutableList().apply { this[allProductsIndex] = updatedProduct }
        }
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


    // Cargar productos y verificar su estado de favoritos
    private fun loadProductsWithFavorites() {
        viewModelScope.launch {
            val firestoreProducts = loadProductsFromFirebase()
            val productsWithFavorites = favoriteRepository.updateProductFavoriteStatus(firestoreProducts)
            allProducts = productsWithFavorites
            _products.value = productsWithFavorites
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            try {
                val success = favoriteRepository.toggleFavorite(product)
                if (success) {
                    // El estado se actualiza automáticamente a través del listener
                    // No necesitamos actualizar manualmente aquí
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