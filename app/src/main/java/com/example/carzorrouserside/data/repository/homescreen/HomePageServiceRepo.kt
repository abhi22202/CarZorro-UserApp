package com.example.carzorrouserside.data.repository.homescreen

import android.util.Log
import com.example.carzorrouserside.data.api.homescreen.HomepageServiceApi
import com.example.carzorrouserside.data.model.homescreen.ActiveBookingResponse
import com.example.carzorrouserside.data.model.homescreen.HomepageService
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomepageServiceRepository @Inject constructor(
    private val api: HomepageServiceApi,
    private val userPreferencesManager: UserPreferencesManager
) {
    companion object {
        private const val TAG = "HomepageServiceRepository"
    }

    suspend fun getHomepageServices(): Resource<List<HomepageService>> {
        return try {
            val token = userPreferencesManager.getJwtToken()
            val userId = userPreferencesManager.getUserId()

            if (userId != null) {
                Log.d(TAG, "Fetching homepage services for user: $userId")
            } else {
                Log.d(TAG, "Fetching homepage services for guest user.")
            }

            val response = api.getHomepageServices(userId, token)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "Successfully fetched ${body.data.size} services")
                    Resource.Success(body.data)
                } else {
                    val errorMessage = body?.message ?: "Failed to fetch services"
                    Log.e(TAG, "API returned unsuccessful response: $errorMessage")
                    Resource.Error(errorMessage)
                }
            } else {
                Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
                val errorMessage = when (response.code()) {
                    401 -> "Session expired. Please login again."
                    500 -> "Server error occurred. Please try again later."
                    else -> "Network error occurred. Please check your connection."
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching services", e)
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "No internet connection."
                else -> "An unexpected error occurred."
            }
            Resource.Error(errorMessage)
        }
    }
    suspend fun getActiveBooking(userId: Int?, token: String?): Resource<ActiveBookingResponse> {
        return try {
            val response = api.getActiveBooking(userId, token)
            if (response.isSuccessful && response.body() != null) {
                Log.d("ActiveBookingRepo", "✅ ActiveBookingResponse: ${response.body()}")
                Resource.Success(response.body()!!)
            } else {
                Log.e("ActiveBookingRepo", "❌ Error Response: ${response.message()}")
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Log.e("ActiveBookingRepo", "⚠️ Exception: ${e.localizedMessage}", e)
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }

}