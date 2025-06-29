package com.proyect.ravvisant.features.favorites.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyect.ravvisant.databinding.ItemProductGridBinding
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.core.common.ProductClickCallback
import com.proyect.ravvisant.core.utils.diff.ProductDiffCallback

class FavoriteAdapter(
    private val callback: ProductClickCallback,
    private val onRemoveFavorite: (Product) -> Unit
) : ListAdapter<Product, FavoriteAdapter.FavoriteViewHolder>(ProductDiffCallback()) {

    inner class FavoriteViewHolder(private val binding: ItemProductGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            // Asegurar que el producto se muestre como favorito
            val productWithFavorite = product.copy(isFavorite = true)
            binding.product = productWithFavorite
            binding.callback = object : ProductClickCallback {
                override fun onFavoriteClick(product: Product) {
                    // En la pantalla de favoritos, el botón de favorito elimina el producto
                    onRemoveFavorite(product)
                }

                override fun onAddToCartClick(product: Product) {
                    callback.onAddToCartClick(product)
                }

                override fun onProductClick(product: Product) {
                    callback.onProductClick(product)
                }
            }

            binding.root.setOnClickListener {
                callback.onProductClick(product)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemProductGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
} 