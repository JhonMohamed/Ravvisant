package com.proyect.ravvisant

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.proyect.ravvisant.core.utils.BadgeBottomNavigationView
import com.proyect.ravvisant.core.firebase.FavoriteCountService
import com.proyect.ravvisant.core.firebase.CartCountService

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BadgeBottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setupWithNavController(navController)
        
        // Configurar los badges del contador de favoritos y carrito
        bottomNav.setupBadge(this)
        
        // Cargar los contadores iniciales
        FavoriteCountService.loadFavoriteCount()
        CartCountService.loadCartCount()
        
        // Método temporal para probar los badges
        testBadges(bottomNav)
    }
    
    private fun testBadges(bottomNav: BadgeBottomNavigationView) {
        // Probar el badge de favoritos después de 2 segundos
        bottomNav.postDelayed({
            Log.d(TAG, "Testing favorite badge with count 3")
            FavoriteCountService.updateCount(3)
        }, 2000)
        
        // Probar el badge del carrito después de 4 segundos
        bottomNav.postDelayed({
            Log.d(TAG, "Testing cart badge with count 5")
            CartCountService.updateCount(5)
        }, 4000)
        
        // Ocultar los badges después de 8 segundos
        bottomNav.postDelayed({
            Log.d(TAG, "Testing badges with count 0")
            FavoriteCountService.updateCount(0)
            CartCountService.updateCount(0)
        }, 8000)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp()
    }
    
    override fun onResume() {
        super.onResume()
        // Refrescar los badges cuando se regresa a la actividad
        findViewById<BadgeBottomNavigationView>(R.id.bottomNavigationView).refreshBadges()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Detener los listeners de los contadores
        FavoriteCountService.stopListening()
        CartCountService.stopListening()
    }
}