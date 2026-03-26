package com.example.carzorrouserside.di

import android.util.Base64
import android.util.Log
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.utils.token.UserAutomatedTokenExpirationHandler
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.Date
import javax.inject.Inject

class UserAuthInterceptor @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val tokenHandler: UserAutomatedTokenExpirationHandler
) : Interceptor {

    companion object {
        private const val TAG = "UserAuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // List of endpoints that don't require authentication
        val publicEndpoints = listOf(
            "/v1/user/login/send-otp",
            "/v1/user/login/verify-otp",
            "/v1/user/register",
            "/v1/user/register/verify-otp"
        )

        // Check if this is a public endpoint
        val isPublicEndpoint = publicEndpoints.any { endpoint ->
            originalRequest.url.encodedPath.contains(endpoint)
        }

        // If public endpoint, proceed without auth
        if (isPublicEndpoint) {
            Log.d(TAG, "🔓 Public endpoint, no auth needed: ${originalRequest.url.encodedPath}")
            return chain.proceed(originalRequest)
        }

        // Check if token is expired before using it
        if (userPreferencesManager.isTokenExpired()) {
            Log.w(TAG, "⛔ Token is expired - triggering automatic logout")
            tokenHandler.handleTokenExpiration("Your session has expired. Please login again.")

            // Still proceed with request (it will fail, but that's expected)
            return chain.proceed(originalRequest)
        }

        // Get token
        val token = userPreferencesManager.getJwtToken()

        if (token == null) {
            Log.w(TAG, "⚠️ No valid token available for: ${originalRequest.url.encodedPath}")

            // Trigger force check for token expiry
            tokenHandler.forceTokenCheck()

            // Proceed with original request (will likely fail with 401)
            return chain.proceed(originalRequest)
        }

        // Enhanced JWT token analysis
        analyzeToken(token)

        // Add Authorization header
        val request = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        Log.d(TAG, "🔐 Added Authorization header to request: ${originalRequest.url.encodedPath}")
        Log.d(TAG, "🔐 Token preview: ${token.take(20)}...${token.takeLast(20)}")
        Log.d(TAG, "🔐 Full Authorization header: Bearer ${token.take(50)}...")

        // Proceed with the request
        val response = chain.proceed(request)

        // Check for token expiry responses
        when (response.code) {
            401 -> {
                Log.w(TAG, "🚫 401 Unauthorized - triggering auto logout")
                tokenHandler.handleTokenExpiration("Your session has expired. Please login again.")
            }

            403 -> {
                try {
                    val responseBody = response.peekBody(1024).string()
                    Log.w(TAG, "🚫 403 Forbidden - Response: $responseBody")

                    if (responseBody.contains("token", ignoreCase = true) ||
                        responseBody.contains("expired", ignoreCase = true)
                    ) {
                        Log.w(TAG, "🚫 403 indicates token expiry - triggering auto logout")
                        tokenHandler.handleTokenExpiration("Your session has expired. Please login again.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error reading 403 response body", e)
                }
            }
        }

        return response
    }

    private fun analyzeToken(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e(TAG, "❌ Invalid JWT format")
                return
            }

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), Charset.defaultCharset())
            val payloadJson = JSONObject(payload)
            val exp = payloadJson.optLong("exp", 0)

            if (exp > 0) {
                val expiryDate = Date(exp * 1000)
                val currentTimeSeconds = System.currentTimeMillis() / 1000
                val remainingSeconds = exp - currentTimeSeconds
                val remainingMinutes = remainingSeconds / 60
                val remainingHours = remainingSeconds / 3600.0

                // **BOLD LOGGING FOR JWT EXPIRATION**
                Log.w(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.w(TAG, "🔥 **JWT EXPIRATION FROM BACKEND: ${expiryDate}** 🔥")
                Log.w(
                    TAG,
                    "⏰ **EXPIRES IN: ${
                        String.format(
                            "%.1f",
                            remainingHours
                        )
                    } HOURS (${remainingMinutes} minutes)** ⏰"
                )
                Log.w(TAG, "🕐 **REMAINING SECONDS: ${remainingSeconds}** 🕐")
                Log.w(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Only treat as expired if actually expired (not just less than 1 hour)
                if (remainingSeconds <= 0) {
                    Log.e(TAG, "⛔ **TOKEN IS EXPIRED!** ⛔")
                    tokenHandler.handleTokenExpiration("Your session has expired. Please login again.")
                } else if (remainingSeconds <= 300) { // 5 minutes warning
                    Log.w(TAG, "⚠️ **TOKEN EXPIRING SOON!** ⚠️")
                }
            } else {
                Log.e(TAG, "❌ No expiration claim found in JWT")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to analyze JWT token", e)
        }
    }
}