package com.example.wellhoney.presentation.activity

import com.example.wellhoney.presentation.adapter.CartAdapter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wellhoney.data.models.CartItem
import com.example.wellhoney.data.models.HoneyProduct
import com.example.wellhoney.databinding.ActivityCartBinding
import com.example.wellhoney.presentation.viewmodel.CartViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartViewModel: CartViewModel
    private val gson = Gson()

    private var products = listOf<HoneyProduct>()
    private var cartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
        products = loadProductsFromJson()

        cartViewModel.setProducts(products)

        binding.buttonCheckout.setOnClickListener {
            cartViewModel.placeOrder()
        }

        cartViewModel.orderResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Заказ успешно оформлен!", Toast.LENGTH_SHORT).show()
            }
            result.onFailure { e ->
                Toast.makeText(this, "Ошибка оформления заказа: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        setupRecyclerView()
        observeCartItems()
    }

    private fun setupRecyclerView() {
        binding.recyclerCart.layoutManager = LinearLayoutManager(this)
        binding.recyclerCart.adapter = CartAdapter(products, cartItems) { id, volume, qty ->
            cartViewModel.updateCartItem(id, volume, qty)
        }
    }

    private fun loadProductsFromJson(): List<HoneyProduct> {
        val json = assets.open("honey_products.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<HoneyProduct>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun observeCartItems() {
        cartViewModel.cartItems.observe(this) { items ->
            cartItems.clear()
            cartItems.addAll(items)
            updateUI()
        }
    }

    private fun updateUI() {
        if (cartItems.isEmpty()) {
            binding.recyclerCart.visibility = View.GONE
            binding.textEmptyCart.visibility = View.VISIBLE
        } else {
            binding.recyclerCart.visibility = View.VISIBLE
            binding.textEmptyCart.visibility = View.GONE
        }
        binding.recyclerCart.adapter?.notifyDataSetChanged()
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val total = cartItems.sumOf { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            (product?.price_500ml ?: 0) * cartItem.quantity
        }
        binding.textTotalPrice.text = "Итого: $total ₽"
    }
}