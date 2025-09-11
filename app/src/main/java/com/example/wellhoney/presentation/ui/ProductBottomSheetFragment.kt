package com.example.wellhoney.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wellhoney.R
import com.example.wellhoney.data.models.HoneyProduct
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProductBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_PRODUCT = "arg_product"

        fun newInstance(product: HoneyProduct): ProductBottomSheetFragment {
            val fragment = ProductBottomSheetFragment()
            val args = Bundle().apply {
                putParcelable(ARG_PRODUCT, product)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bottom_sheet_wrapper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val product = arguments?.getParcelable<HoneyProduct>(ARG_PRODUCT)
            ?: return

        // Встраиваем DetailFragment внутрь контейнера
        childFragmentManager.beginTransaction()
            .replace(
                R.id.bottomSheetContainer,
                ProductDetailFragment.newInstance(product)
            )
            .commit()
    }
}