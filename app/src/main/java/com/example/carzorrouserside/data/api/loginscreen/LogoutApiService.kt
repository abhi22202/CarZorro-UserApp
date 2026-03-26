package com.example.carzorrouserside.data.api.loginscreen


import com.example.carzorrouserside.data.model.loginscreen.LogoutResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface LogoutApiService {

    @POST("v1/user/auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String
    ): Response<LogoutResponse>
}