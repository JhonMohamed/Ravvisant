package com.proyect.ravvisant.core.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object CartCountService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cartCount = MutableLiveData<Int>(0)
    val cartCount: LiveData<Int> = _cartCount

    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun getCartCollection() = firestore.collection("users")
        .document(getCurrentUserId() ?: "")
        .collection("cart")

    fun loadCartCount() {
        val userId = getCurrentUserId() ?: return
        listenerRegistration?.remove()
        listenerRegistration = getCartCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _cartCount.postValue(0)
                    return@addSnapshotListener
                }
                val total = snapshot?.documents?.sumOf { it.getLong("quantity")?.toInt() ?: 0 } ?: 0
                _cartCount.postValue(total)
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun updateCount(count: Int) {
        _cartCount.postValue(count)
    }

    fun resetCount() {
        _cartCount.postValue(0)
    }
    
    suspend fun syncCartCount() {
        try {
            val userId = getCurrentUserId() ?: return
            val snapshot = getCartCollection().get().await()
            val total = snapshot.documents.sumOf { it.getLong("quantity")?.toInt() ?: 0 }
            updateCount(total)
        } catch (e: Exception) {
            resetCount()
        }
    }
} 