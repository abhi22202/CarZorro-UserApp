package com.example.carzorrouserside.data.model.homescreen

import com.google.gson.annotations.SerializedName

// Main response structure
data class PackageDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PackageDetailData?
)

// The main data object
data class PackageDetailData(
    @SerializedName("package") val packageDetails: PackageDetails?,
    @SerializedName("features") val features: List<Feature>?,
    @SerializedName("coating_price_mapping") val coatingPriceMapping: List<CoatingPriceMapping>?,
    // Add other mappings if they become relevant
)

// Detailed information about the package
data class PackageDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("package_name") val name: String?,
    @SerializedName("package_image") val imageUrl: String?,
    @SerializedName("package_description") val description: String?,
    @SerializedName("package_duration") val duration: String? // Assuming duration might be a string like "35mins"
)

// Represents a single feature/inclusion
data class Feature(
    @SerializedName("id") val id: Int,
    @SerializedName("feature_image") val imageUrl: String?,
    @SerializedName("feature") val name: String?,
    @SerializedName("description") val description: String?
)

// Represents the coating price structure
data class CoatingPriceMapping(
    @SerializedName("id") val id: Int,
    @SerializedName("type_name") val typeName: String?,
    @SerializedName("price_mappings") val priceMappings: List<PriceMappingDetail>?
)

// Detailed pricing based on vehicle size
data class PriceMappingDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("warranty") val warranty: Int?,
    @SerializedName("free_service") val freeService: Int?,
    @SerializedName("small_vehicle_price") val smallVehiclePrice: Double?,
    @SerializedName("medium_vehicle_price") val mediumVehiclePrice: Double?,
    @SerializedName("large_vehicle_price") val largeVehiclePrice: Double?
)