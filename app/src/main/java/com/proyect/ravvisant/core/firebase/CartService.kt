package com.proyect.ravvisant.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.proyect.ravvisant.domain.model.CartItem
import kotlinx.coroutines.tasks.await

object CartService {
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
            val docRef = getCartCollection().document(item.id)
            val existing = docRef.get().await().toObject(CartItem::class.java)
            val newQuantity = (existing?.quantity ?: 0) + item.quantity
            docRef.set(item.copy(quantity = newQuantity)).await()
            CartCountService.syncCartCount()
            true
        } catch (e: Exception) {
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
            if (quantity <= 0) {
                getCartCollection().document(productId).delete().await()
            } else {
                getCartCollection().document(productId).update("quantity", quantity).await()
            }
            CartCountService.syncCartCount()
            true
        } catch (e: Exception) {
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