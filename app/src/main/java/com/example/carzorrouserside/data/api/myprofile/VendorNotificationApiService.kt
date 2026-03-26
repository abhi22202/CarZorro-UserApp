package com.example.carzorrouserside.data.api.myprofile

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API Service for triggering vendor notifications
 * This is used if backend doesn't automatically send FCM when startBooking is called
 */
interface VendorNotificationApiService {
    
    /**
     * Trigger FCM notifications to vendors after booking is created
     * Note: This is a fallback. Ideally, backend should handle FCM automatically
     */
    @POST("v1/user/auth/booking/notify-vendors")
    suspend fun notifyVendors(
        @Header("Authorization") token: String,
        @Body request: NotifyVendorsRequest
    ): Response<NotifyVendorsResponse>
}

data class NotifyVendorsRequest(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("service_type") val serviceType: String,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("date_time") val dateTime: String?
)

data class NotifyVendorsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("vendors_notified") val vendorsNotified: Int?
)


