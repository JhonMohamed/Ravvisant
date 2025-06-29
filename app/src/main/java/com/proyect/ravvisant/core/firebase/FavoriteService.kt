package com.proyect.ravvisant.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.domain.model.Product
import kotlinx.coroutines.tasks.await

object FavoriteService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    private fun getFavoritesCollection() = firestore.collection("users")
        .document(getCurrentUserId() ?: "")
        .collection("favorites")
    
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
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun removeFromFavorites(productId: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            getFavoritesCollection().document(productId).delete().await()
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