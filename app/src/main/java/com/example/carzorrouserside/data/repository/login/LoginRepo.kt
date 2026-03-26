package com.example.carzorrouserside.data.repository.login

import android.util.Log
import com.example.carzorrouserside.data.api.loginscreen.FcmTokenRequest
import com.example.carzorrouserside.data.api.loginscreen.UserAuthApiService
import com.example.carzorrouserside.data.model.loginscreen.ErrorResponse
import com.example.carzorrouserside.data.model.loginscreen.SendOtpRequest
import com.example.carzorrouserside.data.model.loginscreen.SendOtpResponse
import com.example.carzorrouserside.data.model.loginscreen.VerifyOtpRequest
import com.example.carzorrouserside.data.model.loginscreen.VerifyOtpResponse
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.util.NetworkResult
import com.example.carzorrouserside.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAuthRepository @Inject constructor(
    private val apiService: UserAuthApiService,
    private val userPreferencesManager: UserPreferencesManager // Inject PreferencesManager
) {
    companion object {
        private const val TAG = "UserAuthRepository"
    }

    suspend fun sendOtp(phone: String): Resource<SendOtpResponse> {
        return try {
            Log.d(TAG, "🚀 Starting sendOtp for phone: $phone")

            val request = SendOtpRequest(phone)
            Log.d(TAG, "📤 Sending request: ${Gson().toJson(request)}")

            val response = apiService.sendOtp(request)

            Log.d(TAG, "📊 Response received:")
            Log.d(TAG, "   ├─ Success: ${response.isSuccessful}")
            Log.d(TAG, "   ├─ Code: ${response.code()}")
            Log.d(TAG, "   └─ Message: ${response.message()}")

            if (response.isSuccessful) {
                response.body()?.let { sendOtpResponse ->
                    Log.d(TAG, "✅ Success Response Body: ${Gson().toJson(sendOtpResponse)}")

                    // Enhanced OTP logging
                    sendOtpResponse.data?.let { data ->
                        Log.d(TAG, "🔢 OTP DETAILS:")
                        Log.d(TAG, "   ├─ Phone: ${data.phone}")
                        Log.d(TAG, "   ├─ OTP: ${data.otp}")
                        Log.d(TAG, "   ├─ Success: ${sendOtpResponse.success}")
                        Log.d(TAG, "   └─ Message: ${sendOtpResponse.message}")
                    }

                    Resource.Success(sendOtpResponse)
                } ?: run {
                    Log.e(TAG, "❌ Response body is null")
                    Resource.Error("Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error Response Body: $errorBody")

                val errorMessage = when (response.code()) {
                    400 -> {
                        try {
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            errorResponse.message ?: "Validation error or User not found"
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing error response: ${e.message}")
                            "Validation error or User not found"
                        }
                    }
                    500 -> "Internal server error"
                    else -> "The selected phone is invalid."
                }
                Log.e(TAG, "❌ Final error message: $errorMessage")
                Resource.Error(errorMessage)
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Validation error or User not found"
                500 -> "Internal server error"
                else -> "Network error: ${e.message()}"
            }
            Log.e(TAG, "❌ HttpException: $errorMessage", e)
            Resource.Error(errorMessage)
        } catch (e: IOException) {
            val errorMessage = "Network connection error. Please check your internet connection."
            Log.e(TAG, "❌ IOException: $errorMessage", e)
            Resource.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "An unexpected error occurred: ${e.localizedMessage}"
            Log.e(TAG, "❌ Exception: $errorMessage", e)
            Resource.Error(errorMessage)
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): NetworkResult<VerifyOtpResponse> {
        return try {
            Log.d(TAG, "🔐 Starting verifyOtp for phone: $phone, OTP: $otp")

            val request = VerifyOtpRequest(phone = phone, otp = otp)
            Log.d(TAG, "📤 Verify request: ${Gson().toJson(request)}")

            val response = apiService.verifyOtp(request)
            Log.d(TAG, "📊 Verify response code: ${response.code()}")

            val result = handleApiResponse(response, phone)
            Log.d(TAG, "📋 Verify result: $result")

            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Verify OTP Exception: ${e.message}", e)
            NetworkResult.Error("Network error: ${e.message}")
        }
    }

    private fun handleApiResponse(response: Response<VerifyOtpResponse>, phone: String): NetworkResult<VerifyOtpResponse> {
        return when {
            response.isSuccessful -> {
                response.body()?.let { body ->
                    Log.d(TAG, "✅ API Response Success: ${Gson().toJson(body)}")

                    // Save user session data if verification is successful
                    if (body.success && body.data != null) {
                        Log.d(TAG, "💾 Saving user session data...")
                        userPreferencesManager.saveUserLoginData(
                            token = body.data.token,
                            userId = body.data.userId,
                            phone = phone
                        )
                        Log.d(TAG, "✅ User session data saved successfully!")

                        // Debug the saved state
                        userPreferencesManager.debugCurrentState()
                    }

                    NetworkResult.Success(body)
                } ?: run {
                    Log.e(TAG, "❌ Empty response body")
                    NetworkResult.Error("Empty response body")
                }
            }
            response.code() == 400 -> {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ 400 Error: $errorBody")
                NetworkResult.Error("Invalid or expired OTP")
            }
            response.code() == 500 -> {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ 500 Error: $errorBody")
                NetworkResult.Error("Server Error")
            }
            else -> {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ ${response.code()} Error: $errorBody")
                NetworkResult.Error("Error: ${response.code()} - ${response.message()}")
            }
        }
    }

    suspend fun updateFcmToken(
        userId: Int,
        fcmToken: String,
        deviceId: String
    ): Resource<Any> {
        return try {
            val request = FcmTokenRequest(
                user_id = userId,
                device_type = "android",
                device_id = deviceId,
                fcm_token = fcmToken
            )
            val response = apiService.updateFcmToken(request)
            Log.d(TAG, "API result: $response")


            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }


    /**
     * Logout user and clear session data
     */
    fun logout() {
        Log.d(TAG, "🚪 User logout initiated")
        userPreferencesManager.clearUserAuthData()
        Log.d(TAG, "✅ User logout completed")
    }

    /**
     * Check if user is currently authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return userPreferencesManager.validateAuthenticationState()
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): Int? {
        return userPreferencesManager.getUserId()
    }

    /**
     * Get current user phone
     */
    fun getCurrentUserPhone(): String? {
        return userPreferencesManager.getUserPhone()
    }

    /**
     * Get current JWT token
     */
    fun getCurrentJwtToken(): String? {
        return userPreferencesManager.getJwtToken()
    }
}