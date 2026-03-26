package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class OfferActionRequest(
    @SerializedName("booking_id") val booking_id: Int,
    @SerializedName("vendor_id") val vendor_id: Int
)

