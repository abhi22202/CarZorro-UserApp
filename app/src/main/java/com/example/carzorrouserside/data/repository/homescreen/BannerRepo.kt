package com.example.carzorrouserside.data.repository.homescreen

import android.util.Log
import com.example.carzorrouserside.data.api.homescreen.BannerApiService
import com.example.carzorrouserside.data.model.homescreen.Banner
import com.example.carzorrouserside.data.model.homescreen.BannerResponse
import com.example.carzorrouserside.data.model.homescreen.LocationData
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.util.LocationManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerRepository @Inject constructor(
    private val bannerApiService: BannerApiService,
    private val userPreferencesManager: UserPreferencesManager,
    private val locationManager: LocationManager,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "BannerRepository"
    }

    fun getHomepageBanners(): Flow<Result<List<Banner>>> = flow {
        try {
            Log.d(TAG, "🎯 Starting to fetch homepage banners")

            val response: Response<BannerResponse>

            // --- MODIFIED LOGIC ---
            // Check if the user is authenticated
            if (userPreferencesManager.validateAuthenticationState()) {
                // AUTHENTICATED PATH: Get credentials and location
                val token = userPreferencesManager.getJwtToken()
                val userId = userPreferencesManager.getUserId()

                if (token.isNullOrEmpty() || userId == null) {
                    emit(Result.failure(Exception("Authenticated user has missing credentials")))
                    return@flow
                }

                Log.d(TAG, "✅ User authenticated (ID: $userId). Fetching with location.")
                val location = locationManager.getCurrentLocation()
                Log.d(TAG, "📍 Location obtained: ${location.latitude}, ${location.longitude}")

                // Make API call with all parameters
                response = bannerApiService.getHomepageBanners(
                    userId = userId.toString(),
                    token = token,
                    latitude = location.latitude.toString(),
                    longitude = location.longitude.toString()
                )

            } else {
                // GUEST PATH: Call the API with null for all parameters
                Log.d(TAG, "👤 User is a guest. Fetching generic banners without headers.")
                response = bannerApiService.getHomepageBanners(
                    userId = null,
                    token = null,
                    latitude = null,
                    longitude = null
                )
            }

            // --- COMMON RESPONSE HANDLING ---
            if (response.isSuccessful) {
                val bannerResponse = response.body()
                if (bannerResponse != null && bannerResponse.success) {
                    val sortedBanners = bannerResponse.data.sortedBy { it.position }
                    Log.d(TAG, "✅ Successfully fetched ${sortedBanners.size} banners")
                    emit(Result.success(sortedBanners))
                } else {
                    val errorMessage = bannerResponse?.message ?: "API returned success=false"
                    Log.e(TAG, "❌ API Logic Error: $errorMessage")
                    emit(Result.failure(Exception(errorMessage)))
                }
            } else {
                val errorMessage = "API call failed: ${response.code()} - ${response.message()}"
                Log.e(TAG, "❌ $errorMessage")
                emit(Result.failure(Exception(errorMessage)))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while fetching banners: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    suspend fun getCurrentLocation(): LocationData {
        return locationManager.getCurrentLocation()
    }

    fun hasLocationPermissions(): Boolean {
        return locationManager.hasPermissions()
    }
}