package com.example.carzorrouserside.data.model.homescreen

import com.google.gson.annotations.SerializedName

data class ActiveBookingResponse(
    val success: Boolean,
    val message: String,
    val data: ActiveBookingData?
)
data class ActiveBookingData(
    val key: String?, // can be "active_booking", "rebid", or "none"

    @SerializedName("booking")
    val booking: ActiveBookingDetails? = null,

    @SerializedName("bids")
    val bids: List<Bid>? = null
)

data class ActiveBookingDetails(
    @SerializedName("id") val id: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("service_type") val serviceType: String?,
    @SerializedName("vendor") val vendor: Vendor?,
    @SerializedName("booking_payment_method") val paymentMode:String?,
    @SerializedName("booking_payable_amount") val price: Int?,
    @SerializedName("vendor_name") val vendorName: String?,
    @SerializedName("vendor_rating") val vendorRating: Double?,
    @SerializedName("vendor_reviews") val vendorReviews: Int?,
    @SerializedName("booking_confirmation_code") val startOtp:Int?,
    @SerializedName("booking_ending_code") val endOtp:Int?,
    @SerializedName("booking_payment_status") val paymentStatus:String?,
    @SerializedName("booking_time") val dateTime :String?,
    @SerializedName("booking_status") val booking_status: String? = null
)

data class Vendor(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("profile_pic") val profilePic: String?,
    @SerializedName("avg_rating") val avgRating: Double?,
    @SerializedName("review_count") val reviewCount: Int?
)


data class Bid(
    @SerializedName("id") val id: Int,
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("vendor_id") val vendorId: Int,
    @SerializedName("prev_amount") val prevAmount: Double,
    @SerializedName("new_amount") val newAmount: Double,
    @SerializedName("vendor_name") val vendorName: String,
    @SerializedName("vendor_rating") val vendorRating: Int,
    @SerializedName("vendor_reviews") val vendorReviews: Double
)
//data class ActiveBookingData(
//    val key: String?,
//    val data: Map<String, Any>?
//)