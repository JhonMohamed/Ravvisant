package com.proyect.ravvisant.core.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.proyect.ravvisant.domain.model.CartItem
import kotlinx.coroutines.tasks.await

object CartService {
    private val TAG = "CartService"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun getCartCollection() = firestore.collection("users")
        .document(getCurrentUserId() ?: "")
        .collection("cart")

    suspend fun getCartItems(): List<CartItem> {
        return try {
            val userId = getCurrentUserId() ?: return emptyList()
            val snapshot = getCartCollection().get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(CartItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addToCart(item: CartItem): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            
            // Obtener cantidad actual en el carrito
            val docRef = getCartCollection().document(item.id)
            val existing = docRef.get().await().toObject(CartItem::class.java)
            val currentCartQuantity = existing?.quantity ?: 0
            
            // Validar stock antes de agregar
            val validation = StockValidationService.validateAddToCart(
                productId = item.id,
                quantityToAdd = item.quantity,
                currentCartQuantity = currentCartQuantity
            )
            
            if (!validation.isValid) {
                Log.w(TAG, "Stock validation failed: ${validation.message}")
                return false
            }
            
            // Agregar al carrito si la validación es exitosa
            val newQuantity = currentCartQuantity + item.quantity
            docRef.set(item.copy(quantity = newQuantity)).await()
            CartCountService.syncCartCount()
            
            Log.d(TAG, "Added to cart: ${item.name}, quantity: $newQuantity")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart", e)
            false
        }
    }

    suspend fun removeFromCart(productId: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            getCartCollection().document(productId).delete().await()
            CartCountService.syncCartCount()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateQuantity(productId: String, quantity: Int): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            
            // Validar stock antes de actualizar
            val validation = StockValidationService.validateUpdateQuantity(productId, quantity)
            
            if (!validation.isValid) {
                Log.w(TAG, "Stock validation failed: ${validation.message}")
                return false
            }
            
            // Actualizar cantidad si la validación es exitosa
            if (quantity <= 0) {
                getCartCollection().document(productId).delete().await()
                Log.d(TAG, "Removed from cart: $productId")
            } else {
                getCartCollection().document(productId).update("quantity", quantity).await()
                Log.d(TAG, "Updated cart quantity: $productId, quantity: $quantity")
            }
            
            CartCountService.syncCartCount()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cart quantity", e)
            false
        }
    }

    suspend fun clearCart() {
        try {
            val userId = getCurrentUserId() ?: return
            val items = getCartCollection().get().await()
            for (doc in items.documents) {
                doc.reference.delete().await()
            }
            CartCountService.syncCartCount()
        } catch (_: Exception) {}
    }

    fun listenCartItems(onUpdate: (List<CartItem>) -> Unit): ListenerRegistration? {
        val userId = getCurrentUserId() ?: return null
        return getCartCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CartItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(items)
            }
    }
} 