package com.proyect.ravvisant.features.favorites.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.domain.repository.FavoriteRepository
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {
    private val repository = FavoriteRepository()
    
    private val _favorites = MutableLiveData<List<Product>>()
    val favorites: LiveData<List<Product>> = _favorites
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        loadFavorites()
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
                    // Actualizar la lista local
                    val currentList = _favorites.value?.toMutableList() ?: mutableListOf()
                    currentList.removeAll { it.id == product.id }
                    _favorites.value = currentList
                    
                    // Sincronizar el contador
                    repository.syncFavoriteCount()
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
                if (success) {
                    // Si estamos en la pantalla de favoritos y quitamos un favorito
                    if (product.isFavorite) {
                        val currentList = _favorites.value?.toMutableList() ?: mutableListOf()
                        currentList.removeAll { it.id == product.id }
                        _favorites.value = currentList
                    }
                } else {
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