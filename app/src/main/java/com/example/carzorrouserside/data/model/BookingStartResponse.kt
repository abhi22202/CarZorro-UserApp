package com.example.carzorrouserside.data.model

import com.google.gson.annotations.SerializedName

//data class BookingStartResponse(
//    @SerializedName("success")
//    val success: Boolean,
//    @SerializedName("message")
//    val message: String,
//    @SerializedName("data")
//    val data: List<Any>
//)
data class BookingStartResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: BookingStartData?
)

data class BookingStartData(
    @SerializedName("booking_id")
    val bookingId: Int?
)

data class CancelBookingRequest(
    val booking_id: Int
)

data class CancelBookingResponse(
    val success: Boolean,
    val message: String,
    val data: List<Any>?
)


