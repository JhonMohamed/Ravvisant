package com.proyect.ravvisant.features.categories

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.proyect.ravvisant.data.model.Category
import com.proyect.ravvisant.databinding.ItemCategoryBinding

class CategoryViewHolder(private val binding: ItemCategoryBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(category: Category) {
        binding.category = category
        binding.executePendingBindings()
    }

}