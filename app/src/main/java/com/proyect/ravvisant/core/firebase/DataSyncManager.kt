package com.proyect.ravvisant.core.firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.proyect.ravvisant.domain.model.CartItem
import com.proyect.ravvisant.domain.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Gestor centralizado de sincronización de datos
 * Resuelve problemas de lentitud y bugs de sincronización
 */
object DataSyncManager {
    private val TAG = "DataSyncManager"
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Estado centralizado
    private val _favorites = MutableLiveData<List<Product>>(emptyList())
    val favorites: LiveData<List<Product>> = _favorites
    
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems
    
    private val _favoriteCount = MutableLiveData<Int>(0)
    val favoriteCount: LiveData<Int> = _favoriteCount
    
    private val _cartCount = MutableLiveData<Int>(0)
    val cartCount: LiveData<Int> = _cartCount
    
    // Listeners únicos para evitar duplicados
    private var favoritesListener: ListenerRegistration? = null
    private var cartListener: ListenerRegistration? = null
    
    // Cache local para evitar llamadas innecesarias
    private var favoritesCache = emptyList<Product>()
    private var cartCache = emptyList<CartItem>()
    private var lastSyncTime = 0L
    private val SYNC_THROTTLE = 1000L // 1 segundo entre sincronizaciones
    
    // Callbacks para notificar cambios
    private val favoriteChangeCallbacks = mutableListOf<(String, Boolean) -> Unit>()
    private val cartChangeCallbacks = mutableListOf<(String, Int) -> Unit>()
    
    init {
        setupAuthListener()
    }
    
    private fun setupAuthListener() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d(TAG, "User logged in, starting sync")
                startSync()
            } else {
                Log.d(TAG, "User logged out, stopping sync")
                stopSync()
                clearData()
            }
        }
    }
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    private fun getFavoritesCollection() = firestore.collection("users")
        .document(getCurrentUserId() ?: "")
        .collection("favorites")
    
    private fun getCartCollection() = firestore.collection("users")
        .document(getCurrentUserId() ?: "")
        .collection("cart")
    
    /**
     * Inicia la sincronización de datos
     */
    fun startSync() {
        scope.launch {
            try {
                setupFavoritesListener()
                setupCartListener()
                Log.d(TAG, "Sync started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting sync", e)
            }
        }
    }
    
    /**
     * Detiene la sincronización de datos
     */
    fun stopSync() {
        try {
            favoritesListener?.remove()
            favoritesListener = null
            cartListener?.remove()
            cartListener = null
            Log.d(TAG, "Sync stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sync", e)
        }
    }
    
    /**
     * Limpia todos los datos
     */
    private fun clearData() {
        _favorites.postValue(emptyList())
        _cartItems.postValue(emptyList())
        _favoriteCount.postValue(0)
        _cartCount.postValue(0)
        favoritesCache = emptyList()
        cartCache = emptyList()
    }
    
    /**
     * Configura el listener de favoritos
     */
    private suspend fun setupFavoritesListener() {
        favoritesListener?.remove()
        
        favoritesListener = getFavoritesCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to favorites", error)
                    _favorites.postValue(emptyList())
                    _favoriteCount.postValue(0)
                    return@addSnapshotListener
                }
                
                val favorites = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                favoritesCache = favorites
                _favorites.postValue(favorites)
                _favoriteCount.postValue(favorites.size)
                
                Log.d(TAG, "Favorites updated: ${favorites.size} items")
            }
    }
    
    /**
     * Configura el listener del carrito
     */
    private suspend fun setupCartListener() {
        cartListener?.remove()
        
        cartListener = getCartCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to cart", error)
                    _cartItems.postValue(emptyList())
                    _cartCount.postValue(0)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CartItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                cartCache = items
                _cartItems.postValue(items)
                _cartCount.postValue(items.sumOf { it.quantity })
                
                Log.d(TAG, "Cart updated: ${items.size} items, total quantity: ${items.sumOf { it.quantity }}")
            }
    }
    
    /**
     * Agrega un producto a favoritos
     */
    suspend fun addToFavorites(product: Product): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSyncTime < SYNC_THROTTLE) {
                Log.d(TAG, "Sync throttled, skipping add to favorites")
                return false
            }
            
            val userId = getCurrentUserId() ?: return false
            getFavoritesCollection().document(product.id).set(product).await()
            lastSyncTime = currentTime
            
            // Notificar cambio
            notifyFavoriteChange(product.id, true)
            Log.d(TAG, "Added to favorites: ${product.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to favorites", e)
            false
        }
    }
    
    /**
     * Remueve un producto de favoritos
     */
    suspend fun removeFromFavorites(productId: String): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSyncTime < SYNC_THROTTLE) {
                Log.d(TAG, "Sync throttled, skipping remove from favorites")
                return false
            }
            
            val userId = getCurrentUserId() ?: return false
            getFavoritesCollection().document(productId).delete().await()
            lastSyncTime = currentTime
            
            // Notificar cambio
            notifyFavoriteChange(productId, false)
            Log.d(TAG, "Removed from favorites: $productId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from favorites", e)
            false
        }
    }
    
    /**
     * Agrega un producto al carrito
     */
    suspend fun addToCart(item: CartItem): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSyncTime < SYNC_THROTTLE) {
                Log.d(TAG, "Sync throttled, skipping add to cart")
                return false
            }
            
            val userId = getCurrentUserId() ?: return false
            val docRef = getCartCollection().document(item.id)
            val existing = docRef.get().await().toObject(CartItem::class.java)
            val newQuantity = (existing?.quantity ?: 0) + item.quantity
            docRef.set(item.copy(quantity = newQuantity)).await()
            lastSyncTime = currentTime
            
            // Notificar cambio
            notifyCartChange(item.id, newQuantity)
            Log.d(TAG, "Added to cart: ${item.name}, quantity: $newQuantity")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart", e)
            false
        }
    }
    
    /**
     * Remueve un producto del carrito
     */
    suspend fun removeFromCart(productId: String): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSyncTime < SYNC_THROTTLE) {
                Log.d(TAG, "Sync throttled, skipping remove from cart")
                return false
            }
            
            val userId = getCurrentUserId() ?: return false
            getCartCollection().document(productId).delete().await()
            lastSyncTime = currentTime
            
            // Notificar cambio
            notifyCartChange(productId, 0)
            Log.d(TAG, "Removed from cart: $productId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from cart", e)
            false
        }
    }
    
    /**
     * Actualiza la cantidad de un producto en el carrito
     */
    suspend fun updateCartQuantity(productId: String, quantity: Int): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSyncTime < SYNC_THROTTLE) {
                Log.d(TAG, "Sync throttled, skipping update cart quantity")
                return false
            }
            
            val userId = getCurrentUserId() ?: return false
            if (quantity <= 0) {
                getCartCollection().document(productId).delete().await()
                notifyCartChange(productId, 0)
            } else {
                getCartCollection().document(productId).update("quantity", quantity).await()
                notifyCartChange(productId, quantity)
            }
            lastSyncTime = currentTime
            
            Log.d(TAG, "Updated cart quantity: $productId, quantity: $quantity")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cart quantity", e)
            false
        }
    }
    
    /**
     * Limpia el carrito
     */
    suspend fun clearCart() {
        try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSyncTime < SYNC_THROTTLE) {
                Log.d(TAG, "Sync throttled, skipping clear cart")
                return
            }
            
            val userId = getCurrentUserId() ?: return
            val items = getCartCollection().get().await()
            for (doc in items.documents) {
                doc.reference.delete().await()
            }
            lastSyncTime = currentTime
            
            Log.d(TAG, "Cart cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cart", e)
        }
    }
    
    /**
     * Verifica si un producto está en favoritos
     */
    fun isFavorite(productId: String): Boolean {
        return favoritesCache.any { it.id == productId }
    }
    
    /**
     * Obtiene la cantidad de un producto en el carrito
     */
    fun getCartItemQuantity(productId: String): Int {
        return cartCache.find { it.id == productId }?.quantity ?: 0
    }
    
    /**
     * Registra un callback para cambios en favoritos
     */
    fun addFavoriteChangeListener(callback: (String, Boolean) -> Unit) {
        favoriteChangeCallbacks.add(callback)
    }
    
    /**
     * Remueve un callback de favoritos
     */
    fun removeFavoriteChangeListener(callback: (String, Boolean) -> Unit) {
        favoriteChangeCallbacks.remove(callback)
    }
    
    /**
     * Registra un callback para cambios en el carrito
     */
    fun addCartChangeListener(callback: (String, Int) -> Unit) {
        cartChangeCallbacks.add(callback)
    }
    
    /**
     * Remueve un callback del carrito
     */
    fun removeCartChangeListener(callback: (String, Int) -> Unit) {
        cartChangeCallbacks.remove(callback)
    }
    
    /**
     * Notifica cambios en favoritos
     */
    private fun notifyFavoriteChange(productId: String, isFavorite: Boolean) {
        favoriteChangeCallbacks.forEach { callback ->
            try {
                callback(productId, isFavorite)
            } catch (e: Exception) {
                Log.e(TAG, "Error in favorite change callback", e)
            }
        }
    }
    
    /**
     * Notifica cambios en el carrito
     */
    private fun notifyCartChange(productId: String, quantity: Int) {
        cartChangeCallbacks.forEach { callback ->
            try {
                callback(productId, quantity)
            } catch (e: Exception) {
                Log.e(TAG, "Error in cart change callback", e)
            }
        }
    }
    
    /**
     * Fuerza una sincronización manual
     */
    fun forceSync() {
        scope.launch {
            try {
                Log.d(TAG, "Forcing sync...")
                setupFavoritesListener()
                setupCartListener()
                lastSyncTime = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e(TAG, "Error forcing sync", e)
            }
        }
    }
    
    /**
     * Limpia todos los listeners y callbacks
     */
    fun cleanup() {
        stopSync()
        favoriteChangeCallbacks.clear()
        cartChangeCallbacks.clear()
        clearData()
        Log.d(TAG, "Cleanup completed")
    }
} 