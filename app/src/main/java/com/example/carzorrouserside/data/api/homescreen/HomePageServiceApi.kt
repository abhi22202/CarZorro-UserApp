package com.example.carzorrouserside.data.api.homescreen

import com.example.carzorrouserside.data.model.homescreen.ActiveBookingResponse
import com.example.carzorrouserside.data.model.homescreen.HomepageServiceResponse
import com.example.carzorrouserside.data.model.homescreen.PopularInAreaResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface HomepageServiceApi {
    @GET("v1/user/homepage/service")
    suspend fun getHomepageServices(
        @Header("x-user-id") userId: Int?,
        @Header("x-token") token: String?
    ): Response<HomepageServiceResponse>

    @GET("v1/user/homepage/popular-in-area")
    suspend fun getPopularInArea(
        @Header("x-user-id") userId: String?,
        @Header("x-token") token: String?,
        @Header("x-latitude") latitude: String,
        @Header("x-longitude") longitude: String
    ): Response<PopularInAreaResponse>

    @GET("v1/user/auth/booking/active")
    suspend fun getActiveBooking(
        @Header("x-user-id") userId: Int?,
        @Header("x-token") token: String?
    ): Response<ActiveBookingResponse>

}