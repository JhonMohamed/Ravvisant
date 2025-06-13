package com.proyect.ravvisant.features.product.adapters

import androidx.recyclerview.widget.RecyclerView
import com.proyect.ravvisant.databinding.ItemProductGridBinding
import com.proyect.ravvisant.domain.Product
import com.proyect.ravvisant.features.home.adapters.ProductClickCallback

class ProductViewHolder(private val binding: ItemProductGridBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(product: Product, callback: ProductClickCallback) {
        binding.product = product
        binding.callback = callback

        binding.root.setOnClickListener {
            callback.onProductClick(product)
        }

        binding.executePendingBindings()
    }
}