package com.example.carzorrouserside.data.api

import com.example.carzorrouserside.data.model.booking.BookingListingResponse
import com.example.carzorrouserside.data.model.booking.CancelBookingRequest
import com.example.carzorrouserside.data.model.booking.CancelReason
import com.example.carzorrouserside.data.model.booking.CancelReasonResponse
import com.example.carzorrouserside.data.model.booking.OfferActionRequest
import com.example.carzorrouserside.data.model.booking.BookingDetailsResponse
import com.example.carzorrouserside.data.model.booking.CheckCouponRequest
import com.example.carzorrouserside.data.model.booking.CheckCouponResponse
import com.example.carzorrouserside.data.model.booking.BookingPaymentRequest
import com.example.carzorrouserside.data.model.booking.BookingPaymentResponse
import com.example.carzorrouserside.data.model.booking.BookingPaymentConfirmRequest
import com.example.carzorrouserside.data.model.booking.BookingPaymentConfirmResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface BookingApiService {

    @GET("v1/user/auth/booking/listing")
    suspend fun getBookingListing(
        @Header("x-booking-status") status: String? = null
    ): Response<BookingListingResponse>

    @GET("v1/user/auth/booking/cancel/reasons")
    suspend fun getCancelReasons(): Response<CancelReasonResponse>

    @POST("v1/user/auth/booking/cancel/request")
    suspend fun cancelBooking(
        @Body request: CancelBookingRequest
    ): Response<Any>

    @POST("v1/user/auth/booking/offer/accept")
    suspend fun acceptBookingOffer(
        @Body request: OfferActionRequest
    ): Response<Any>

    @POST("v1/user/auth/booking/offer/decline")
    suspend fun declineBookingOffer(
        @Body request: OfferActionRequest
    ): Response<Any>

    @GET("v1/user/auth/booking/details")
    suspend fun getBookingDetails(
        @Header("x-booking-id") bookingId: Int
    ): Response<BookingDetailsResponse>

    @POST("v1/user/auth/booking/check-coupon")
    suspend fun checkCoupon(
        @Body request: CheckCouponRequest
    ): Response<CheckCouponResponse>

    @POST("v1/user/auth/booking/payment")
    suspend fun makeBookingPayment(
        @Body request: BookingPaymentRequest
    ): Response<BookingPaymentResponse>

    @POST("v1/user/auth/booking/payment/confirm")
    suspend fun confirmBookingPayment(
        @Body request: BookingPaymentConfirmRequest
    ): Response<BookingPaymentConfirmResponse>
}
