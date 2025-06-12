package com.proyect.ravvisant.utils.diff

import androidx.recyclerview.widget.DiffUtil
import com.proyect.ravvisant.domain.Product

class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(
        oldItem: Product,
        newItem: Product
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: Product,
        newItem: Product
    ): Boolean = oldItem == newItem

}