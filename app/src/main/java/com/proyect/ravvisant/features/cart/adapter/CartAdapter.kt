package com.proyect.ravvisant.features.cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.R
import com.proyect.ravvisant.databinding.ItemCartProductBinding
import com.proyect.ravvisant.domain.model.CartItem

class CartAdapter(
    private val onQuantityChanged: (String, Int) -> Unit,
    private val onRemoveItem: (String) -> Unit,
    private val onQuantityChangeFailed: (String) -> Unit = {}
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemCartProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.apply {
                productName.text = item.name
                productPrice.text = "$${item.price}"
                quantityText.text = item.quantity.toString()
                // Consultar el stock real en Firestore
                val db = FirebaseFirestore.getInstance()
                db.collection("products").document(item.id).get()
                    .addOnSuccessListener { doc ->
                        val stock = doc.getLong("stock")?.toInt() ?: 0
                        stockText.text = "Stock: $stock"
                    }
                    .addOnFailureListener {
                        stockText.text = "Stock: -"
                    }
                // Mostrar el total por producto
                val total = item.price * item.quantity
                productTotal.text = "Total: $${"%.2f".format(total)}"

                // Cargar imagen del producto
                Glide.with(productImage)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.img_background_gray)
                    .error(R.drawable.img_background_gray)
                    .into(productImage)

                // Botones de cantidad
                btnIncrease.setOnClickListener {
                    onQuantityChanged(item.id, item.quantity + 1)
                }

                btnDecrease.setOnClickListener {
                    if (item.quantity > 1) {
                        onQuantityChanged(item.id, item.quantity - 1)
                    } else {
                        onRemoveItem(item.id)
                    }
                }

                // Botón eliminar
                btnRemove.setOnClickListener {
                    onRemoveItem(item.id)
                }
            }
        }
    }

    private class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
} 