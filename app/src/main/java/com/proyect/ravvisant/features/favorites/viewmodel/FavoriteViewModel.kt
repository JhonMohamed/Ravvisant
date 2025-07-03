package com.proyect.ravvisant.features.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.domain.repository.FavoriteRepository
import com.proyect.ravvisant.core.firebase.FavoriteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {
    private val repository = FavoriteRepository()
    
    private val _favorites = MutableStateFlow<List<Product>>(emptyList())
    val favorites: StateFlow<List<Product>> = _favorites
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Callback para cambios en favoritos
    private val favoriteChangeCallback: (String, Boolean) -> Unit = { productId, isFavorite ->
        if (!isFavorite) {
            // Si se quitó un favorito, removerlo de la lista
            val currentList = _favorites.value.toMutableList()
            currentList.removeAll { it.id == productId }
            _favorites.value = currentList
        }
    }
    
    init {
        // Registrar listener para cambios en favoritos
        FavoriteService.addFavoriteChangeListener(favoriteChangeCallback)
    }
    
    override fun onCleared() {
        super.onCleared()
        // Remover listener cuando el ViewModel se destruye
        FavoriteService.removeFavoriteChangeListener(favoriteChangeCallback)
    }
    
    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val favoritesList = repository.getFavorites()
                _favorites.value = favoritesList
            } catch (e: Exception) {
                _error.value = "Error al cargar favoritos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun removeFromFavorites(product: Product) {
        viewModelScope.launch {
            try {
                val success = repository.removeFromFavorites(product.id)
                if (success) {
                    // La lista se actualiza automáticamente a través del listener
                } else {
                    _error.value = "Error al eliminar de favoritos"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar de favoritos: ${e.message}"
            }
        }
    }
    
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            try {
                val success = repository.toggleFavorite(product)
                if (!success) {
                    _error.value = "Error al actualizar favoritos"
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar favoritos: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 