package com.example.carzorrouserside.data.repository.register




import android.util.Log
import com.example.carzorrouserside.data.api.register.UserAuthRegisterApiService
import com.example.carzorrouserside.data.model.register.ErrorResponse
import com.example.carzorrouserside.data.model.register.RegisterRequest
import com.example.carzorrouserside.data.model.register.RegisterResponse
import com.example.carzorrouserside.data.model.register.SignUpOtpVerificationRequest
import com.example.carzorrouserside.data.model.register.SignUpOtpVerificationResponse

import com.example.carzorrouserside.util.NetworkResult
import com.google.gson.Gson
import retrofit2.Response
import javax.inject.Inject

class UserAuthRegisterRepository @Inject constructor(
    private val apiService:UserAuthRegisterApiService,
    private val gson: Gson
) {

    private val TAG = "UserRegistrationRepository"

    /**
     * Register a new user and get initial OTP
     */
    suspend fun initiateUserRegistration(request: RegisterRequest): NetworkResult<RegisterResponse> {
        return try {
            Log.d(TAG, "🚀 Initiating user registration for: ${request.phone}")
            val response = apiService.initiateUserRegistration(request)
            handleApiResponse(response, "User registration initiated")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Network error during registration: ${e.message}")
            NetworkResult.Error("Network error during registration: ${e.message}")
        }
    }

    /**
     * Verify OTP for registration completion
     */
    suspend fun verifyRegistrationOtp(
        userId: Int,
        otpCode: String
    ): NetworkResult<SignUpOtpVerificationResponse> {
        return try {
            Log.d(TAG, "🔐 Verifying registration OTP for user ID: $userId")

            val request = SignUpOtpVerificationRequest(
                userId = userId,
                otpCode = otpCode
            )

            val response = apiService.verifyRegistrationOtp(request)
            handleApiResponse(response, "OTP verification completed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Network error during OTP verification: ${e.message}")
            NetworkResult.Error("Network error during OTP verification: ${e.message}")
        }
    }

    /**
     * Resend OTP by calling the registration API again with the same user data
     */
    suspend fun resendRegistrationOtp(
        registerRequest: RegisterRequest
    ): NetworkResult<RegisterResponse> {
        return try {
            Log.d(TAG, "🔄 Resending registration OTP for: ${registerRequest.phone}")

            val response = apiService.resendRegistrationOtp(registerRequest)
            handleApiResponse(response, "OTP resent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Network error during OTP resend: ${e.message}")
            NetworkResult.Error("Network error during OTP resend: ${e.message}")
        }
    }

    /**
     * Generic API response handler with comprehensive error handling
     */
    private fun <T> handleApiResponse(
        response: Response<T>,
        successLogMessage: String
    ): NetworkResult<T> {
        return when {
            response.isSuccessful -> {
                response.body()?.let { responseBody ->
                    Log.d(TAG, "✅ $successLogMessage")
                    NetworkResult.Success(responseBody)
                } ?: run {
                    Log.e(TAG, "❌ Empty response body for: $successLogMessage")
                    NetworkResult.Error("Empty response from server")
                }
            }

            response.code() == 400 -> {
                val errorMessage = parseErrorResponse(response.errorBody()?.string())
                    ?: "Invalid request. Please check your input."
                Log.e(TAG, "❌ Bad Request (400): $errorMessage")
                NetworkResult.Error(errorMessage)
            }

            response.code() == 401 -> {
                Log.e(TAG, "❌ Unauthorized (401)")
                NetworkResult.Error("Unauthorized access. Please try again.")
            }

            response.code() == 404 -> {
                Log.e(TAG, "❌ Not Found (404)")
                NetworkResult.Error("Service not available. Please try again later.")
            }

            response.code() == 422 -> {
                val errorMessage = parseErrorResponse(response.errorBody()?.string())
                    ?: "Invalid data provided. Please check your input."
                Log.e(TAG, "❌ Validation Error (422): $errorMessage")
                NetworkResult.Error(errorMessage)
            }

            response.code() == 500 -> {
                Log.e(TAG, "❌ Server Error (500)")
                NetworkResult.Error("Server error. Please try again later.")
            }

            response.code() in 500..599 -> {
                Log.e(TAG, "❌ Server Error (${response.code()})")
                NetworkResult.Error("Server is experiencing issues. Please try again later.")
            }

            else -> {
                val errorMessage = parseErrorResponse(response.errorBody()?.string())
                    ?: "Unknown error occurred. Please try again."
                Log.e(TAG, "❌ Unknown Error (${response.code()}): $errorMessage")
                NetworkResult.Error(errorMessage)
            }
        }
    }

    /**
     * Parse error response from API
     */
    private fun parseErrorResponse(errorBody: String?): String? {
        return try {
            errorBody?.let { body ->
                val errorResponse = gson.fromJson(body, ErrorResponse::class.java)
                errorResponse.message
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to parse error response: ${e.message}")
            null
        }
    }
}