package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingPaymentOrderResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: BookingPaymentOrderData?
)

data class BookingPaymentOrderData(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("razorpay_key")
    val razorpayKey: String
)

