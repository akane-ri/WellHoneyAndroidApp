package com.example.wellhoney.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Order(
    val orderId: String = "",
    val orderNumber: Long = 0L,
    val items: Map<String, Map<String, Int>>, // productId - volume - quantity
    val totalPrice: Int = 0,
    val timestamp: Long = 0L
) : Parcelable
