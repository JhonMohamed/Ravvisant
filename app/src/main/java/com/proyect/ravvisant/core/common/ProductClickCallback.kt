package com.proyect.ravvisant.core.common

import com.proyect.ravvisant.domain.model.Product

interface ProductClickCallback {
    fun onFavoriteClick(product: Product)
    fun onAddToCartClick(product: Product)
    fun onProductClick(product: Product)
}