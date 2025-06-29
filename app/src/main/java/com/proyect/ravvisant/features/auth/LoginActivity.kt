package com.proyect.ravvisant.features.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.proyect.ravvisant.MainActivity
import com.proyect.ravvisant.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginActivity", "GoogleSignIn resultCode: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                Log.d("LoginActivity", "GoogleSignIn account: $account")
                if (account?.idToken != null) {
                    Log.d("LoginActivity", "ID Token obtenido: ${account.idToken}")
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { authTask ->
                            if (authTask.isSuccessful) {
                                Toast.makeText(this, "Inicio exitoso", Toast.LENGTH_SHORT).show()
                                goToMain()
                            } else {
                                Log.e("LoginActivity", "Error con Google: ${authTask.exception}")
                                Toast.makeText(this, "Error con Google: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Log.e("LoginActivity", "No se pudo obtener el idToken de Google.")
                    Toast.makeText(this, "No se pudo obtener el idToken de Google.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error en login Google: ${e.message}")
                Toast.makeText(this, "Error en login Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("LoginActivity", "Resultado de Google Sign-In cancelado o fallido")
            Toast.makeText(this, "Login cancelado o fallido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnGoogle = findViewById<MaterialButton>(R.id.btnGoogle)
        val edtCorreo = findViewById<TextInputEditText>(R.id.edtcorreo)
        val edtPassword = findViewById<TextInputEditText>(R.id.edtpassword)
        val tvRegistrate = findViewById<android.widget.TextView>(R.id.tvRegistrate)

        tvRegistrate.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = edtCorreo.text?.toString()?.trim() ?: ""
            val password = edtPassword.text?.toString()?.trim() ?: ""
            if (validateInputs(email, password, edtCorreo, edtPassword)) {
                signInWithEmail(email, password)
            }
        }

        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun validateInputs(email: String, password: String, edtCorreo: TextInputEditText, edtPassword: TextInputEditText): Boolean {
        var valid = true

        edtCorreo.apply {
            error = if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                valid = false
                "Correo inválido."
            } else null
        }

        edtPassword.apply {
            error = if (password.isEmpty() || password.length < 6) {
                valid = false
                "Contraseña muy corta."
            } else null
        }

        return valid
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goToMain()
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}