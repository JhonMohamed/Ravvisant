package com.proyect.ravvisant.utils.diff

import androidx.recyclerview.widget.DiffUtil
import com.proyect.ravvisant.data.model.Category

class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(
        oldItem: Category,
        newItem: Category
    ): Boolean =
        oldItem.id == newItem.id


    override fun areContentsTheSame(
        oldItem: Category,
        newItem: Category
    ): Boolean =
        oldItem == newItem


}