package com.proyect.ravvisant.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.proyect.ravvisant.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var edtNombre: TextInputEditText
    private lateinit var edtCorreo: TextInputEditText
    private lateinit var edtPassword: TextInputEditText
    private lateinit var edtConfirmPassword: TextInputEditText
    private lateinit var btnCreateAccount: MaterialButton
    private lateinit var btnLoginBack: MaterialButton
    private lateinit var tvInitialSesion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        edtNombre = findViewById(R.id.edtnombre)
        edtCorreo = findViewById(R.id.edtcorreo)
        edtPassword = findViewById(R.id.edtpassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        btnLoginBack = findViewById(R.id.btnLoginBack)
        tvInitialSesion = findViewById(R.id.tvInitialSesion)

        btnCreateAccount.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val correo = edtCorreo.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val confirmPassword = edtConfirmPassword.text.toString().trim()

            if (nombre.isEmpty()) {
                edtNombre.error = "Ingrese su nombre"
                return@setOnClickListener
            }
            if (correo.isEmpty()) {
                edtCorreo.error = "Ingrese su correo"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                edtPassword.error = "Ingrese su contraseña"
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                edtConfirmPassword.error = "Confirme su contraseña"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                edtConfirmPassword.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }
            if (password.length < 6) {
                edtPassword.error = "La contraseña debe tener al menos 6 caracteres"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(nombre)
                            .build()
                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            Toast.makeText(this, "Registro exitoso. Inicia sesión.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        btnLoginBack.setOnClickListener {
            finish()
        }

        tvInitialSesion.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}