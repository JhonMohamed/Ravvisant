package com.proyect.ravvisant.features.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.proyect.ravvisant.data.model.Category
import com.proyect.ravvisant.databinding.ItemCategoryBinding
import com.proyect.ravvisant.utils.diff.CategoryDiffCallback

class CategoryAdapter : ListAdapter<Category, CategoryViewHolder>(CategoryDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        val bindings =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(bindings)
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}