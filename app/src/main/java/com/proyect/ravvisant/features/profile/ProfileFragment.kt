package com.proyect.ravvisant.features.profile

import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.proyect.ravvisant.R
import com.proyect.ravvisant.features.auth.LoginActivity
import androidx.lifecycle.Observer
import com.proyect.ravvisant.core.firebase.CartCountService
import com.proyect.ravvisant.core.firebase.FavoriteCountService

class ProfileFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var authListener: FirebaseAuth.AuthStateListener? = null

    private var ivProfile: ImageView? = null
    private var tvUser: TextView? = null
    private var tvCorreoUser: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        val btnAction = view.findViewById<ImageButton>(R.id.btnAction)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val Ubication = view.findViewById<CardView>(R.id.cvDirecciones)
        val cvExit = view.findViewById<CardView>(R.id.cvExit)
        val cvShop = view.findViewById<CardView>(R.id.cvShop)
        val cvFavorits = view.findViewById<CardView>(R.id.cvFavorits)
        val cvFavoritos = view.findViewById<CardView>(R.id.cvFavoritos)

        ivProfile = view.findViewById(R.id.ivProfile)
        tvUser = view.findViewById(R.id.tvUser)
        tvCorreoUser = view.findViewById(R.id.tvCorreoUser)

        // Contadores dinámicos
        val tvCar = view.findViewById<TextView>(R.id.tvCar)
        val tvFavo = view.findViewById<TextView>(R.id.tvFavo)
        val tvFavoritosCount = view.findViewById<TextView>(R.id.tvFavoritosCount)
        CartCountService.loadCartCount()
        FavoriteCountService.loadFavoriteCount()
        CartCountService.cartCount.observe(viewLifecycleOwner, Observer { count ->
            tvCar.text = count.toString()
        })
        FavoriteCountService.favoriteCount.observe(viewLifecycleOwner, Observer { count ->
            tvFavo.text = count.toString()
            tvFavoritosCount.text = count.toString()
        })

        // Actualiza datos de usuario
        updateUserInfo(auth.currentUser)

        // refrescar datos si cambiamos el usuario
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            updateUserInfo(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authListener!!)

        Ubication.setOnClickListener {
            findNavController().navigate(R.id.locationFragment)
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

        cvShop.setOnClickListener {
            findNavController().navigate(R.id.cartFragment)
        }
        cvFavorits.setOnClickListener {
            findNavController().navigate(R.id.favoriteFragment)
        }
        cvFavoritos.setOnClickListener {
            findNavController().navigate(R.id.favoriteFragment)
        }
    }

    private fun updateUserInfo(user: FirebaseUser?) {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // para evitar leaks
        authListener?.let { auth.removeAuthStateListener(it) }
        authListener = null
        ivProfile = null
        tvUser = null
        tvCorreoUser = null
    }

    private fun isInBottomNavigation(): Boolean {
        val navController = findNavController()
        return navController.currentDestination?.id in setOf(
            R.id.homeFragment,
            R.id.searchFragment,
            R.id.cartFragment,
            R.id.favoriteFragment,
            R.id.profileFragment
        )
    }
}