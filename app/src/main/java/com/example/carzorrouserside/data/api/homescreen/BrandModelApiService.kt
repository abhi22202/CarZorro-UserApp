package com.example.carzorrouserside.data.api.homescreen

import com.example.carzorrouserside.data.model.car.BaseResponse
import com.example.carzorrouserside.data.model.car.CarListingItem
import com.example.carzorrouserside.data.model.homescreen.BrandResponse
import com.example.carzorrouserside.data.model.homescreen.ModelResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BrandModelApiService {

    @GET("v1/user/auth/car/brand-listing")
    suspend fun getBrands(
        @Header("Authorization") token: String
    ): Response<BrandResponse>

    @GET("v1/user/auth/car/brand-search")
    suspend fun searchBrands(
        @Header("Authorization") token: String,
        @Query("search") searchQuery: String
    ): Response<BrandResponse>

    @GET("v1/user/auth/car/model-listing")
    suspend fun getModels(
        @Header("Authorization") token: String,
        @Query("brand_id") brandId: Int
    ): Response<ModelResponse>

    @GET("v1/user/auth/car/model-search")
    suspend fun searchModels(
        @Header("Authorization") token: String,
        @Query("brand_id") brandId: Int,
        @Query("search") searchQuery: String
    ): Response<ModelResponse>
}
