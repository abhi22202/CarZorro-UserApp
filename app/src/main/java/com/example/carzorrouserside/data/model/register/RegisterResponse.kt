package com.example.carzorrouserside.data.model.register

import com.google.gson.annotations.SerializedName
data class RegisterRequest(
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("dob")
    val dob: String, // Format: YYYY-MM-DD
    @SerializedName("phone")
    val phone: String,
    @SerializedName("gender")
    val gender: String
)
data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: RegisterData?
)

data class RegisterData(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("otp")
    val otp: Int
)




data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String,
    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
)