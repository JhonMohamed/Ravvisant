package com.proyect.ravvisant.domain

data class Product(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val price: Double = 0.0,
    val rating: Float = 0f,
    val stock: Int = 0,
    val imageUrl: String = "",
    val isFavorite: Boolean = false
)