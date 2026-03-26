package com.example.carzorrouserside.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.carzorrouserside.data.model.homescreen.AddressItem
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "SharedPreferencesManager"
        private const val PREFS_NAME = "carzorro_address_prefs"

        // Address related keys
        private const val KEY_SELECTED_ADDRESS = "selected_address_json"
        private const val KEY_LAST_LOCATION = "last_location"
        private const val KEY_ADDRESS_COUNT = "address_count"
        private const val KEY_LAST_UPDATE_TIME = "last_update_time"

        // Address operations keys
        private const val KEY_ADDRESS_TO_DELETE_ID = "address_to_delete_id"
        private const val KEY_ADDRESS_TO_EDIT_ID = "address_to_edit_id"
        private const val KEY_SELECTED_ADDRESS_FOR_OPERATIONS = "selected_address_for_operations"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // =================== ADDRESS SELECTION FOR HOME ===================

    fun saveSelectedAddress(address: AddressItem) {
        try {
            val addressJson = gson.toJson(address)
            sharedPreferences.edit()
                .putString(KEY_SELECTED_ADDRESS, addressJson)
                .putLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis())
                .apply()

            Log.d(TAG, "Selected address saved successfully: ${address.fullName} in ${address.city}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save selected address: ${e.message}", e)
        }
    }

    fun getSelectedAddress(): AddressItem? {
        return try {
            val addressJson = sharedPreferences.getString(KEY_SELECTED_ADDRESS, null)

            if (addressJson.isNullOrEmpty()) {
                Log.d(TAG, "No saved address found in preferences")
                return null
            }

            val address = gson.fromJson(addressJson, AddressItem::class.java)
            Log.d(TAG, "Loaded selected address: ${address.fullName} in ${address.city}")
            address
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Invalid JSON format for saved address: ${e.message}", e)
            clearSelectedAddress()
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load selected address: ${e.message}", e)
            null
        }
    }

    fun clearSelectedAddress() {
        try {
            sharedPreferences.edit()
                .remove(KEY_SELECTED_ADDRESS)
                .remove(KEY_LAST_UPDATE_TIME)
                .apply()
            Log.d(TAG, "Cleared selected address from preferences")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear selected address: ${e.message}", e)
        }
    }

    fun hasSelectedAddress(): Boolean {
        return sharedPreferences.contains(KEY_SELECTED_ADDRESS) &&
                !sharedPreferences.getString(KEY_SELECTED_ADDRESS, null).isNullOrEmpty()
    }

    fun isSelectedAddressValid(maxAgeHours: Long): Boolean {
        return try {
            if (!hasSelectedAddress()) return false

            val lastUpdate = getLastUpdateTime()
            val currentTime = System.currentTimeMillis()
            val maxAge = maxAgeHours * 60 * 60 * 1000L

            val isValid = (currentTime - lastUpdate) <= maxAge
            Log.d(TAG, "Address validity check: $isValid (age: ${(currentTime - lastUpdate) / 1000 / 60} minutes)")

            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check address validity: ${e.message}", e)
            false
        }
    }

    // =================== ADDRESS OPERATIONS MANAGEMENT ===================

    fun storeAddressToDelete(addressId: Int) {
        Log.d(TAG, "Storing address ID for deletion: $addressId")
        sharedPreferences.edit().putInt(KEY_ADDRESS_TO_DELETE_ID, addressId).apply()
    }

    fun getAddressToDeleteId(): Int? {
        val addressId = sharedPreferences.getInt(KEY_ADDRESS_TO_DELETE_ID, -1)
        return if (addressId != -1) {
            Log.d(TAG, "Retrieved address ID for deletion: $addressId")
            addressId
        } else {
            Log.w(TAG, "No address ID stored for deletion")
            null
        }
    }

    fun clearAddressToDelete() {
        Log.d(TAG, "Clearing stored address ID for deletion")
        sharedPreferences.edit().remove(KEY_ADDRESS_TO_DELETE_ID).apply()
    }

    fun storeAddressToEdit(addressId: Int) {
        Log.d(TAG, "Storing address ID for editing: $addressId")
        sharedPreferences.edit().putInt(KEY_ADDRESS_TO_EDIT_ID, addressId).apply()
    }

    fun getAddressToEditId(): Int? {
        val addressId = sharedPreferences.getInt(KEY_ADDRESS_TO_EDIT_ID, -1)
        return if (addressId != -1) {
            Log.d(TAG, "Retrieved address ID for editing: $addressId")
            addressId
        } else {
            Log.w(TAG, "No address ID stored for editing")
            null
        }
    }

    fun clearAddressToEdit() {
        Log.d(TAG, "Clearing stored address ID for editing")
        sharedPreferences.edit().remove(KEY_ADDRESS_TO_EDIT_ID).apply()
    }

    fun storeSelectedAddressForOperations(address: AddressItem) {
        Log.d(TAG, "Storing complete address for operations: ${address.fullName}")
        val addressJson = gson.toJson(address)
        sharedPreferences.edit().putString(KEY_SELECTED_ADDRESS_FOR_OPERATIONS, addressJson).apply()
    }

    fun getSelectedAddressForOperations(): AddressItem? {
        val addressJson = sharedPreferences.getString(KEY_SELECTED_ADDRESS_FOR_OPERATIONS, null)
        return if (!addressJson.isNullOrBlank()) {
            try {
                val address = gson.fromJson(addressJson, AddressItem::class.java)
                Log.d(TAG, "Retrieved address for operations: ${address.fullName}")
                address
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing stored address for operations: ${e.message}")
                null
            }
        } else {
            Log.w(TAG, "No address stored for operations")
            null
        }
    }

    fun clearSelectedAddressForOperations() {
        Log.d(TAG, "Clearing stored address for operations")
        sharedPreferences.edit().remove(KEY_SELECTED_ADDRESS_FOR_OPERATIONS).apply()
    }

    // =================== LOCATION MANAGEMENT ===================

    fun saveLastLocation(latitude: Double, longitude: Double) {
        try {
            val locationString = "$latitude,$longitude"
            sharedPreferences.edit()
                .putString(KEY_LAST_LOCATION, locationString)
                .apply()
            Log.d(TAG, "Last location saved: $locationString")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save last location: ${e.message}", e)
        }
    }

    fun getLastLocation(): String? {
        return try {
            val location = sharedPreferences.getString(KEY_LAST_LOCATION, null)
            if (location != null) {
                Log.d(TAG, "Loaded last location: $location")
            }
            location
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load last location: ${e.message}", e)
            null
        }
    }

    fun clearLastLocation() {
        try {
            sharedPreferences.edit()
                .remove(KEY_LAST_LOCATION)
                .apply()
            Log.d(TAG, "Cleared last location")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear last location: ${e.message}", e)
        }
    }

    // =================== ADDRESS COUNT MANAGEMENT ===================

    fun saveAddressCount(count: Int) {
        try {
            sharedPreferences.edit()
                .putInt(KEY_ADDRESS_COUNT, count)
                .apply()
            Log.d(TAG, "Address count saved: $count")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save address count: ${e.message}", e)
        }
    }

    fun getAddressCount(): Int {
        return try {
            sharedPreferences.getInt(KEY_ADDRESS_COUNT, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load address count: ${e.message}", e)
            0
        }
    }

    // =================== UTILITY METHODS ===================

    fun getLastUpdateTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_UPDATE_TIME, 0L)
    }

    fun clearAllAddressData() {
        try {
            sharedPreferences.edit()
                .remove(KEY_SELECTED_ADDRESS)
                .remove(KEY_LAST_LOCATION)
                .remove(KEY_ADDRESS_COUNT)
                .remove(KEY_LAST_UPDATE_TIME)
                .remove(KEY_ADDRESS_TO_DELETE_ID)
                .remove(KEY_ADDRESS_TO_EDIT_ID)
                .remove(KEY_SELECTED_ADDRESS_FOR_OPERATIONS)
                .apply()
            Log.d(TAG, "Cleared all address-related data")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all address data: ${e.message}", e)
        }
    }

    fun debugLogStoredData() {
        try {
            Log.d(TAG, "=== STORED PREFERENCES DEBUG ===")
            Log.d(TAG, "Selected Address JSON: ${sharedPreferences.getString(KEY_SELECTED_ADDRESS, "null")}")
            Log.d(TAG, "Last Location: ${sharedPreferences.getString(KEY_LAST_LOCATION, "null")}")
            Log.d(TAG, "Address Count: ${sharedPreferences.getInt(KEY_ADDRESS_COUNT, 0)}")
            Log.d(TAG, "Last Update: ${sharedPreferences.getLong(KEY_LAST_UPDATE_TIME, 0L)}")
            Log.d(TAG, "Address to Delete ID: ${getAddressToDeleteId() ?: "NONE"}")
            Log.d(TAG, "Address to Edit ID: ${getAddressToEditId() ?: "NONE"}")
            Log.d(TAG, "Address for Operations: ${if (getSelectedAddressForOperations() != null) "EXISTS" else "NONE"}")
            Log.d(TAG, "=== END PREFERENCES DEBUG ===")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to debug preferences: ${e.message}", e)
        }
    }
}