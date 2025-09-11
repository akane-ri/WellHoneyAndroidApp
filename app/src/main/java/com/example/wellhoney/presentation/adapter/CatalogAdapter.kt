package com.example.wellhoney.presentation.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wellhoney.R
import com.example.wellhoney.data.models.HoneyProduct
import com.example.wellhoney.databinding.ItemCatalogBinding

class CatalogAdapter(
    private val catalogList: List<HoneyProduct>,
    private val onItemClick: (HoneyProduct) -> Unit
) :
    RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {


    inner class CatalogViewHolder(private val binding: ItemCatalogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HoneyProduct) {
            binding.itemTitle.text = highlightText(
                binding.root.context,
                item.name,
                listOf("мёд"),
                R.color.light_orange,
                isTitle = true
            )

            binding.itemSubtitle.text = highlightText(
                binding.root.context,
                item.description,
                item.highlight,
                R.color.light_orange
            )

            binding.itemPrice500ml.text = item.price_500ml.toString() + "₽"
            binding.itemPrice1l.text = item.price_1l.toString() + "₽"

            Glide.with(binding.itemImage.context)
                .load(item.img)
                .placeholder(R.drawable.no_photo)
                .error(R.drawable.no_photo)
                .into(binding.itemImage)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    fun highlightText(
        context: Context,
        text: String,
        keywords: List<String>,
        colorRes: Int,
        isTitle: Boolean = false
    ): SpannableString {
        // Для заголовка: без пробелов и заглавными
        val displayText = if (isTitle) text.replace(" ", "").uppercase() else text
        val spannable = SpannableString(displayText)

        for (word in keywords) {
            val cleanWord = if (isTitle) word.replace(" ", "").uppercase() else word
            var startIndex = displayText.indexOf(cleanWord)
            while (startIndex != -1) {
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, colorRes)),
                    startIndex,
                    startIndex + cleanWord.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                startIndex = displayText.indexOf(cleanWord, startIndex + cleanWord.length)
            }
        }

        return spannable
    }



    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        holder.bind(catalogList[position])
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CatalogViewHolder {
        val binding = ItemCatalogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatalogViewHolder(binding)
    }

    override fun getItemCount(): Int = catalogList.size
}