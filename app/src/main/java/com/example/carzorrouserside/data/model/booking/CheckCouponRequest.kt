package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class CheckCouponRequest(
    @SerializedName("coupon_code")
    val couponCode: String,
    @SerializedName("vendor_id")
    val vendorId: Int,
    @SerializedName("user_address_id")
    val userAddressId: Int,
    @SerializedName("car_type")
    val carType: String,
    @SerializedName("feature_type")
    val featureType: String?
)

