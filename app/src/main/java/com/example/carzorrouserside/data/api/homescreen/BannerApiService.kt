package com.example.carzorrouserside.data.api.homescreen

import com.example.carzorrouserside.data.model.homescreen.BannerResponse // Assuming this is your response wrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface BannerApiService {

    @GET("v1/user/homepage/banner")
    suspend fun getHomepageBanners(

        @Header("x-user-id") userId: String?,
        @Header("x-token") token: String?,
        @Header("x-latitude") latitude: String?,
        @Header("x-longitude") longitude: String?
    ): Response<BannerResponse>
}