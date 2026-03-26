package com.example.carzorrouserside.data.model


import androidx.annotation.DrawableRes

data class BookingHistoryItem(
    val id: String,
    val serviceName: String,
    val orderDate: String,
    val orderTime: String,
    val serviceStatus: String,
    val price: Int,
    val duration: String,
    val rating: Float,
    @DrawableRes val imageResId: Int,
    val itemType: ItemType = ItemType.SERVICE
)

enum class ItemType {
    SERVICE,
    PRODUCT
}