package com.example.carzorrouserside.data.repository.homescreen

import android.util.Log
import com.example.carzorrouserside.data.api.homescreen.HomepageServiceApi
import com.example.carzorrouserside.data.model.homescreen.PopularProviderItem
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.di.SharedPreferencesManager
import com.example.carzorrouserside.util.LocationManager
import com.example.carzorrouserside.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PopularAreaRepository @Inject constructor(
    private val api: HomepageServiceApi,
    private val userPreferencesManager: UserPreferencesManager,
    private val locationManager: LocationManager,
    private val sharedPreferencesManager: SharedPreferencesManager
) {
    fun getPopularInArea(): Flow<Resource<List<PopularProviderItem>>> = flow {
        emit(Resource.Loading())
        try {
            // Get selected address
            val selectedAddress = sharedPreferencesManager.getSelectedAddress()
            
            // Parse latitude and longitude from selected address
            var latitude: Double? = null
            var longitude: Double? = null
            
            selectedAddress?.latitudeAndLongitude?.let { latLongString ->
                try {
                    val parts = latLongString.split(",")
                    if (parts.size == 2) {
                        latitude = parts[0].trim().toDoubleOrNull()
                        longitude = parts[1].trim().toDoubleOrNull()
                        Log.d("PopularAreaRepo", "Using address location: lat=$latitude, lng=$longitude")
                    }
                } catch (e: Exception) {
                    Log.e("PopularAreaRepo", "Failed to parse coordinates: ${e.message}")
                }
            }
            
            // If no address location, get from LocationManager using best available method
            if (latitude == null || longitude == null) {
                try {
                    val location = locationManager.getBestAvailableLocation()
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("PopularAreaRepo", "Using device location: lat=$latitude, lng=$longitude")
                } catch (e: Exception) {
                    Log.e("PopularAreaRepo", "Failed to get device location: ${e.message}", e)
                    // Fallback to default location (New Delhi) if all else fails
                    latitude = 28.6139
                    longitude = 77.2090
                    Log.w("PopularAreaRepo", "Using fallback default location: lat=$latitude, lng=$longitude")
                }
            }
            
            // Ensure we always have valid coordinates
            if (latitude == null || longitude == null) {
                Log.w("PopularAreaRepo", "No valid coordinates found, using default location")
                latitude = 28.6139  // New Delhi default
                longitude = 77.2090
            }
            
            // Get user credentials if authenticated
            val userId: String? = if (userPreferencesManager.validateAuthenticationState()) {
                userPreferencesManager.getUserId()?.toString()
            } else {
                null
            }
            
            val token: String? = if (userPreferencesManager.validateAuthenticationState()) {
                userPreferencesManager.getJwtToken()
            } else {
                null
            }
            
            Log.d("PopularAreaRepo", "Fetching popular providers - userId=$userId, hasToken=${token != null}, lat=$latitude, lng=$longitude")
            
            // Convert coordinates to strings as required by the API
            val latitudeString = latitude.toString()
            val longitudeString = longitude.toString()
            
            val response = api.getPopularInArea(
                userId = userId,
                token = token,
                latitude = latitudeString,
                longitude = longitudeString
            )

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.success) {
                    val providers = responseBody.data ?: emptyList()
                    Log.d("PopularAreaRepo", "Successfully fetched ${providers.size} popular providers")
                    emit(Resource.Success(providers))
                } else {
                    Log.e("PopularAreaRepo", "API returned success=false: ${responseBody.message}")
                    emit(Resource.Error(responseBody.message))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("PopularAreaRepo", "API Error: ${response.code()} - ${response.message()} - $errorBody")
                emit(Resource.Error("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("PopularAreaRepo", "Network Exception: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected network error occurred"))
        }
    }
}