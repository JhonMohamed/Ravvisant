package com.proyect.ravvisant.features.home.adapters

import com.proyect.ravvisant.domain.Product

interface ProductClickCallback {
    fun onFavoriteClick(product: Product)
    fun onAddToCartClick(product: Product)
    fun onProductClick(product: Product)
}