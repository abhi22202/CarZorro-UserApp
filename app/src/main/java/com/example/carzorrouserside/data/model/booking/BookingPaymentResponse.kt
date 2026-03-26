package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingPaymentResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: BookingPaymentData?
)

data class BookingPaymentData(
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("booking_id")
    val bookingId: Int,
    @SerializedName("option")
    val option: String,
    @SerializedName("order_id")
    val orderId: String?
)

