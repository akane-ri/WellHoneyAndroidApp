package com.example.wellhoney.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HoneyProduct(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val highlight: List<String> = emptyList(),
    val img: String = "",
    val price_500ml: Int = 0,
    val price_1l: Int = 0
) : Parcelable