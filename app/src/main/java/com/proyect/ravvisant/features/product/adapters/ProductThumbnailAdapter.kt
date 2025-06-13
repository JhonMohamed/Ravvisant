package com.proyect.ravvisant.features.product.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.proyect.ravvisant.databinding.ItemProductThumbnailBinding

class ProductThumbnailAdapter(
    private val onThumbnailClick: (position: Int) -> Unit
) : RecyclerView.Adapter<ThumbnailViewHolder>() {

    private val imageUrls = mutableListOf<String>()
    private var selectedPosition = 0

    fun setImages(newImages: List<String>) {
        imageUrls.clear()
        imageUrls.addAll(newImages)
        notifyDataSetChanged()
    }

    fun selectImage(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val binding = ItemProductThumbnailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ThumbnailViewHolder(binding, onThumbnailClick)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        holder.bind(imageUrls[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = imageUrls.size
}