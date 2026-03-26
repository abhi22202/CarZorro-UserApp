package com.example.carzorrouserside.data.token

import android.util.Log
import com.example.carzorrouserside.utils.token.UserAutomatedTokenExpirationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserTokenHandler @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val automatedTokenHandler: UserAutomatedTokenExpirationHandler // 🔥 NEW: Inject the automated handler
) {
    companion object {
        private const val TAG = "UserTokenHandler"
    }

    private val _sessionExpiredEvents = MutableSharedFlow<String>()
    val sessionExpiredEvents: SharedFlow<String> = _sessionExpiredEvents

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    /**
     * Handle user token expiration - now delegates to automated handler
     */
    fun handleTokenExpiration(errorMessage: String = "Your session has expired. Please login again.") {
        Log.w(TAG, "🔒 User session expired: $errorMessage")

        // 🔥 NEW: Delegate to automated handler instead of manual handling
        automatedTokenHandler.handleTokenExpiration(errorMessage)

        // Keep the old flow for backward compatibility
        coroutineScope.launch {
            _sessionExpiredEvents.emit(errorMessage)
        }
    }

    /**
     * Check if error indicates token expiration and delegate to automated handler
     */
    fun checkAndHandleTokenExpiration(errorCode: Int, errorMessage: String?): Boolean {
        // 🔥 NEW: Use automated handler for more comprehensive error checking
        return automatedTokenHandler.checkAndHandleTokenExpiration(errorCode, errorMessage)
    }

    /**
     * 🔥 NEW: Force token check - delegates to automated handler
     */
    fun forceTokenCheck() {
        automatedTokenHandler.forceTokenCheck()
    }

    /**
     * 🔥 NEW: Check if logout is in progress
     */
    fun isLogoutInProgress(): Boolean {
        return automatedTokenHandler.isLogoutInProgress()
    }
}