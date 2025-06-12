package com.proyect.ravvisant.features.home.adapters

import androidx.recyclerview.widget.RecyclerView
import com.proyect.ravvisant.databinding.ItemProductBinding
import com.proyect.ravvisant.domain.Product

class HomeProductViewHolder(private val binding: ItemProductBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(product: Product) {
        binding.product = product
        binding.executePendingBindings();
    }
}