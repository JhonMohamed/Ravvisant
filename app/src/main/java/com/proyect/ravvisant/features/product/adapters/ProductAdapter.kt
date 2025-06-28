package com.proyect.ravvisant.features.product.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyect.ravvisant.databinding.ItemProductGridBinding
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.core.common.ProductClickCallback
import com.proyect.ravvisant.core.utils.diff.ProductDiffCallback

class ProductAdapter(private val callback: ProductClickCallback) :
    ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    inner class ProductViewHolder(private val binding: ItemProductGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.product = product
            binding.callback = callback

            // Opcional: Si usas Glide u otra lógica adicional
            binding.root.setOnClickListener {
                callback.onProductClick(product)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}