package com.example.carzorrouserside.data.model.homescreen

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a single banner item
 */
data class Banner(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("position")
    val position: Int,

    @SerializedName("banner_img_url")
    val bannerImageUrl: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("state")
    val state: Int,

    @SerializedName("city")
    val city: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

/**
 * Data class representing the API response for banners
 */
data class BannerResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Banner>
)

/**
 * UI state for banner loading
 */
data class BannerUiState(
    val isLoading: Boolean = false,
    val banners: List<Banner> = emptyList(),
    val error: String? = null
)

/**
 * Location data class
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double
)