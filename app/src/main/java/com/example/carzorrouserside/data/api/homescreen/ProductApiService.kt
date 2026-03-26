package com.example.carzorrouserside.data.api.homescreen

import com.example.carzorrouserside.data.model.homescreen.ProductDetailResponse
import com.example.carzorrouserside.data.model.homescreen.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ProductApiService {

    @GET("v1/user/homepage/product/product-listing")
    suspend fun getProducts(
        @Header("per-page") perPage: Int = 20,
        @Header("search") search: String? = null,
        @Header("X-CSRF-TOKEN") token: String = "",
        @Query("page") page: Int = 1
    ): Response<ProductResponse>

    @GET("v1/user/homepage/product/details")
    suspend fun getProductDetail(
        @Header("product-id") productId: Int,
        @Header("Authorization") authorization: String,
        @Header("X-CSRF-TOKEN") token: String = ""
    ): Response<ProductDetailResponse>
}