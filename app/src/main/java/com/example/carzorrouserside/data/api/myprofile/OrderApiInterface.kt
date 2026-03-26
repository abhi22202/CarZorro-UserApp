package com.example.carzorrouserside.data.api.myprofile

import com.example.carzorrouserside.data.model.BookingStartRequest
import com.example.carzorrouserside.data.model.BookingStartResponse
import com.example.carzorrouserside.data.model.booking.OrderDetailResponse
import com.example.carzorrouserside.data.model.booking.OrderListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderApiInterface {

    @GET("v1/user/auth/order/order-listing")
    suspend fun getOrderHistory(
        @Header("Authorization") token: String,
        @Header("user-id") userId: Int,
        @Query("page") page: Int = 1,
        @Query("per-page") perPage: Int = 20
    ): Response<OrderListResponse>


@GET("v1/user/auth/order/order-details")
suspend fun getOrderDetails(
    @Header("Authorization") token: String,
    @Header("user-id") userId: Int,
    @Header("order-id") orderId: Int
): Response<OrderDetailResponse>

    @POST("v1/user/auth/booking/start")
    suspend fun startBooking(
        @Header("Authorization") token: String,
        @Body bookingStartRequest: BookingStartRequest
    ): Response<BookingStartResponse>

    @POST("v1/user/auth/booking/cancel/request")
    suspend fun cancelBooking(
        @Body request: com.example.carzorrouserside.data.model.booking.CancelBookingRequest
    ): Response<kotlin.Any>

//@POST("v1/user/auth/booking/start")
//
//suspend fun startBooking(
////    @Header("Authorization") token: String,
////    @Header("X-CSRF-TOKEN") csrfToken: String = "",
//    @Body bookingStartRequest: BookingStartRequest
//): Response<BookingStartResponse>
}