package com.example.wellhoney.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.wellhoney.R
import com.example.wellhoney.data.models.HoneyProduct
import com.example.wellhoney.databinding.FragmentProductDetailBinding
import com.example.wellhoney.presentation.viewmodel.CartViewModel

class ProductDetailFragment : Fragment() {

    companion object {
        private const val ARG_PRODUCT = "arg_product"

        fun newInstance(product: HoneyProduct): ProductDetailFragment {
            val fragment = ProductDetailFragment()
            val args = Bundle().apply {
                putParcelable(ARG_PRODUCT, product)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var product: HoneyProduct? = null
    private lateinit var cartViewModel: CartViewModel
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = arguments?.getParcelable(ARG_PRODUCT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]

        product?.let { prod ->
            binding.tvName.text = prod.name
            binding.tvDescription.text = prod.description

            binding.itemPrice500ml.text = "${prod.price_500ml}  ₽"
            binding.itemPrice1l.text = "${prod.price_1l}  ₽"

            Glide.with(binding.imageProduct.context)
                .load(product!!.img)
                .placeholder(R.drawable.no_photo)
                .error(R.drawable.no_photo)
                .into(binding.imageProduct)

            // Инициализация состояния для 500мл и 1л
            val initialQuantity500 = cartViewModel.getQuantity(prod.id, "500ml") ?: 0
            val initialQuantity1 = cartViewModel.getQuantity(prod.id, "1l") ?: 0
            updateUI(prod.id, "500ml", initialQuantity500)
            updateUI(prod.id, "1l", initialQuantity1)

            // Добавить в корзину 500мл
            binding.btnAddToCart500.setOnClickListener {
                cartViewModel.addToCart(prod.id, "500ml")
                val qty = cartViewModel.getQuantity(prod.id, "500ml") ?: 0
                updateUI(prod.id, "500ml", qty)
            }

            // Добавить в корзину 1л
            binding.btnAddToCart1.setOnClickListener {
                cartViewModel.addToCart(prod.id, "1l")
                val qty = cartViewModel.getQuantity(prod.id, "1l") ?: 0
                updateUI(prod.id, "1l", qty)
            }

            // Для 500 мл
            binding.btnIncrease500.setOnClickListener {
                val current = cartViewModel.getQuantity(product!!.id, "500ml") ?: 0
                cartViewModel.increaseQuantity(product!!.id, "500ml")
                updateUI(product!!.id, "500ml", current + 1)
            }

            binding.btnDecrease500.setOnClickListener {
                val current = cartViewModel.getQuantity(product!!.id, "500ml") ?: 0
                if (current > 1) {
                    cartViewModel.decreaseQuantity(product!!.id, "500ml")
                    updateUI(product!!.id, "500ml", current - 1)
                } else {
                    cartViewModel.removeFromCart(product!!.id, "500ml")
                    updateUI(product!!.id, "500ml", 0)
                }
            }

// Для 1 л
            binding.btnIncrease1.setOnClickListener {
                val current = cartViewModel.getQuantity(product!!.id, "1l") ?: 0
                cartViewModel.increaseQuantity(product!!.id, "1l")
                updateUI(product!!.id, "1l", current + 1)
            }

            binding.btnDecrease1.setOnClickListener {
                val current = cartViewModel.getQuantity(product!!.id, "1l") ?: 0
                if (current > 1) {
                    cartViewModel.decreaseQuantity(product!!.id, "1l")
                    updateUI(product!!.id, "1l", current - 1)
                } else {
                    cartViewModel.removeFromCart(product!!.id, "1l")
                    updateUI(product!!.id, "1l", 0)
                }
            }

            // Наблюдение за LiveData корзины
            cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
                val qty500 =
                    items.find { it.productId == prod.id && it.volume == "500ml" }?.quantity ?: 0
                val qty1 =
                    items.find { it.productId == prod.id && it.volume == "1l" }?.quantity ?: 0
                updateUI(prod.id, "500ml", qty500)
                updateUI(prod.id, "1l", qty1)
            }
        }
    }

    // Универсальная функция обновления UI
    private fun updateUI(productId: String, volume: String, quantity: Int) {
        when (volume) {
            "500ml" -> {
                if (quantity > 0) {
                    binding.tvQuantity500.text = quantity.toString()
                    binding.btnAddToCart500.visibility = View.GONE
                    binding.quantityLayout500.visibility = View.VISIBLE
                } else {
                    binding.btnAddToCart500.visibility = View.VISIBLE
                    binding.quantityLayout500.visibility = View.GONE
                }
            }

            "1l" -> {
                if (quantity > 0) {
                    binding.tvQuantity1.text = quantity.toString()
                    binding.btnAddToCart1.visibility = View.GONE
                    binding.quantityLayout1.visibility = View.VISIBLE
                } else {
                    binding.btnAddToCart1.visibility = View.VISIBLE
                    binding.quantityLayout1.visibility = View.GONE
                }
            }
        }
    }
}
