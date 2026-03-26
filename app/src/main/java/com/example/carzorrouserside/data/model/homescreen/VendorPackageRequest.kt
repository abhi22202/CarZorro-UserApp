package com.example.carzorrouserside.data.model.vendor

import com.google.gson.annotations.SerializedName

data class VendorPackageResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<VendorPackage>
)

data class VendorPackage(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("discount_price")
    val discountPrice: Double,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("review_count")
    val reviewCount: Int,
    @SerializedName("washing_price_mapping")
    val washingPriceMapping: List<WashingPriceMapping>
)

data class WashingPriceMapping(
    @SerializedName("id")
    val id: Int,
    @SerializedName("package_id")
    val packageId: Int,
    @SerializedName("car_type")
    val carType: String,
    @SerializedName("doorstep_price")
    val doorstepPrice: Double,
    @SerializedName("pickup_price")
    val pickupPrice: Double,
    @SerializedName("self_visit_price")
    val selfVisitPrice: Double
)