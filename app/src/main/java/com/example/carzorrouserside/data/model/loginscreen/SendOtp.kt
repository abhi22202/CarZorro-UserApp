package com.example.carzorrouserside.data.model.loginscreen

import com.google.gson.annotations.SerializedName

data class SendOtpRequest(
    @SerializedName("phone")
    val phone: String
)

data class SendOtpResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: OtpData?
)

data class OtpData(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("otp")
    val otp: Int
)

data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("errors")
    val errors: Any? = null
)
data class UserLoginUiState(
    val isLoading: Boolean = false,
    val otpSent: Boolean = false,
    val message: String? = null,
    val serverOtp: String? = null,
    val error: String? = null
)
data class VerifyOtpRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("otp")
    val otp: String
)

data class VerifyOtpResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: AuthData?
)

data class AuthData(
    @SerializedName("token")
    val token: String,
    @SerializedName("user_id")
    val userId: Int
)
data class OtpVerificationUiState(
    val isLoading: Boolean = false,
    val isVerificationSuccessful: Boolean = false,
    val error: String? = null,
    val token: String? = null,
    val message: String? = null,
    val resendSuccess: Boolean = false, // Make sure this exists
    val newOtp: String? = null // Make sure this exists
)
data class UserSessionData(
    val userId: Int,
    val phone: String?,
    val token: String,
    val isLoggedIn: Boolean
)