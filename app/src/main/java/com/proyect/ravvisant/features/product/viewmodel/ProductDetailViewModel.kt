package com.proyect.ravvisant.features.product.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product
    init {
        // Intentar cargar el producto desde SavedStateHandle si está disponible
        savedStateHandle.get<String>("productId")?.let { productId ->
            loadProduct(productId)
        }
    }

    fun loadProduct(productId: String) {
        firestore.collection("products")
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)
                _product.value = product
            }
            .addOnFailureListener { exception ->
                Log.e("ProductDetailViewModel", "Error al cargar producto", exception)
            }
    }
}