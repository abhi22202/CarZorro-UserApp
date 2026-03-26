package com.example.carzorrouserside.data.model.vendor

import com.google.gson.annotations.SerializedName

data class FavouriteVendorResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<FavouriteVendorDto>
)

data class FavouriteVendorDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("profile_pic") val profilePic: String?,
    @SerializedName("distance") val distance: String,
    @SerializedName("rating") val rating: Double
)