package com.proyect.ravvisant.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val price: Double = 0.0,
    val rating: Float = 0f,
    val stock: Int = 0,
    val imageUrls: List<String> = listOf(),
    val description: String = "",
    val isFavorite: Boolean = false,
    val categoryId: String = ""
) {
    val imageUrl: String
        get() = if (imageUrls.isNotEmpty()) imageUrls[0] else ""
}