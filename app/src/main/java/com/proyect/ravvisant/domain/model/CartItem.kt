package com.proyect.ravvisant.domain.model

import java.io.Serializable

// Modelo para un item del carrito
// Puede contener más campos según necesidades

data class CartItem(
    val id: String = "", // id del producto
    val name: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
) : Serializable 