package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class BookingDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: BookingDetailsData?
)

data class BookingDetailsData(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_address_id") val userAddressId: Int,
    @SerializedName("car_id") val carId: Int,
    @SerializedName("service_type") val serviceType: String,
    @SerializedName("booking_status") val bookingStatus: String,
    @SerializedName("booking_user_current_price") val bookingUserCurrentPrice: Double,
    @SerializedName("created_time") val createdTime: String,
    @SerializedName("service_time") val serviceTime: String? = null,
    @SerializedName("date_time") val dateTime: String? = null,
    @SerializedName("address") val address: BookingAddress?,
    @SerializedName("vendor") val vendor: BookingVendor? = null,
    // Keep these for backward compatibility, but they should come from vendor object
    @SerializedName("vendor_name") val vendorName: String? = null,
    @SerializedName("vendor_rating") val vendorRating: Double? = null,
    @SerializedName("vendor_reviews") val vendorReviews: Int? = null,
    @SerializedName("vendor_id") val vendorId: Int? = null
)

data class BookingAddress(
    @SerializedName("id") val id: Int,
    @SerializedName("flat_no") val flatNo: String?,
    @SerializedName("landmark") val landmark: String?,
    @SerializedName("city") val city: String,
    @SerializedName("state") val state: String,
    @SerializedName("country") val country: String,
    @SerializedName("address") val address: String
)

data class BookingVendor(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("profile_pic") val profilePic: String? = null,
    @SerializedName("cin_number") val cinNumber: String? = null,
    @SerializedName("cin_document") val cinDocument: String? = null,
    @SerializedName("aadhar_number") val aadharNumber: String? = null,
    @SerializedName("aadhar_document") val aadharDocument: String? = null,
    @SerializedName("gstin_number") val gstinNumber: String? = null,
    @SerializedName("gstin_document") val gstinDocument: String? = null,
    @SerializedName("pan_number") val panNumber: String? = null,
    @SerializedName("pan_document") val panDocument: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("vendor_completion_stage") val vendorCompletionStage: String? = null,
    @SerializedName("approved_by") val approvedBy: String? = null,
    @SerializedName("approved_at") val approvedAt: String? = null,
    @SerializedName("business_method") val businessMethod: String? = null,
    @SerializedName("business_sub_method") val businessSubMethod: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("security_deposit_amount") val securityDepositAmount: Double? = null,
    @SerializedName("security_deposit_left") val securityDepositLeft: Double? = null,
    @SerializedName("wallet_balance") val walletBalance: Double? = null,
    @SerializedName("penalty_balance") val penaltyBalance: Double? = null,
    @SerializedName("referral_code") val referralCode: String? = null,
    @SerializedName("rating") val rating: String? = null
)

