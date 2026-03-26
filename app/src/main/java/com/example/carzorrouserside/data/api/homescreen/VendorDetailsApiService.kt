package com.example.carzorrouserside.data.api.vendor

import com.example.carzorrouserside.data.model.vendor.FavoriteVendorRequest
import com.example.carzorrouserside.data.model.vendor.FavoriteVendorResponse
import com.example.carzorrouserside.data.model.vendor.FavouriteVendorResponse
import com.example.carzorrouserside.data.model.vendor.VendorDetailsResponse
import com.example.carzorrouserside.data.model.vendor.VendorPackageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface VendorApiService {
    @GET("v1/user/vendor/details")
    suspend fun getVendorDetails(
        @Header("x-vendor-id") vendorId: String
    ): Response<VendorDetailsResponse>

    @GET("v1/user/vendor/package")
    suspend fun getVendorPackages(
        @Header("x-vendor-id") vendorId: String
    ): Response<VendorPackageResponse>

    @POST("v1/user/auth/vendor/favourite")
    suspend fun toggleFavoriteStatus(
        @Header("Authorization") token: String,
        @Body request: FavoriteVendorRequest
    ): Response<FavoriteVendorResponse>

    @GET("v1/user/auth/vendor/favourite-listing")
    suspend fun getFavouriteVendors(): Response<FavouriteVendorResponse>
}