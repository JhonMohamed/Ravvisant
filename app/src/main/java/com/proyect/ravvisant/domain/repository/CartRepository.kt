package com.proyect.ravvisant.domain.repository

import com.proyect.ravvisant.core.firebase.CartService
import com.proyect.ravvisant.domain.model.CartItem

class CartRepository {
    suspend fun getCartItems(): List<CartItem> = CartService.getCartItems()
    suspend fun addToCart(item: CartItem): Boolean = CartService.addToCart(item)
    suspend fun removeFromCart(productId: String): Boolean = CartService.removeFromCart(productId)
    suspend fun updateQuantity(productId: String, quantity: Int): Boolean = CartService.updateQuantity(productId, quantity)
    suspend fun clearCart() = CartService.clearCart()
    fun listenCartItems(onUpdate: (List<CartItem>) -> Unit) = CartService.listenCartItems(onUpdate)
} 