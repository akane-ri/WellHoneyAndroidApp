package com.example.wellhoney.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.wellhoney.data.models.HoneyProduct
import com.example.wellhoney.databinding.FragmentOrderDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderDetailsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentOrderDetailsBinding? = null
    private val binding get() = _binding!!

    private var orderItems: Map<String, Map<String, Int>> = emptyMap()
    private var productsMap: Map<String, HoneyProduct> = emptyMap()

    companion object {
        private const val ARG_ORDER_ITEMS = "arg_order_items"
        private const val ARG_PRODUCTS_MAP = "arg_products_map"

        fun newInstance(
            orderItems: Map<String, Map<String, Int>>,
            productsMap: Map<String, HoneyProduct>
        ): OrderDetailsFragment {
            val fragment = OrderDetailsFragment()
            val bundle = Bundle()
            bundle.putSerializable(ARG_ORDER_ITEMS, HashMap(orderItems))
            bundle.putSerializable(ARG_PRODUCTS_MAP, HashMap(productsMap))
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем сериализованные данные через HashMap
        @Suppress("UNCHECKED_CAST")
        orderItems = (requireArguments().getSerializable(ARG_ORDER_ITEMS) as? HashMap<String, HashMap<String, Int>>)
            ?: emptyMap()

        @Suppress("UNCHECKED_CAST")
        productsMap = (requireArguments().getSerializable(ARG_PRODUCTS_MAP) as? HashMap<String, HoneyProduct>)
            ?: emptyMap()

        binding.orderItemsContainer.removeAllViews()
        var total = 0

        // Проходим по каждому продукту и каждому объёму
        orderItems.forEach { (productId, volumesMap) ->
            val product = productsMap[productId]
            if (product != null) {
                volumesMap.forEach { (volume, quantity) ->
                    if (quantity <= 0) return@forEach

                    val price = when(volume) {
                        "500ml" -> product.price_500ml
                        "1l" -> product.price_1l
                        else -> 0
                    }
                    val productTotal = price * quantity
                    total += productTotal

                    val textView = TextView(requireContext()).apply {
                        text = "${product.name} — $productTotal ₽ ($quantity x $volume)"
                        textSize = 16f
                        setPadding(0, 8, 0, 8)
                    }
                    binding.orderItemsContainer.addView(textView)
                }
            }
        }

        binding.orderTotal.text = "Итого: $total ₽"
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

