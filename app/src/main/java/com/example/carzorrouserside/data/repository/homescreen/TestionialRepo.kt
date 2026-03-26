package com.example.carzorrouserside.data.repository.homescreen

import com.example.carzorrouserside.data.api.homescreen.TestimonialApiService
import com.example.carzorrouserside.data.model.homescreen.Testimonial
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestimonialRepository @Inject constructor(
    private val apiService: TestimonialApiService,
    private val userPreferencesManager: UserPreferencesManager
) {

    suspend fun getHomepageTestimonials(): Resource<List<Testimonial>> {
        return withContext(Dispatchers.IO) {
            // <<< FIX HERE: Removed the strict authentication check.
            val userId = userPreferencesManager.getUserId()
            val token = userPreferencesManager.getJwtToken()

            try {
                // This call now works for both logged-in users and guests.
                val response = apiService.getTestimonials(userId?.toString(), token)
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    if (responseBody.success) {
                        Resource.Success(responseBody.data)
                    } else {
                        Resource.Error(responseBody.message ?: "API returned success=false")
                    }
                } else {
                    Resource.Error("Failed to fetch testimonials: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
}