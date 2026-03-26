package com.example.carzorrouserside.data.api.homescreen

import com.example.carzorrouserside.data.model.homescreen.TestimonialResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface TestimonialApiService {

    @GET("v1/user/homepage/testimonial")
    suspend fun getTestimonials(
        // <<< MODIFIED: Made parameters nullable to allow for guest users
        @Header("x-user-id") userId: String?,
        @Header("x-token") token: String?
    ): Response<TestimonialResponse>
}