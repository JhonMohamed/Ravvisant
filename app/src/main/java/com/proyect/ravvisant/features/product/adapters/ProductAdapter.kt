package com.proyect.ravvisant.features.product.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.proyect.ravvisant.databinding.ItemProductGridBinding
import com.proyect.ravvisant.domain.Product
import com.proyect.ravvisant.features.home.adapters.ProductClickCallback
import com.proyect.ravvisant.utils.diff.ProductDiffCallback

class ProductAdapter(private val callback: ProductClickCallback) :
    ListAdapter<Product, ProductViewHolder>(ProductDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val binding = ItemProductGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position), callback)
    }

}