package com.example.wellhoney.presentation.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wellhoney.R
import com.example.wellhoney.data.HoneyRepository
import com.example.wellhoney.databinding.ActivityCatalogBinding
import com.example.wellhoney.presentation.adapter.CatalogAdapter
import com.example.wellhoney.presentation.ui.ProductBottomSheetFragment
import com.example.wellhoney.presentation.ui.ProductDetailFragment
import com.example.wellhoney.presentation.viewmodel.CartViewModel

class CatalogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatalogBinding
    private val cartViewModel : CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = HoneyRepository(this)
        val products = repository.loadHoneyProducts()

        cartViewModel.cartItems.observe(this) { items ->
            val count = items.sumOf { it.quantity }
            updateCartCount(count)
        }

        val adapter = CatalogAdapter(products) { selectedProduct ->
            val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            if (isLandscape) {
                // Найти корневой контейнер с id catalog_root
                val root = findViewById<ViewGroup>(R.id.catalog_root)

                // Проверяем, есть ли уже detailFragmentContainer
                var detailContainer = findViewById<FrameLayout?>(R.id.detailFragmentContainer)
                if (detailContainer == null) {
                    // Создаем контейнер для фрагмента
                    detailContainer = FrameLayout(this).apply {
                        id = R.id.detailFragmentContainer
                        layoutParams = LinearLayout.LayoutParams(
                            root.width / 2,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(ContextCompat.getColor(this@CatalogActivity, android.R.color.white))
                    }
                    root.addView(detailContainer)

                    // Меняем ширину списка на половину экрана
                    val listParams = binding.rvCatalog.layoutParams
                    listParams.width = root.width / 2
                    binding.rvCatalog.layoutParams = listParams
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.detailFragmentContainer, ProductDetailFragment.newInstance(selectedProduct))
                    .commit()
            } else {
                ProductBottomSheetFragment.newInstance(selectedProduct)
                    .show(supportFragmentManager, "product_sheet")
            }
        }

        // Изначально detailFragmentContainer может отсутствовать — не показываем фрагмент
        binding.rvCatalog.layoutManager = LinearLayoutManager(this)
        binding.rvCatalog.adapter = adapter

        binding.buttonUser?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.buttonCart?.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun updateCartCount(count: Int) {
        if (count > 0) {
            binding.cartBadge?.text = count.toString()
            binding.cartBadge?.visibility = View.VISIBLE
        } else {
            binding.cartBadge?.visibility = View.GONE
        }
    }
}

