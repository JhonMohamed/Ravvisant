package com.proyect.ravvisant.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.proyect.ravvisant.R

class RegisterActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        val tvHaveAccount=findViewById<TextView>(R.id.tvInitialSesion)
        val btnLoginBack=findViewById<Button>(R.id.btnLoginBack)

        tvHaveAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        btnLoginBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}