package com.example.carzorrouserside.data.api.homescreen

import com.example.carzorrouserside.data.model.homescreen.PackageDetailResponse
import com.example.carzorrouserside.data.model.homescreen.PackageListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PackageApiService {

    @GET("v1/user/package/popular")
    suspend fun getPopularPackages(
        @Header("latitude") latitude: Double,
        @Header("longitude") longitude: Double,
        @Header("x-per-page") perPage: Int,
        @Query("page") page: Int
    ): Response<PackageListResponse>


    @GET("v1/user/package/all")
    suspend fun getAllPackages(
        @Header("x-per-page") perPage: Int,
        @Query("page") page: Int
    ): Response<PackageListResponse>


    @GET("v1/user/package/details")
    suspend fun getPackageDetails(
        @Header("x-package-id") packageId: Int
    ): Response<PackageDetailResponse>


    @GET("v1/user/package/search")
    suspend fun searchPackages(
        @Header("x-search") query: String,
        @Header("latitude") latitude: Double,
        @Header("longitude") longitude: Double
    ): Response<PackageListResponse>
}