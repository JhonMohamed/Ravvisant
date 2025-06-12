package com.proyect.ravvisant.features.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyect.ravvisant.databinding.ItemProductBinding
import com.proyect.ravvisant.domain.Product
import com.proyect.ravvisant.R
import com.proyect.ravvisant.utils.diff.ProductDiffCallback

class HomeProductAdapter(private val callback: ProductClickCallback? = null) :
    ListAdapter<Product, HomeProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeProductViewHolder {
        val binding = DataBindingUtil.inflate<ItemProductBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_product,
            parent,
            false
        )
        binding.callback = callback
        return HomeProductViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: HomeProductViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

}