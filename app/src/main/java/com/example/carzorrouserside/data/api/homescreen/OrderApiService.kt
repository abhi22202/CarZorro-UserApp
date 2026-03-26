package com.example.carzorrouserside.data.api.homescreen


import com.example.carzorrouserside.data.model.homescreen.CreateOrderRequest
import com.example.carzorrouserside.data.model.homescreen.CreateOrderResponse
import com.example.carzorrouserside.data.model.homescreen.VerifyOrderRequest
import com.example.carzorrouserside.data.model.homescreen.VerifyOrderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OrderApiService {

    @POST("v1/user/auth/product/create-order")
    suspend fun createOrder(
        @Header("user-id") userId: String,
        @Header("Authorization") authorization: String,
        @Header("X-CSRF-TOKEN") token: String = "",
        @Body request: CreateOrderRequest
    ): Response<CreateOrderResponse>

    @POST("v1/user/auth/product/verify-order")
    suspend fun verifyOrder(
        @Header("user-id") userId: String,
        @Header("Authorization") authorization: String,
        @Header("X-CSRF-TOKEN") token: String = "",
        @Body request: VerifyOrderRequest
    ): Response<VerifyOrderResponse>
}