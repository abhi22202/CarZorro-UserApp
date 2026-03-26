package com.example.carzorrouserside.data.model.register

import com.google.gson.annotations.SerializedName


data class SignUpOtpVerificationRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("otp")
    val otpCode: String
)



data class SignUpOtpVerificationResponse(
    @SerializedName("success")
    val isSuccessful: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val userData: List<Any> = emptyList(),

    @SerializedName("token")
    val authToken: String? = null // In case token is provided in future
)



data class SignUpOtpUiState(
    val isLoadingVerification: Boolean = false,
    val isLoadingResend: Boolean = false,
    val isVerificationSuccessful: Boolean = false,
    val isResendSuccessful: Boolean = false,
    val authenticationToken: String? = null,
    val verificationErrorMessage: String? = null,
    val resendErrorMessage: String? = null,
    val newOtpFromServer: String? = null,
    val successMessage: String? = null
)