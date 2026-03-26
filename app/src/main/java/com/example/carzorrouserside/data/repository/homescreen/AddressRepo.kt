package com.example.carzorrouserside.data.repository.homescreen

import android.util.Log
import com.example.carzorrouserside.data.api.homescreen.AddressApiService
import com.example.carzorrouserside.data.model.homescreen.*
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.di.SharedPreferencesManager
import com.example.carzorrouserside.util.LocationManager
import com.example.carzorrouserside.util.Resource
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLException

class AddressRepository @Inject constructor(
    private val apiService: AddressApiService,
    private val userPreferencesManager: UserPreferencesManager,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val locationManager: LocationManager
) {
    companion object {
        const val TAG = "AddressRepository"
    }

    private val gson = Gson()


    suspend fun getAddressList(): Flow<Resource<AddressListingResponse>> = flow {
        Log.d(TAG, "=== Starting getAddressList operation ===")

        try {
            emit(Resource.Loading())

            if (!userPreferencesManager.validateAuthenticationState()) {
                Log.e(TAG, "User not authenticated")
                emit(Resource.Error("Authentication required. Please login again."))
                return@flow
            }

            val token = userPreferencesManager.getJwtToken()
            if (token.isNullOrBlank()) {
                Log.e(TAG, "No valid JWT token found")
                emit(Resource.Error("Authentication token expired. Please login again."))
                return@flow
            }

            Log.d(TAG, "JWT token available for address listing: ${token.take(10)}...")

            val bearerToken = "Bearer $token"
            val response = apiService.getAddressList(bearerToken)

            Log.d(TAG, "Address List API Response - Code: ${response.code()}")

            when (response.code()) {
                401 -> {
                    Log.e(TAG, "Authentication failed - clearing user session")
                    userPreferencesManager.clearUserAuthData()
                    emit(Resource.Error("Authentication failed. Please login again."))
                    return@flow
                }
                403 -> emit(Resource.Error("Access denied. Please check your permissions."))
                404 -> emit(Resource.Error("Address service not found"))
                500 -> emit(Resource.Error("Server error. Please try again later."))
            }

            if (response.isSuccessful) {
                response.body()?.let { addressListingResponse ->
                    if (addressListingResponse.success) {
                        Log.i(TAG, "Address list retrieved successfully: ${addressListingResponse.data?.size ?: 0} addresses found")
                        emit(Resource.Success(addressListingResponse))
                    } else {
                        emit(Resource.Error(addressListingResponse.message))
                    }
                } ?: emit(Resource.Error("Empty response from server"))
            } else {
                val errorMessage = "Failed to load addresses: ${response.message()}"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> "No internet connection. Please check your network."
                is SocketTimeoutException -> "Request timeout. Please try again."
                is SSLException -> "Secure connection failed. Please try again."
                is JsonSyntaxException -> "Server returned an invalid response."
                else -> e.message ?: "Unknown error occurred"
            }

            Log.e(TAG, "Exception occurred during getAddressList: $errorMessage", e)
            emit(Resource.Error(errorMessage))
        }
    }

    // =================== ADD ADDRESS ===================

    suspend fun addAddress(addressDetails: AddressDetails): Resource<AddressResponse> {
        Log.d(TAG, "=== Starting addAddress operation ===")

        try {
            if (!userPreferencesManager.validateAuthenticationState()) {
                Log.e(TAG, "User not authenticated")
                return Resource.Error("Authentication required. Please login again.")
            }

            val token = userPreferencesManager.getJwtToken()
            if (token.isNullOrBlank()) {
                Log.e(TAG, "No valid JWT token found")
                return Resource.Error("Authentication token expired. Please login again.")
            }

            Log.d(TAG, "Input AddressDetails: ${gson.toJson(addressDetails)}")
            Log.d(TAG, "JWT token available: ${token.take(10)}...")

            // Fetch current location
            Log.i(TAG, "Fetching current location...")
            val currentLocation = locationManager.getCurrentLocation()
            val latLongString = "${currentLocation.latitude},${currentLocation.longitude}"

            Log.i(TAG, "Location fetched - Latitude: ${currentLocation.latitude}, Longitude: ${currentLocation.longitude}")
            Log.d(TAG, "Formatted lat,long string: $latLongString")

            // Create and log address request with location
            val addressRequest = AddressRequest(
                fullName = addressDetails.fullName,
                phoneNumber = addressDetails.phoneNumber,
                altPhoneNumber = addressDetails.altPhoneNumber.takeIf { it.isNotBlank() },
                state = addressDetails.state,
                city = addressDetails.city,
                pincode = addressDetails.pincode,
                address = "${addressDetails.houseNo}, ${addressDetails.locality}, ${addressDetails.district}".trim().replace(", ,", ","),
                landmark = addressDetails.locality,
                buildingNoOrName = addressDetails.houseNo,
                latitudeAndLongitude = latLongString
            )

            Log.d(TAG, "=== COMPLETE API PAYLOAD ===")
            Log.d(TAG, "Created AddressRequest: ${gson.toJson(addressRequest)}")
            Log.d(TAG, "=== END PAYLOAD ===")

            Log.i(TAG, "Making API call to add address...")

            val bearerToken = "Bearer $token"
            val response = apiService.addAddress(bearerToken, "", addressRequest)

            Log.d(TAG, "API Response - Code: ${response.code()}")

            if (response.isSuccessful) {
                Log.i(TAG, "API call successful")

                response.body()?.let { addressResponse ->
                    Log.d(TAG, "Response Body: ${gson.toJson(addressResponse)}")

                    if (addressResponse.success) {
                        Log.i(TAG, "Address added successfully: ${addressResponse.message}")
                        return Resource.Success(addressResponse)
                    } else {
                        Log.w(TAG, "API returned success=false with message: ${addressResponse.message}")
                        return Resource.Error(addressResponse.message)
                    }
                } ?: run {
                    Log.e(TAG, "Response body is null despite successful response")
                    return Resource.Error("Empty response from server")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "API Error - Code: ${response.code()}")
                Log.e(TAG, "API Error Body: $errorBody")

                if (errorBody?.contains("<html>") == true) {
                    Log.e(TAG, "Server returned HTML error page instead of JSON")
                    return Resource.Error("Server error. Please check your internet connection and try again.")
                }

                val errorMessage = parseValidationError(errorBody) ?: "Failed to add address: ${response.message()}"
                Log.e(TAG, errorMessage)
                return Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> "No internet connection. Please check your network."
                is SocketTimeoutException -> "Request timeout. Please try again."
                is SSLException -> "Secure connection failed. Please try again."
                is JsonSyntaxException -> "Server returned an invalid response."
                else -> e.message ?: "Unknown error occurred"
            }

            Log.e(TAG, "Exception occurred during addAddress: $errorMessage", e)
            return Resource.Error(errorMessage)
        }
    }

    // =================== EDIT ADDRESS ===================

    // =================== EDIT ADDRESS ===================

    suspend fun editAddress(addressDetails: AddressDetails): Resource<EditAddressResponse> {
        Log.d(TAG, "=== Starting editAddress operation ===")

        try {
            if (!userPreferencesManager.validateAuthenticationState()) {
                Log.e(TAG, "User not authenticated for edit")
                return Resource.Error("Authentication required. Please login again.")
            }

            val token = userPreferencesManager.getJwtToken()
            if (token.isNullOrBlank()) {
                Log.e(TAG, "No valid JWT token found for edit")
                return Resource.Error("Authentication token expired. Please login again.")
            }

            val addressId = addressDetails.addressId
                ?: sharedPreferencesManager.getAddressToEditId()
                ?: return Resource.Error("Address ID not found for editing")

            Log.d(TAG, "Editing address with ID: $addressId")

            val currentLocation = locationManager.getCurrentLocation()
            val latLongString = "${currentLocation.latitude},${currentLocation.longitude}"

            val editRequest = EditAddressRequest(
                address_id = addressId,
                full_name = addressDetails.fullName,
                phone_number = addressDetails.phoneNumber,
                alt_phone_number = addressDetails.altPhoneNumber.takeIf { it.isNotBlank() },
                state = addressDetails.state,
                city = addressDetails.city,
                pincode = addressDetails.pincode,
                address = "${addressDetails.houseNo}, ${addressDetails.locality}, ${addressDetails.district}".trim().replace(", ,", ","),
                latitude_and_longitude = latLongString,
                landmark = addressDetails.locality.takeIf { it.isNotBlank() },
                address_type = addressDetails.addressType,
                building_no_or_name = addressDetails.houseNo
            )

            Log.d(TAG, "Making API call to edit address ID: $addressId")
            Log.d(TAG, "Edit request payload: ${gson.toJson(editRequest)}")

            val bearerToken = "Bearer $token"
            val response = apiService.editAddress(bearerToken, "", editRequest)

            Log.d(TAG, "Edit API Response - Code: ${response.code()}")
            Log.d(TAG, "Edit API Response Headers: ${response.headers()}")

            // Handle specific HTTP error codes before checking success
            when (response.code()) {
                401 -> {
                    Log.e(TAG, "Authentication failed during edit - clearing user session")
                    userPreferencesManager.clearUserAuthData()
                    return Resource.Error("Authentication failed. Please login again.")
                }
                403 -> return Resource.Error("Access denied. Please check your permissions.")
                404 -> return Resource.Error("Address not found or endpoint unavailable.")
                405 -> {
                    Log.e(TAG, "Method not allowed - server expects different HTTP method")
                    val allowedMethods = response.headers()["allow"] ?: "unknown"
                    Log.e(TAG, "Server allows: $allowedMethods")
                    return Resource.Error("Server error: Invalid request method. Please contact support.")
                }
                422 -> {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseValidationError(errorBody) ?: "Validation failed"
                    Log.e(TAG, "Validation error: $errorMessage")
                    return Resource.Error(errorMessage)
                }
                500, 502, 503, 504 -> return Resource.Error("Server error. Please try again later.")
            }

            if (response.isSuccessful) {
                // Check content type before attempting to parse JSON
                val contentType = response.headers()["content-type"] ?: ""
                Log.d(TAG, "Response content type: $contentType")

                if (contentType.contains("text/html")) {
                    Log.e(TAG, "Server returned HTML instead of JSON - this is a server configuration issue")
                    Log.e(TAG, "HTML response received with 200 status code")
                    return Resource.Error("Server configuration error: Expected JSON response but received HTML. Please contact API support.")
                }

                if (!contentType.contains("application/json")) {
                    Log.e(TAG, "Server returned unexpected content type: $contentType")
                    return Resource.Error("Server returned unexpected response format: $contentType")
                }

                try {
                    response.body()?.let { editResponse ->
                        Log.d(TAG, "Edit response body: ${gson.toJson(editResponse)}")

                        if (editResponse.success) {
                            Log.i(TAG, "Address edited successfully: ${editResponse.message}")
                            sharedPreferencesManager.clearAddressToEdit()
                            sharedPreferencesManager.clearSelectedAddressForOperations()
                            return Resource.Success(editResponse)
                        } else {
                            Log.w(TAG, "Edit API returned success=false: ${editResponse.message}")
                            return Resource.Error(editResponse.message)
                        }
                    } ?: run {
                        Log.e(TAG, "Edit response body is null despite successful HTTP status")
                        return Resource.Error("Server returned empty response")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse successful response: ${e.message}", e)
                    return Resource.Error("Failed to parse server response: ${e.message}")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Edit API Error - Code: ${response.code()}")
                Log.e(TAG, "Edit API Error Body: $errorBody")

                // Check if server returned HTML instead of JSON
                if (errorBody?.trimStart()?.startsWith("<!DOCTYPE html>", ignoreCase = true) == true ||
                    errorBody?.trimStart()?.startsWith("<html>", ignoreCase = true) == true) {
                    Log.e(TAG, "Server returned HTML error page instead of JSON")
                    return Resource.Error("Server error occurred. Please check your connection and try again.")
                }

                val errorMessage = parseValidationError(errorBody) ?: "Edit operation failed: ${response.message()}"
                return Resource.Error(errorMessage)
            }

        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> "No internet connection. Please check your network."
                is SocketTimeoutException -> "Request timeout. Please try again."
                is SSLException -> "Secure connection failed. Please try again."
                is JsonSyntaxException -> "Server returned an invalid response."
                else -> e.message ?: "Unknown error occurred"
            }

            Log.e(TAG, "Exception occurred during editAddress: $errorMessage", e)
            return Resource.Error(errorMessage)
        }
    }

    // =================== DELETE ADDRESS ===================

    suspend fun deleteAddress(): Resource<DeleteAddressResponse> {
        Log.d(TAG, "=== Starting deleteAddress operation ===")

        try {
            if (!userPreferencesManager.validateAuthenticationState()) {
                Log.e(TAG, "User not authenticated for delete")
                return Resource.Error("Authentication required. Please login again.")
            }

            val token = userPreferencesManager.getJwtToken()
            if (token.isNullOrBlank()) {
                Log.e(TAG, "No valid JWT token found for delete")
                return Resource.Error("Authentication token expired. Please login again.")
            }

            val addressId = sharedPreferencesManager.getAddressToDeleteId()
                ?: return Resource.Error("No address selected for deletion")

            Log.d(TAG, "Deleting address with ID: $addressId")

            val deleteRequest = DeleteAddressRequest(
                addressId = addressId
            )

            Log.d(TAG, "Making API call to delete address ID: $addressId")
            Log.d(TAG, "Delete request payload: ${gson.toJson(deleteRequest)}")

            val bearerToken = "Bearer $token"
            val response = apiService.deleteAddress(bearerToken, "", deleteRequest)

            Log.d(TAG, "Delete API Response - Code: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { deleteResponse ->
                    Log.d(TAG, "Delete response body: ${gson.toJson(deleteResponse)}")

                    if (deleteResponse.success) {
                        Log.i(TAG, "Address deleted successfully: ${deleteResponse.message}")
                        sharedPreferencesManager.clearAddressToDelete()
                        sharedPreferencesManager.clearSelectedAddressForOperations()
                        return Resource.Success(deleteResponse)
                    } else {
                        Log.w(TAG, "Delete API returned success=false: ${deleteResponse.message}")
                        return Resource.Error(deleteResponse.message)
                    }
                } ?: run {
                    Log.e(TAG, "Delete response body is null")
                    return Resource.Error("Empty response from server")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseValidationError(errorBody) ?: "Delete operation failed: ${response.message()}"
                Log.e(TAG, "$errorMessage, Error body: $errorBody")
                return Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> "No internet connection. Please check your network."
                is SocketTimeoutException -> "Request timeout. Please try again."
                is SSLException -> "Secure connection failed. Please try again."
                is JsonSyntaxException -> "Server returned an invalid response."
                else -> e.message ?: "Unknown error occurred"
            }

            Log.e(TAG, "Exception occurred during deleteAddress: $errorMessage", e)
            return Resource.Error(errorMessage)
        }
    }

    // =================== DELETE ADDRESS BY ID ===================

    suspend fun deleteAddressById(addressId: Int): Resource<DeleteAddressResponse> {
        Log.d(TAG, "=== Starting deleteAddressById operation for ID: $addressId ===")
        sharedPreferencesManager.storeAddressToDelete(addressId)
        return deleteAddress()
    }

    // =================== HELPER METHODS ===================

    private fun parseValidationError(errorBody: String?): String? {
        return try {
            errorBody?.let {
                val json = JSONObject(it)
                json.optString("message").takeIf { message -> message.isNotBlank() }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse validation error: ${e.message}")
            null
        }
    }

    // =================== UTILITY METHODS ===================

    fun isAuthenticated(): Boolean = userPreferencesManager.validateAuthenticationState()
    //fun getUserData() = userPreferencesManager.get
    fun debugAuthState() = userPreferencesManager.debugCurrentState()
}
