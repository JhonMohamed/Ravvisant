package com.proyect.ravvisant.features.product.adapters

import androidx.recyclerview.widget.RecyclerView
import com.proyect.ravvisant.databinding.ItemProductThumbnailBinding

class ThumbnailViewHolder(
    private val binding: ItemProductThumbnailBinding,
    private val onThumbnailClick: (position: Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.root.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onThumbnailClick(position)
            }
        }
    }

    fun bind(imageUrl: String, isSelected: Boolean) {
        binding.imageUrl = imageUrl
        binding.selected = isSelected
        binding.executePendingBindings()
    }
}