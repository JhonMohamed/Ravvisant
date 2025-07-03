package com.proyect.ravvisant.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.domain.model.Product
import kotlinx.coroutines.tasks.await

object FavoriteService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Lista de callbacks para notificar cambios en favoritos
    private val favoriteChangeCallbacks = mutableListOf<(String, Boolean) -> Unit>()
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    private fun getFavoritesCollection() = firestore.collection("users")
        .document(getCurrentUserId() ?: "")
        .collection("favorites")
    
    // Función para registrar callbacks de cambios en favoritos
    fun addFavoriteChangeListener(callback: (String, Boolean) -> Unit) {
        favoriteChangeCallbacks.add(callback)
    }
    
    // Función para remover callbacks
    fun removeFavoriteChangeListener(callback: (String, Boolean) -> Unit) {
        favoriteChangeCallbacks.remove(callback)
    }
    
    // Función para notificar cambios a todos los listeners
    private fun notifyFavoriteChange(productId: String, isFavorite: Boolean) {
        favoriteChangeCallbacks.forEach { callback ->
            try {
                callback(productId, isFavorite)
            } catch (e: Exception) {
                // Ignorar errores en callbacks individuales
            }
        }
    }
    
    suspend fun getFavorites(): List<Product> {
        return try {
            val userId = getCurrentUserId() ?: return emptyList()
            val snapshot = getFavoritesCollection().get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun addToFavorites(product: Product): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            getFavoritesCollection().document(product.id).set(product).await()
            // Notificar el cambio
            notifyFavoriteChange(product.id, true)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun removeFromFavorites(productId: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            getFavoritesCollection().document(productId).delete().await()
            // Notificar el cambio
            notifyFavoriteChange(productId, false)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun isFavorite(productId: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            val doc = getFavoritesCollection().document(productId).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun toggleFavorite(product: Product): Boolean {
        return if (product.isFavorite) {
            removeFromFavorites(product.id)
        } else {
            addToFavorites(product)
        }
    }
    
    suspend fun updateProductFavoriteStatus(products: List<Product>): List<Product> {
        return products.map { product ->
            val isFavorite = isFavorite(product.id)
            product.copy(isFavorite = isFavorite)
        }
    }
    
    suspend fun syncFavoriteCount() {
        try {
            val userId = getCurrentUserId() ?: return
            val snapshot = getFavoritesCollection().get().await()
            FavoriteCountService.updateCount(snapshot.size())
        } catch (e: Exception) {
            FavoriteCountService.resetCount()
        }
    }
} 