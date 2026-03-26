package com.example.carzorrouserside.data.repository.login


import android.util.Log
import com.example.carzorrouserside.data.api.loginscreen.LogoutApiService
import com.example.carzorrouserside.data.model.loginscreen.LogoutResponse
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.data.token.BookingSessionManager
import com.example.carzorrouserside.util.NetworkResult
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutRepository @Inject constructor(
    private val logoutApiService: LogoutApiService,
    private val userPreferencesManager: UserPreferencesManager,
    private val bookingSessionManager: BookingSessionManager,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "LogoutRepository"
    }

    suspend fun logout(): NetworkResult<LogoutResponse> {
        return try {
            Log.d(TAG, "🚪 ===== LOGOUT REQUEST STARTED =====")

            // Get the current JWT token
            val jwtToken = userPreferencesManager.getJwtToken()

            if (jwtToken.isNullOrBlank()) {
                Log.w(TAG, "⚠️ No JWT token found, performing local logout only")
                // Clear local data even if no token
                clearLocalUserData()
                return NetworkResult.Success(
                    LogoutResponse(
                        success = true,
                        message = "Logged out successfully (local)",
                        data = emptyList()
                    )
                )
            }

            Log.d(TAG, "🔑 JWT Token: ${jwtToken.take(50)}...")
            Log.d(TAG, "📡 Making logout API call...")

            // Make the API call
            val response = logoutApiService.logout("Bearer $jwtToken")

            Log.d(TAG, "📡 API Response received:")
            Log.d(TAG, "   ├─ Code: ${response.code()}")
            Log.d(TAG, "   ├─ Message: ${response.message()}")
            Log.d(TAG, "   └─ Is Successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val logoutResponse = response.body()
                Log.d(TAG, "✅ Logout API successful:")
                Log.d(TAG, "   ├─ Success: ${logoutResponse?.success}")
                Log.d(TAG, "   └─ Message: ${logoutResponse?.message}")

                // Clear local data regardless of API response
                clearLocalUserData()
                clearFcmToken()

                NetworkResult.Success(
                    logoutResponse ?: LogoutResponse(
                        success = true,
                        message = "Logout successful",
                        data = emptyList()
                    )
                )
            } else {
                Log.e(TAG, "❌ Logout API failed:")
                Log.e(TAG, "   ├─ Code: ${response.code()}")
                Log.e(TAG, "   └─ Error: ${response.errorBody()?.string()}")

                // Clear local data even if API fails (user should still be logged out locally)
                clearLocalUserData()

                // Return success for local logout even if API fails
                NetworkResult.Success(
                    LogoutResponse(
                        success = true,
                        message = "Logged out successfully (local)",
                        data = emptyList()
                    )
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Logout exception: ${e.message}", e)

            // Clear local data even on exception
            clearLocalUserData()

            // Return success for local logout even on exception
            NetworkResult.Success(
                LogoutResponse(
                    success = true,
                    message = "Logged out successfully (local)",
                    data = emptyList()
                )
            )
        } finally {
            Log.d(TAG, "🚪 ===== LOGOUT REQUEST COMPLETED =====")
        }
    }
    private fun clearFcmToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Delete local FCM token
                FirebaseMessaging.getInstance().deleteToken().await()
                Log.d(TAG, "🧹 Local FCM token deleted successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error clearing local FCM token: ${e.localizedMessage}")
            }
        }
    }


    private fun clearLocalUserData() {
        Log.d(TAG, "🧹 Clearing all local user data...")

        // Clear user preferences and authentication data
        userPreferencesManager.clearUserAuthData()

        // Clear any pending booking data
        bookingSessionManager.clearPendingBooking()

        Log.d(TAG, "🧹 Local data cleared successfully")
    }

    fun isUserLoggedIn(): Boolean {
        return userPreferencesManager.validateAuthenticationState()
    }
}