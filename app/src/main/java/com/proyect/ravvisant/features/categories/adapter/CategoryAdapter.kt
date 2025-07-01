package com.proyect.ravvisant.features.categories.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.proyect.ravvisant.core.utils.diff.CategoryDiffCallback
import com.proyect.ravvisant.databinding.ItemCategoryBinding
import com.proyect.ravvisant.domain.model.Category
import com.proyect.ravvisant.features.categories.CategoryViewHolder

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit // Callback que recibimos como parámetro
) : ListAdapter<Category, CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)

        // Configurar el click listener en el item
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }
}