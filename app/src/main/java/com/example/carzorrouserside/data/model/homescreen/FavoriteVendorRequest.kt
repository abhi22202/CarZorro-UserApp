package com.example.carzorrouserside.data.model.vendor

import com.google.gson.annotations.SerializedName

// Request body for the POST call
data class FavoriteVendorRequest(
    @SerializedName("vendor_id")
    val vendorId: Int
)

// Response from the POST call
data class FavoriteVendorResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: FavoriteVendorData?
)

data class FavoriteVendorData(
    @SerializedName("vendor_id")
    val vendorId: Int
)