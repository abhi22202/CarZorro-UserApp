package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingPaymentOrderRequest(
    @SerializedName("amount")
    val amount: Double
)

