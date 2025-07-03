package com.proyect.ravvisant.features.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.proyect.ravvisant.R
import com.proyect.ravvisant.core.firebase.FavoriteCountService
import com.proyect.ravvisant.core.firebase.CartCountService
import com.proyect.ravvisant.core.utils.BadgeBottomNavigationView
import com.proyect.ravvisant.features.auth.LoginActivity
import androidx.appcompat.app.AppCompatDelegate

class ProfileFragment : Fragment() {
    private val TAG = "ProfileFragment"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var authListener: FirebaseAuth.AuthStateListener? = null

    private var ivProfile: ImageView? = null
    private var tvUser: TextView? = null
    private var tvCorreoUser: TextView? = null
    
    // Elementos de favoritos
    private var tvFavo: TextView? = null
    private var tvFavoritosCount: TextView? = null
    
    // Elementos del carrito
    private var tvCar: TextView? = null

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            initializeViews(view)
            setupThemeSwitch(view)
            setupUserInfo()
            setupNavigation(view)
            setupLogout(view)
            observeFavoriteCount()
            observeCartCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
        }
    }
    
    private fun initializeViews(view: View) {
        try {
            val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
            val btnAction = view.findViewById<ImageButton>(R.id.btnAction)
            val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
            val Ubication = view.findViewById<CardView>(R.id.cvDirecciones)
            val cvExit = view.findViewById<CardView>(R.id.cvExit)
            
            // CardViews para navegación
            val cvShop = view.findViewById<CardView>(R.id.cvShop)
            val cvFavorits = view.findViewById<CardView>(R.id.cvFavorits)
            val cvFavoritos = view.findViewById<CardView>(R.id.cvFavoritos)

            ivProfile = view.findViewById(R.id.ivProfile)
            tvUser = view.findViewById(R.id.tvUser)
            tvCorreoUser = view.findViewById(R.id.tvCorreoUser)
            
            // Inicializar elementos de favoritos con verificación
            tvFavo = view.findViewById(R.id.tvFavo)
            tvFavoritosCount = view.findViewById(R.id.tvFavoritosCount)
            
            // Inicializar elementos del carrito
            tvCar = view.findViewById(R.id.tvCar)
            
            // Verificar que los elementos existen
            if (tvFavo == null) {
                Log.w(TAG, "tvFavo not found in layout")
            }
            if (tvFavoritosCount == null) {
                Log.w(TAG, "tvFavoritosCount not found in layout")
            }
            if (tvCar == null) {
                Log.w(TAG, "tvCar not found in layout")
            }

            toolbarTitle?.text = "Perfil"
            btnAction?.visibility = View.GONE

            if (isInBottomNavigation()) {
                toolbar?.navigationIcon = null
            } else {
                toolbar?.setNavigationOnClickListener {
                    findNavController().navigateUp()
                }
            }
            
            // Navegación a ubicación
            Ubication.setOnClickListener {
                findNavController().navigate(R.id.locationFragment)
            }
            
            // Navegación al carrito
            cvShop?.setOnClickListener {
                try {
                    // Usar el bottom navigation directamente
                    val activity = requireActivity()
                    val bottomNav = activity.findViewById<BadgeBottomNavigationView>(R.id.bottomNavigationView)
                    bottomNav.selectedItemId = R.id.cartFragment
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to cart", e)
                }
            }
            
            // Navegación a favoritos (desde el card pequeño)
            cvFavorits?.setOnClickListener {
                try {
                    // Usar el bottom navigation directamente
                    val activity = requireActivity()
                    val bottomNav = activity.findViewById<BadgeBottomNavigationView>(R.id.bottomNavigationView)
                    bottomNav.selectedItemId = R.id.favoriteFragment
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to favorites", e)
                }
            }
            
            // Navegación a favoritos (desde el card grande)
            cvFavoritos?.setOnClickListener {
                try {
                    // Usar el bottom navigation directamente
                    val activity = requireActivity()
                    val bottomNav = activity.findViewById<BadgeBottomNavigationView>(R.id.bottomNavigationView)
                    bottomNav.selectedItemId = R.id.favoriteFragment
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to favorites", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
        }
    }
    
    private fun setupThemeSwitch(view: View) {
        try {
            val switchTheme = view.findViewById<SwitchMaterial>(R.id.switchTheme)
            val ivTema = view.findViewById<ImageView>(R.id.ivTema)
            val tvTema = view.findViewById<TextView>(R.id.tvTema)

            val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

            AppCompatDelegate.setDefaultNightMode(savedMode)
            switchTheme.isChecked = savedMode == AppCompatDelegate.MODE_NIGHT_NO
            updateThemeIconAndText(ivTema, tvTema, switchTheme.isChecked)

            switchTheme.setOnCheckedChangeListener { _, isChecked ->
                val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
                AppCompatDelegate.setDefaultNightMode(mode)
                prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
                updateThemeIconAndText(ivTema, tvTema, isChecked)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up theme switch", e)
        }
    }
    
    private fun setupUserInfo() {
        try {
            // Actualiza datos de usuario
            updateUserInfo(auth.currentUser)

            // refrescar datos si cambiamos el usuario
            authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                updateUserInfo(firebaseAuth.currentUser)
            }
            auth.addAuthStateListener(authListener!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up user info", e)
        }
    }
    
    private fun setupNavigation(view: View) {
        try {
            val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
            if (isInBottomNavigation()) {
                toolbar?.navigationIcon = null
            } else {
                toolbar?.setNavigationOnClickListener {
                    findNavController().navigateUp()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation", e)
        }
    }
    
    private fun setupLogout(view: View) {
        try {
            val cvExit = view.findViewById<CardView>(R.id.cvExit)
            
            // CERRAR SESIÓN
            cvExit.setOnClickListener {
                // en Firebase
                auth.signOut()
                // en Google
                val googleSignInClient = GoogleSignIn.getClient(
                    requireContext(),
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                )
                googleSignInClient.signOut().addOnCompleteListener {
                    // a LoginActivity y limpiar
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up logout", e)
        }
    }
    
    private fun observeFavoriteCount() {
        try {
            // Asegurar que el contador esté cargado
            FavoriteCountService.loadFavoriteCount()
            
            FavoriteCountService.favoriteCount.observe(viewLifecycleOwner) { count ->
                updateFavoriteCount(count)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing favorite count", e)
            // Establecer valores por defecto en caso de error
            updateFavoriteCount(0)
        }
    }
    
    private fun updateFavoriteCount(count: Int) {
        try {
            tvFavo?.text = count.toString()
            tvFavoritosCount?.text = count.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating favorite count", e)
        }
    }
    
    private fun observeCartCount() {
        try {
            // Asegurar que el contador esté cargado
            CartCountService.loadCartCount()
            
            CartCountService.cartCount.observe(viewLifecycleOwner) { count ->
                updateCartCount(count)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing cart count", e)
            // Establecer valores por defecto en caso de error
            updateCartCount(0)
        }
    }
    
    private fun updateCartCount(count: Int) {
        try {
            tvCar?.text = count.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cart count", e)
        }
    }

    private fun updateUserInfo(user: FirebaseUser?) {
        try {
            tvUser?.text = user?.displayName ?: "Sin nombre"
            tvCorreoUser?.text = user?.email ?: "Sin correo"
            val photoUrl = user?.photoUrl
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.img_perfil)
                    .into(ivProfile!!)
            } else {
                ivProfile?.setImageResource(R.drawable.img_perfil)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user info", e)
        }
    }

    private fun updateThemeIconAndText(ivTema: ImageView, tvTema: TextView, isLight: Boolean) {
        try {
            if (isLight) {
                ivTema.setImageResource(R.drawable.ic_sun)
                tvTema.setText(R.string.theme_light)
            } else {
                ivTema.setImageResource(R.drawable.ic_moon)
                tvTema.setText(R.string.theme_dark)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating theme icon and text", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            // para evitar leaks
            authListener?.let { auth.removeAuthStateListener(it) }
            authListener = null
            ivProfile = null
            tvUser = null
            tvCorreoUser = null
            tvFavo = null
            tvFavoritosCount = null
            tvCar = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroyView", e)
        }
    }

    private fun isInBottomNavigation(): Boolean {
        return try {
            val navController = findNavController()
            navController.currentDestination?.id in setOf(
                R.id.homeFragment,
                R.id.searchFragment,
                R.id.cartFragment,
                R.id.favoriteFragment,
                R.id.profileFragment
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if in bottom navigation", e)
            false
        }
    }
}