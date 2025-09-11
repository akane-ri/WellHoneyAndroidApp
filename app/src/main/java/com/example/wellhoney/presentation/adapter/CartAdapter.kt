package com.example.wellhoney.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wellhoney.R
import com.example.wellhoney.data.models.CartItem
import com.example.wellhoney.data.models.HoneyProduct
import com.example.wellhoney.databinding.ItemCartBinding

class CartAdapter(
    private val products: List<HoneyProduct>,
    private val cartItems: List<CartItem>,
    private val onQuantityChange: (productId: String, volume: String, newQuantity: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        val product = products.find { it.id == cartItem.productId } ?: return

        holder.binding.apply {
            var price = 0
            when (cartItem.volume) {
                "500ml" -> {
                    price = product.price_500ml
                    textProductName.text = product.name + " 500мл"
                }
                "1l" -> {
                    price = product.price_1l
                    textProductName.text = product.name + " 1л"
                }
                else -> textProductName.text = product.name
            }

            textQuantity.text = cartItem.quantity.toString()


            textPrice.text = "${price * cartItem.quantity} ₽"

            Glide.with(imageProduct.context)
                .load(product.img)
                .placeholder(R.drawable.no_photo)
                .error(R.drawable.no_photo)
                .into(imageProduct)

            buttonIncrease.setOnClickListener {
                onQuantityChange(cartItem.productId, cartItem.volume, cartItem.quantity + 1)
            }

            buttonDecrease.setOnClickListener {
                val newQty = cartItem.quantity - 1
                onQuantityChange(cartItem.productId, cartItem.volume, newQty)
            }

            buttonRemove.setOnClickListener {
                onQuantityChange(cartItem.productId, cartItem.volume, 0)
            }
        }
    }

    override fun getItemCount() = cartItems.size
}
