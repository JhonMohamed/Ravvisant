package com.proyect.ravvisant.features.cart.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyect.ravvisant.domain.model.CartItem
import com.proyect.ravvisant.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.map

class CartViewModel : ViewModel() {
    private val TAG = "CartViewModel"
    private val repository = CartRepository()
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems
    private var cartListener: ListenerRegistration? = null

    val totalAmount = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }
    val totalProducts = cartItems.map { items ->
        items.sumOf { it.quantity }
    }

    init {
        startListeningCart()
    }

    fun startListeningCart() {
        cartListener?.remove()
        cartListener = repository.listenCartItems { items ->
            _cartItems.value = items
        }
    }

    fun addToCart(item: CartItem) {
        viewModelScope.launch {
            val success = repository.addToCart(item)
            if (success) startListeningCart()
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            val success = repository.removeFromCart(productId)
            if (success) startListeningCart()
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            val success = repository.updateQuantity(productId, quantity)
            if (success) {
                startListeningCart()
            } else {
                Log.w(TAG, "Failed to update quantity for product $productId to $quantity")
                // Aquí podrías emitir un evento de error si es necesario
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
            startListeningCart()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cartListener?.remove()
    }
} 