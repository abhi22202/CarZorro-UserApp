package com.example.carzorrouserside.data.token


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.carzorrouserside.data.model.BookingData

import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "BookingSessionManager"
        private const val PREFS_NAME = "carzorro_booking_session"
        private const val KEY_PENDING_BOOKING = "pending_booking_data"
        private const val KEY_BOOKING_FLOW_STATE = "booking_flow_state"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save booking data temporarily during authentication flow
     */
    fun savePendingBooking(bookingData: BookingData) {
        Log.d(TAG, "💾 Saving pending booking data...")
        Log.d(TAG, "   ├─ Vendor ID: ${bookingData.vendorId}")
        Log.d(TAG, "   ├─ Service: ${bookingData.serviceName}")
        Log.d(TAG, "   └─ Total: ₹${bookingData.totalAmount}")

        val bookingJson = gson.toJson(bookingData)
        val result = prefs.edit()
            .putString(KEY_PENDING_BOOKING, bookingJson)
            .putString(KEY_BOOKING_FLOW_STATE, "PAYMENT_PENDING")
            .commit()

        Log.d(TAG, "💾 Save result: ${if (result) "✅ Success" else "❌ Failed"}")
    }

    /**
     * Get pending booking data
     */
    fun getPendingBooking(): BookingData? {
        val bookingJson = prefs.getString(KEY_PENDING_BOOKING, null)
        return if (bookingJson != null) {
            try {
                val bookingData = gson.fromJson(bookingJson, BookingData::class.java)
                Log.d(TAG, "📋 Retrieved pending booking: Vendor ID ${bookingData.vendorId}")
                bookingData
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error parsing booking data: ${e.message}", e)
                null
            }
        } else {
            Log.d(TAG, "📋 No pending booking found")
            null
        }
    }

    /**
     * Check if there's a pending booking
     */
    fun hasPendingBooking(): Boolean {
        val hasPending = prefs.contains(KEY_PENDING_BOOKING)
        Log.d(TAG, "🔍 Has pending booking: $hasPending")
        return hasPending
    }

    /**
     * Clear pending booking data (after successful payment or cancellation)
     */
    fun clearPendingBooking() {
        Log.d(TAG, "🧹 Clearing pending booking data...")
        val result = prefs.edit()
            .remove(KEY_PENDING_BOOKING)
            .remove(KEY_BOOKING_FLOW_STATE)
            .commit()
        Log.d(TAG, "🧹 Clear result: ${if (result) "✅ Success" else "❌ Failed"}")
    }

    /**
     * Get booking flow state
     */
    fun getBookingFlowState(): String? {
        return prefs.getString(KEY_BOOKING_FLOW_STATE, null)
    }

    /**
     * Update booking flow state
     */
    fun updateBookingFlowState(state: String) {
        Log.d(TAG, "🔄 Updating booking flow state to: $state")
        prefs.edit().putString(KEY_BOOKING_FLOW_STATE, state).apply()
    }

    /**
     * Debug current session state
     */
    fun debugSessionState() {
        Log.d(TAG, "🔍 === BOOKING SESSION STATE ===")
        Log.d(TAG, "📱 Has Pending Booking: ${hasPendingBooking()}")
        Log.d(TAG, "📱 Flow State: ${getBookingFlowState()}")
        getPendingBooking()?.let { booking ->
            Log.d(TAG, "📱 Pending Booking Details:")
            Log.d(TAG, "   ├─ Vendor ID: ${booking.vendorId}")
            Log.d(TAG, "   ├─ Service: ${booking.serviceName}")
            Log.d(TAG, "   └─ Total: ₹${booking.totalAmount}")
        }
        Log.d(TAG, "🔍 === END BOOKING SESSION STATE ===")
    }
}