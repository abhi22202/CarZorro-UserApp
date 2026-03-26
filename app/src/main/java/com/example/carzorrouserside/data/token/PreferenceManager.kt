package com.example.carzorrouserside.data.token

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.carzorrouserside.data.model.loginscreen.UserSessionData
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

// ✅ Define ONE global DataStore outside the class (very important)
private const val DATASTORE_NAME = "user_prefs"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val TAG = "UserPreferencesManager"
        private const val PREFS_NAME = "carzorro_user_prefs"
        private const val KEY_JWT_TOKEN = "user_jwt_token"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "user_is_logged_in"
        private const val KEY_TOKEN_EXPIRY = "user_token_expiry"
        private const val TOKEN_VALIDITY_DURATION = 24 * 60 * 60 * 1000L // 24 hours
        private const val KEY_PENDING_BOOKING_ID = "pending_booking_id"
        private const val KEY_PENDING_ROUTE = "pending_booking_route"
        private const val KEY_PENDING_SHEET = "pending_sheet"
        private const val KEY_PENDING_ROUTE_ARGS = "pending_booking_route_args"
    }

    private val gson = Gson()
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        validateAuthenticationState()
        Log.d(TAG, "🚀 UserPreferencesManager initialized")
    }

    // ✅ -------------------- BOOKING STATUS (NEW) -------------------- ✅
    suspend fun saveBookingStatus(status: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("booking_status")] = status
        }
        Log.d(TAG, "💾 Booking status saved: $status")
    }

    suspend fun getBookingStatus(): String? {
        val prefs = context.dataStore.data.first()
        val status = prefs[stringPreferencesKey("booking_status")]
        Log.d(TAG, "📦 Loaded booking status: $status")
        return status
    }
    // ✅ -------------------------------------------------------------- ✅

    fun saveUserLoginData(
        token: String,
        userId: Int,
        phone: String?,
        tokenExpiryMillis: Long = System.currentTimeMillis() + TOKEN_VALIDITY_DURATION
    ) {
        prefs.edit().apply {
            putString(KEY_JWT_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_PHONE, phone)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_TOKEN_EXPIRY, tokenExpiryMillis)
        }.apply()
        validateAuthenticationState()
    }

    fun getJwtToken(): String? {
        val token = prefs.getString(KEY_JWT_TOKEN, null)
        return if (token != null && !isTokenExpired()) token else null
    }

    fun getRawJwtToken(): String? = prefs.getString(KEY_JWT_TOKEN, null)

    fun getUserId(): Int? = prefs.getInt(KEY_USER_ID, -1).takeIf { it > 0 }

    fun getUserPhone(): String? = prefs.getString(KEY_USER_PHONE, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun isTokenExpired(): Boolean {
        val expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() >= expiry
    }

    fun getTimeUntilTokenExpiry(): Long {
        val expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return (expiry - System.currentTimeMillis()).coerceAtLeast(0)
    }

    fun validateAuthenticationState(): Boolean {
        val isValid = !getJwtToken().isNullOrBlank() && getUserId() != null && isLoggedIn()
        _isAuthenticated.value = isValid
        return isValid
    }

    fun clearUserAuthData() {
        prefs.edit().apply {
            remove(KEY_JWT_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_PHONE)
            remove(KEY_TOKEN_EXPIRY)
            putBoolean(KEY_IS_LOGGED_IN, false)
        }.apply()
        _isAuthenticated.value = false
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        _isAuthenticated.value = false
    }

    fun debugCurrentState() {
        Log.d(TAG, "=== USER PREFS ===")
        Log.d(TAG, "User ID: ${getUserId()}")
        Log.d(TAG, "Token: ${getJwtToken()?.take(10)}...")
        Log.d(TAG, "Is Logged In: ${isLoggedIn()}")
        Log.d(TAG, "Token Expired: ${isTokenExpired()}")
        Log.d(TAG, "Auth State: ${validateAuthenticationState()}")
    }

    data class PendingBookingData(
        val id: Int,
        val route: String,
        val argsJson: String?,
        val sheetType: String
    )

    fun savePendingBooking(
        pendingBookingId: Int,
        route: String,
        routeArgsJson: String? = null,
        sheetType: String = "BOOKING"
    ) {
        prefs.edit().apply {
            putInt(KEY_PENDING_BOOKING_ID, pendingBookingId)
            putString(KEY_PENDING_ROUTE, route)
            putString(KEY_PENDING_ROUTE_ARGS, routeArgsJson)
            putString(KEY_PENDING_SHEET, sheetType)
        }.apply()
        Log.d(TAG, "💾 Saved pending booking (route=$route, sheet=$sheetType)")
    }

    fun getPendingBooking(): PendingBookingData? {
        val id = prefs.getInt(KEY_PENDING_BOOKING_ID, -1)
        val route = prefs.getString(KEY_PENDING_ROUTE, null)
        val args = prefs.getString(KEY_PENDING_ROUTE_ARGS, null)
        val sheet = prefs.getString(KEY_PENDING_SHEET, "BOOKING") ?: "BOOKING"
        if (route == null) return null

        return PendingBookingData(id, route, args, sheet)
    }

    fun clearPendingBooking() {
        prefs.edit().apply {
            remove(KEY_PENDING_BOOKING_ID)
            remove(KEY_PENDING_ROUTE)
            remove(KEY_PENDING_ROUTE_ARGS)
            remove(KEY_PENDING_SHEET)
        }.apply()
        Log.d(TAG, "🧹 Cleared pending booking (including sheet type)")
    }
}
