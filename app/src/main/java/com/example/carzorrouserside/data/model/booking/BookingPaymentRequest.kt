package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingPaymentRequest(
    @SerializedName("booking_id")
    val bookingId: Int,
    @SerializedName("option")
    val option: String, // "pay_online" or "pay_after_service"
    @SerializedName("coupon_id")
    val couponId: Int? = null,
    @SerializedName("user_id")
    val userId: Int? = null // Optional - backend might extract from JWT, but adding for explicit validation
)

