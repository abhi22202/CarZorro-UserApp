package com.example.carzorrouserside.data.model.vendor

import com.google.gson.annotations.SerializedName

// Main response wrapper
data class VendorDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: VendorData?
)

// The main data object
data class VendorData(
    @SerializedName("vendor") val vendor: Vendor,
    @SerializedName("business") val business: Business,
    @SerializedName("is_favourite") val isFavourite: Boolean? = false,
    @SerializedName("rating") val rating: Double,
    @SerializedName("review") val reviewCount: Int,
    @SerializedName("address") val address: Address,
    @SerializedName("allReviews") val allReviews: List<AllReview>?
)

data class Vendor(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String?,
    @SerializedName("reviews") val reviews: List<VendorReview>? = emptyList(),
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("profile_pic") val profilePic: String? = null
)

data class VendorReview(
    @SerializedName("id") val id: Int,
    @SerializedName("rating") val rating: Double,
    @SerializedName("comment") val comment: String
)

data class Business(
    @SerializedName("id") val id: Int,
    @SerializedName("business_name") val businessName: String,
    @SerializedName("status") val status: String?,
    @SerializedName("vendor_id") val vendorId: Int? = null,
    @SerializedName("bussiness_name") val businessNameAlt: String? = null, // Keep for backward compatibility
    @SerializedName("bussiness_type") val businessType: String? = null
)

data class Address(
    @SerializedName("id") val id: Int,
    @SerializedName("flat_no") val flatNo: String?,
    @SerializedName("landmark") val landmark: String?,
    @SerializedName("city") val city: String,
    @SerializedName("state") val state: String,
    @SerializedName("country") val country: String?,
    @SerializedName("address") val fullAddress: String,
    @SerializedName("vendor_bussiness_id") val vendorBusinessId: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("pincode") val pincode: String? = null
)

data class AllReviews(
    @SerializedName("id") val id: Int?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("review") val review: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("user") val user: List<UserReviewDetail>? = emptyList()
)

data class AllReview(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("vendor_id") val vendorId: Int? = null,
    @SerializedName("vendor_business_id") val vendorBusinessId: Int? = null,
    @SerializedName("package_id") val packageId: Int? = null,
    @SerializedName("booking_id") val bookingId: Int? = null,
    @SerializedName("rating") val rating: Int,
    @SerializedName("review") val review: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("user") val user: UserReviewDetail
)

data class UserReviewDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("profile_pic") val profilePic: String? = null
)