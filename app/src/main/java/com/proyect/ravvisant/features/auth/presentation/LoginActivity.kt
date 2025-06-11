package com.proyect.ravvisant.features.auth.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.proyect.ravvisant.MainActivity
import com.proyect.ravvisant.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tvRegister = findViewById<TextView>(R.id.tvRegistrate)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        btnLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}