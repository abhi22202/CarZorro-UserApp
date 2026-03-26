package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class CheckCouponResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: CouponData?
)

data class CouponData(
    @SerializedName("coupon_id")
    val couponId: Int,
    @SerializedName("minimum_cart_value")
    val minimumCartValue: Double,
    @SerializedName("discount_percentage")
    val discountPercentage: Double,
    @SerializedName("discount_upto")
    val discountUpto: Double
)

