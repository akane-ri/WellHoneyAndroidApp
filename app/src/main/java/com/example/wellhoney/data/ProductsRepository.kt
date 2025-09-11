package com.example.wellhoney.data

import android.content.Context
import android.util.Log
import com.example.wellhoney.data.models.HoneyProduct
import org.json.JSONArray
import java.io.IOException

class HoneyRepository(private val context: Context) {
    fun loadHoneyProducts(): List<HoneyProduct> {
        val jsonString = try {
            context.assets.open("honey_products.json").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            return emptyList()
        }

        val jsonArray = JSONArray(jsonString)
        val products = mutableListOf<HoneyProduct>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val highlightList = (0 until (obj.optJSONArray("highlight")?.length() ?: 0))
                .map { obj.getJSONArray("highlight").getString(it) }

            val product = HoneyProduct(
                id = obj.getString("id"),
                name = obj.getString("name"),
                description = obj.getString("description"),
                highlight = highlightList,
                img = obj.getString("img"),
                price_500ml = obj.getInt("price_500ml"),
                price_1l = obj.getInt("price_1l")
            )

            products.add(product)
        }
        return products
    }
}