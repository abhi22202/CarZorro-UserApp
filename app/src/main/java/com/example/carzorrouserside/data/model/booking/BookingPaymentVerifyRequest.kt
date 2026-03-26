package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingPaymentVerifyRequest(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("pay_id")
    val payId: String
)

