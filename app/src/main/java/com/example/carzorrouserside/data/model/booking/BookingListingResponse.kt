package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingListingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: BookingListingData?
)

data class BookingListingData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("data") val bookings: List<BookingItem>,
    @SerializedName("first_page_url") val firstPageUrl: String?,
    @SerializedName("from") val from: Int?,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("last_page_url") val lastPageUrl: String?,
    @SerializedName("links") val links: List<PageLink>?,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    @SerializedName("path") val path: String?,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    @SerializedName("to") val to: Int?,
    @SerializedName("total") val total: Int
)

data class BookingItem(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("service_feature_name") val serviceFeatureName: String,
    @SerializedName("service_feature_image") val serviceFeatureImage: String?,
    @SerializedName("created_time") val createdTime: String,
    @SerializedName("service_time") val serviceTime: String,
    @SerializedName("price") val price: Int
)

data class PageLink(
    @SerializedName("url") val url: String?,
    @SerializedName("label") val label: String,
    @SerializedName("active") val active: Boolean
)

