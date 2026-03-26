package com.example.carzorrouserside.utils.token

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavController

import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.util.SnackbarEvent
import com.example.carzorrouserside.util.SnackbarType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAutomatedTokenExpirationHandler @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val application: Application
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "UserAutoTokenHandler"
        private const val TOKEN_CHECK_INTERVAL = 30 * 1000L // Check every 30 seconds
        private const val TOKEN_WARNING_THRESHOLD = 2 * 60 * 1000L // 2 minutes warning

        @Volatile
        private var INSTANCE: UserAutomatedTokenExpirationHandler? = null

        fun getInstance(): UserAutomatedTokenExpirationHandler? = INSTANCE
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var navControllerRef: WeakReference<NavController>? = null
    private val _globalSnackbarEvents = MutableSharedFlow<SnackbarEvent>()
    val globalSnackbarEvents: SharedFlow<SnackbarEvent> = _globalSnackbarEvents.asSharedFlow()

    private var tokenMonitoringJob: Job? = null
    private var isLogoutInProgress = false

    init {
        INSTANCE = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d(TAG, "🚀 UserAutomatedTokenExpirationHandler initialized - AUTO LOGOUT ENABLED")

        // Start monitoring immediately when app starts
        startTokenMonitoring()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "📱 App foreground - starting aggressive token monitoring")
        startTokenMonitoring()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "📱 App background - continuing token monitoring")
        // Don't stop monitoring in background - keep checking for expiry
    }

    fun registerNavController(navController: NavController) {
        navControllerRef = WeakReference(navController)
        Log.d(TAG, "📱 NavController registered - AUTO LOGOUT READY")
        startTokenMonitoring()
    }

    fun unregisterNavController() {
        navControllerRef = null
        Log.d(TAG, "📱 NavController unregistered")
    }

    /**
     * AGGRESSIVE TOKEN MONITORING - Checks every 30 seconds
     */
    private fun startTokenMonitoring() {
        if (tokenMonitoringJob?.isActive == true) {
            Log.d(TAG, "⏰ Token monitoring already active")
            return
        }

        tokenMonitoringJob = applicationScope.launch {
            Log.d(TAG, "⏰ ========== STARTING AGGRESSIVE TOKEN MONITORING ==========")
            Log.d(TAG, "🔄 Check interval: ${TOKEN_CHECK_INTERVAL / 1000} seconds")
            Log.d(TAG, "⚠️ Warning threshold: ${TOKEN_WARNING_THRESHOLD / 1000 / 60} minutes")

            while (isActive) {
                try {
                    if (userPreferencesManager.getJwtToken() != null) {
                        checkTokenExpiryAggressively()
                    } else {
                        Log.v(TAG, "⏸️ User not logged in - pausing monitoring")
                    }

                    delay(TOKEN_CHECK_INTERVAL)

                } catch (e: CancellationException) {
                    Log.d(TAG, "⏰ Token monitoring cancelled")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error in token monitoring", e)
                    delay(TOKEN_CHECK_INTERVAL)
                }
            }
        }
    }

    private fun stopTokenMonitoring() {
        tokenMonitoringJob?.cancel()
        tokenMonitoringJob = null
        Log.d(TAG, "⏰ Token monitoring stopped")
    }

    /**
     * AGGRESSIVE TOKEN EXPIRY CHECK
     */
    private suspend fun checkTokenExpiryAggressively() {
        if (isLogoutInProgress) {
            return
        }

        val token = userPreferencesManager.getJwtToken()
        if (token == null) {
            Log.d(TAG, "🔓 No token available")
            return
        }

        val isExpired = userPreferencesManager.isTokenExpired()
        val timeUntilExpiry = userPreferencesManager.getTimeUntilTokenExpiry()

        Log.v(TAG, "🔍 Token check: Expired=$isExpired, TimeLeft=${timeUntilExpiry/1000}s")

        when {
            isExpired -> {
                Log.w(TAG, "⛔ ========== TOKEN EXPIRED - TRIGGERING AUTO LOGOUT ==========")
                handleTokenExpiration("Your session has expired. Please login again.")
            }

            timeUntilExpiry > 0 && timeUntilExpiry <= TOKEN_WARNING_THRESHOLD -> {
                val minutesLeft = timeUntilExpiry / (60 * 1000)
                Log.w(TAG, "⚠️ Token expires in $minutesLeft minutes - showing warning")
                showTokenExpiryWarning(minutesLeft)
            }
        }
    }

    private suspend fun showTokenExpiryWarning(minutesLeft: Long) {
        try {
            _globalSnackbarEvents.emit(
                SnackbarEvent(
                    message = "Session expires in $minutesLeft minutes",
                    type = SnackbarType.WARNING
                )
            )
            Log.d(TAG, "⚠️ Token expiry warning shown: $minutesLeft minutes")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing expiry warning", e)
        }
    }

    /**
     * FORCE TOKEN EXPIRY CHECK (called from interceptors, repositories)
     */
    fun forceTokenCheck() {
        if (isLogoutInProgress) {
            return
        }

        Log.d(TAG, "🔄 ========== FORCE TOKEN CHECK ==========")
        applicationScope.launch {
            checkTokenExpiryAggressively()
        }
    }

    /**
     * HANDLE TOKEN EXPIRATION - AUTOMATIC LOGOUT & NAVIGATION
     */
    fun handleTokenExpiration(
        errorMessage: String = "Your session has expired. Please login again.",
        shouldShowSnackbar: Boolean = true
    ) {
        if (isLogoutInProgress) {
            Log.d(TAG, "🔄 Logout already in progress")
            return
        }

        isLogoutInProgress = true

        Log.w(TAG, "🚨 ========== AUTO TOKEN EXPIRATION LOGOUT ==========")
        Log.w(TAG, "📝 Reason: $errorMessage")
        Log.w(TAG, "🔄 Clearing session and navigating to login...")

        applicationScope.launch {
            try {
                // 1. Clear all authentication data immediately
                Log.d(TAG, "🧹 Step 1: Clearing authentication data...")
                userPreferencesManager.clearUserAuthData()
                Log.d(TAG, "✅ Authentication data cleared")

                // 2. Show logout message if requested
                if (shouldShowSnackbar) {
                    Log.d(TAG, "📢 Step 2: Showing logout snackbar...")
                    _globalSnackbarEvents.emit(
                        SnackbarEvent(
                            message = errorMessage,
                            type = SnackbarType.ERROR
                        )
                    )
                    Log.d(TAG, "✅ Logout snackbar emitted")
                }

                // 3. Small delay to ensure state updates
                delay(300)

                // 4. Force navigation to login
                Log.d(TAG, "🏃 Step 3: Force navigating to login...")
                forceNavigateToLogin()

                Log.w(TAG, "✅ ========== AUTO LOGOUT COMPLETED ==========")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in auto logout process", e)
                // Still try to navigate even if there's an error
                forceNavigateToLogin()
            } finally {
                isLogoutInProgress = false
            }
        }
    }

    /**
     * FORCE NAVIGATE TO LOGIN SCREEN
     */
    private fun forceNavigateToLogin() {
        val navController = navControllerRef?.get()
        if (navController != null) {
            try {
                Log.d(TAG, "🏃 Force navigating to login screen...")
                Log.d(TAG, "📍 Current destination: ${navController.currentDestination?.route}")

                // Clear entire back stack and navigate to login
                navController.navigate(Routes.LOGIN_SCREEN) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }

                Log.d(TAG, "✅ Successfully navigated to login")
                Log.d(TAG, "📍 New destination: ${navController.currentDestination?.route}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to navigate to login", e)

                // Fallback: Try alternative navigation approach
                try {
                    Log.d(TAG, "🔄 Trying alternative navigation...")
                    navController.popBackStack(0, true)
                    navController.navigate(Routes.LOGIN_SCREEN)
                    Log.d(TAG, "✅ Alternative navigation successful")
                } catch (e2: Exception) {
                    Log.e(TAG, "❌ Alternative navigation also failed", e2)
                }
            }
        } else {
            Log.w(TAG, "⚠️ NavController not available - cannot auto-navigate")
            Log.w(TAG, "💡 User will need to restart app or navigate manually")
        }
    }

    /**
     * CHECK API ERROR RESPONSES FOR TOKEN EXPIRY
     */
    fun checkAndHandleTokenExpiration(
        errorCode: Int,
        errorMessage: String?,
        customMessage: String? = null
    ): Boolean {
        val isExpired = when {
            errorCode == 401 -> true
            errorCode == 403 && errorMessage?.contains("token", ignoreCase = true) == true -> true
            errorCode == 403 && errorMessage?.contains("expired", ignoreCase = true) == true -> true
            errorCode == 403 && errorMessage?.contains("unauthorized", ignoreCase = true) == true -> true
            errorMessage?.contains("jwt", ignoreCase = true) == true &&
                    errorMessage.contains("expired", ignoreCase = true) -> true
            else -> false
        }

        Log.d(TAG, "🔍 API error check:")
        Log.d(TAG, "  ├─ Error Code: $errorCode")
        Log.d(TAG, "  ├─ Error Message: $errorMessage")
        Log.d(TAG, "  └─ Token Expired: $isExpired")

        if (isExpired) {
            val message = customMessage ?: "Your session has expired. Please login again."
            Log.w(TAG, "🚨 API indicates token expired - triggering auto logout")
            handleTokenExpiration(message)
        }

        return isExpired
    }

    fun cleanup() {
        Log.d(TAG, "🧹 Cleaning up UserAutomatedTokenExpirationHandler")
        stopTokenMonitoring()
        applicationScope.cancel()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        navControllerRef = null
        isLogoutInProgress = false
        INSTANCE = null
    }

    fun isLogoutInProgress(): Boolean = isLogoutInProgress
}