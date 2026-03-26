package com.example.carzorrouserside.data.model.homescreen

import com.google.gson.annotations.SerializedName
import com.example.carzorrouserside.R

data class ApiPackage(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("review_count") val reviewCount: Int?,

    @SerializedName("price") val price: Double?,

    @SerializedName("compare_to_price") private val compareToPrice: Double?,
    @SerializedName("discount_price") private val discountPrice: Double?,

    ) {
    val displayPrice: Double
        get() = price ?: 0.0

    val originalPrice: Double?
        get() {
            val original = compareToPrice ?: discountPrice
            return if (original != null && original > displayPrice) original else null
        }

    val imageRes: Int
        get() = R.drawable.car_wash
}


data class PackagePaginationData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("data") val packages: List<ApiPackage>,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("total") val total: Int
)

data class PackageListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val paginationData: PackagePaginationData?
)