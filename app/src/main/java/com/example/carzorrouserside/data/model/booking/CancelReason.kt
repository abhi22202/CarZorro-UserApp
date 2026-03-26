package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class CancelReason(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val reason: String? = null, // API returns "title" but we use "reason" as property name
    @SerializedName("type") val type: String? = null,
    @SerializedName("is_active") val is_active: Int
)

data class CancelReasonResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<CancelReason>?
)

