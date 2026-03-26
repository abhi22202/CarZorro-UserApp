package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingPaymentConfirmResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Any? = null
)

