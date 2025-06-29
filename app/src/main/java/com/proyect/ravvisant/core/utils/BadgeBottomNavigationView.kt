package com.proyect.ravvisant.core.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proyect.ravvisant.R
import com.proyect.ravvisant.core.firebase.CartCountService
import com.proyect.ravvisant.core.firebase.FavoriteCountService

class BadgeBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private val TAG = "BadgeBottomNavView"
    private var favoriteBadge: BadgeDrawable? = null
    private var cartBadge: BadgeDrawable? = null
    private var lifecycleOwner: LifecycleOwner? = null

    fun setupBadge(lifecycleOwner: LifecycleOwner) {
        Log.d(TAG, "Setting up badges")
        this.lifecycleOwner = lifecycleOwner
        
        // Crear badges para favoritos y carrito
        favoriteBadge = getOrCreateBadge(R.id.favoriteFragment)
        cartBadge = getOrCreateBadge(R.id.cartFragment)
        
        Log.d(TAG, "Badges created: favorite=${favoriteBadge != null}, cart=${cartBadge != null}")
        
        // Observar cambios en el contador de favoritos
        FavoriteCountService.favoriteCount.observe(lifecycleOwner) { count ->
            Log.d(TAG, "Favorite count changed: $count")
            updateFavoriteBadge(count)
        }
        
        // Observar cambios en el contador del carrito
        CartCountService.cartCount.observe(lifecycleOwner) { count ->
            Log.d(TAG, "Cart count changed: $count")
            updateCartBadge(count)
        }
    }
    
    private fun updateFavoriteBadge(count: Int) {
        Log.d(TAG, "Updating favorite badge with count: $count")
        favoriteBadge?.let { badge ->
            if (count > 0) {
                badge.isVisible = true
                badge.number = count
                Log.d(TAG, "Favorite badge made visible with number: $count")
            } else {
                badge.isVisible = false
                Log.d(TAG, "Favorite badge made invisible")
            }
        } ?: run {
            Log.w(TAG, "Favorite badge drawable is null")
        }
    }
    
    private fun updateCartBadge(count: Int) {
        Log.d(TAG, "Updating cart badge with count: $count")
        cartBadge?.let { badge ->
            if (count > 0) {
                badge.isVisible = true
                badge.number = count
                Log.d(TAG, "Cart badge made visible with number: $count")
            } else {
                badge.isVisible = false
                Log.d(TAG, "Cart badge made invisible")
            }
        } ?: run {
            Log.w(TAG, "Cart badge drawable is null")
        }
    }
    
    fun refreshBadges() {
        Log.d(TAG, "Refreshing badges")
        FavoriteCountService.loadFavoriteCount()
        CartCountService.loadCartCount()
    }
} 