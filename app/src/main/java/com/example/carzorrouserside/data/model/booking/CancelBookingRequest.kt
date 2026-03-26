package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class CancelBookingRequest(
    @SerializedName("booking_id") val booking_id: Int,
    @SerializedName("cancel_reason_id") val cancel_reason_id: Int? = null,
    @SerializedName("cancel_reason_text") val cancel_reason_text: String? = null
)

