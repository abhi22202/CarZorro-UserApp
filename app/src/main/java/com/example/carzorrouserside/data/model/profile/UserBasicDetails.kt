package com.example.carzorrouserside.data.model.profile

import com.google.gson.annotations.SerializedName

data class UserBasicDetailsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: UserBasicDetails?
)

data class UserBasicDetails(
    @SerializedName("id")
    val id: Int,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("dob")
    val dob: String?,
    @SerializedName("profile_pic")
    val profilePic: String?,
    @SerializedName("alt_phone")
    val altPhone: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("phone_verification_status")
    val phoneVerificationStatus: String?,
    @SerializedName("email_verification_status")
    val emailVerificationStatus: String?,
    @SerializedName("referral_code")
    val referralCode: String?,
    @SerializedName("wallet_balance")
    val walletBalance: Double? = null
) {
    val isPhoneVerified: Boolean
        get() = phoneVerificationStatus == "1"
    
    val isEmailVerified: Boolean
        get() = emailVerificationStatus == "1"
}

data class EditProfileRequest(
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("dob")
    val dob: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("alt_phone")
    val altPhone: String?,
    @SerializedName("gender")
    val gender: String?
)

data class EditProfileResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<Any>? = null
)

data class EditProfileImageResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<Any>? = null
)

