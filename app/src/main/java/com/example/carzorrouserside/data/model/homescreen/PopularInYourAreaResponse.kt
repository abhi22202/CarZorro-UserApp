package com.example.carzorrouserside.data.model.homescreen

import com.google.gson.annotations.SerializedName

data class PopularInAreaResponse(
    val success: Boolean,
    val message: String,
    val data: List<PopularProviderItem>
)

data class PopularProviderItem(
    val id: Int,
    val name: String,
    @SerializedName("profile_pic")
    val profilePic: String?,
    val distance: String,
    val rating: Double
)