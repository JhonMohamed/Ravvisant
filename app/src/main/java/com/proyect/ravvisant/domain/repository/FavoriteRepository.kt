package com.proyect.ravvisant.domain.repository

import com.proyect.ravvisant.core.firebase.FavoriteService
import com.proyect.ravvisant.domain.model.Product

class FavoriteRepository {
    
    suspend fun getFavorites(): List<Product> {
        return FavoriteService.getFavorites()
    }
    
    suspend fun addToFavorites(product: Product): Boolean {
        return FavoriteService.addToFavorites(product)
    }
    
    suspend fun removeFromFavorites(productId: String): Boolean {
        return FavoriteService.removeFromFavorites(productId)
    }
    
    suspend fun isFavorite(productId: String): Boolean {
        return FavoriteService.isFavorite(productId)
    }
    
    suspend fun toggleFavorite(product: Product): Boolean {
        return FavoriteService.toggleFavorite(product)
    }
    
    suspend fun updateProductFavoriteStatus(products: List<Product>): List<Product> {
        return FavoriteService.updateProductFavoriteStatus(products)
    }
    
    suspend fun syncFavoriteCount() {
        FavoriteService.syncFavoriteCount()
    }
} 