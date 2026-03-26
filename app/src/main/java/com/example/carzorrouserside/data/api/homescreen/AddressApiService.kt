package com.example.carzorrouserside.data.api.homescreen

import com.example.carzorrouserside.data.model.homescreen.AddressListingResponse
import com.example.carzorrouserside.data.model.homescreen.AddressRequest
import com.example.carzorrouserside.data.model.homescreen.AddressResponse
import com.example.carzorrouserside.data.model.homescreen.DeleteAddressRequest
import com.example.carzorrouserside.data.model.homescreen.DeleteAddressResponse
import com.example.carzorrouserside.data.model.homescreen.EditAddressRequest
import com.example.carzorrouserside.data.model.homescreen.EditAddressResponse
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AddressApiService {
    @GET("v1/user/auth/address/listing")
    suspend fun getAddressList(
        @Header("Authorization") token: String
    ): Response<AddressListingResponse>

    @POST("v1/user/auth/address/add")
    suspend fun addAddress(
        @Header("Authorization") token: String,
        @Header("X-CSRF-TOKEN") csrfToken: String = "",
        @Body addressRequest: AddressRequest

    ): Response<AddressResponse>

    @POST("v1/user/auth/address/edit")
    suspend fun editAddress(
        @Header("Authorization") token: String,
        @Header("X-CSRF-TOKEN") csrfToken: String = "",
        @Body request: EditAddressRequest
    ): Response<EditAddressResponse>

    @POST("v1/user/auth/address/delete")
    suspend fun deleteAddress(
        @Header("Authorization") token: String,
        @Header("X-CSRF-TOKEN") csrfToken: String = "",
        @Body request: DeleteAddressRequest
    ): Response<DeleteAddressResponse>
}