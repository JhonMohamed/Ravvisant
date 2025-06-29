package com.proyect.ravvisant.core.firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FavoriteCountService {
    private val TAG = "FavoriteCountService"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _favoriteCount = MutableLiveData<Int>(0)
    val favoriteCount: LiveData<Int> = _favoriteCount
    
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    private fun getFavoritesCollection() = firestore.collection("users")
        .document(getCurrentUserId() ?: "")
        .collection("favorites")
    
    fun loadFavoriteCount() {
        val userId = getCurrentUserId()
        Log.d(TAG, "Loading favorite count for user: $userId")
        
        if (userId == null) {
            Log.w(TAG, "No user logged in, setting count to 0")
            _favoriteCount.postValue(0)
            return
        }
        
        // Cancelar listener anterior si existe
        listenerRegistration?.remove()
        
        // Configurar listener en tiempo real
        listenerRegistration = getFavoritesCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to favorites", error)
                    _favoriteCount.postValue(0)
                    return@addSnapshotListener
                }
                
                val count = snapshot?.size() ?: 0
                Log.d(TAG, "Favorite count updated: $count")
                _favoriteCount.postValue(count)
            }
    }
    
    fun stopListening() {
        Log.d(TAG, "Stopping favorite count listener")
        listenerRegistration?.remove()
        listenerRegistration = null
    }
    
    fun incrementCount() {
        val currentCount = _favoriteCount.value ?: 0
        _favoriteCount.postValue(currentCount + 1)
    }
    
    fun decrementCount() {
        val currentCount = _favoriteCount.value ?: 0
        _favoriteCount.postValue(maxOf(0, currentCount - 1))
    }
    
    fun updateCount(count: Int) {
        _favoriteCount.postValue(count)
    }
    
    fun resetCount() {
        _favoriteCount.postValue(0)
    }
} 