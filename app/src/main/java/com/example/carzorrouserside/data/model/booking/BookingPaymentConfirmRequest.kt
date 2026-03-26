package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingPaymentConfirmRequest(
    @SerializedName("booking_id")
    val bookingId: Int,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("payment_id")
    val paymentId: String
)

