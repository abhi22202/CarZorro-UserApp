package com.example.carzorrouserside.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager as SystemLocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.carzorrouserside.data.model.homescreen.LocationData
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LocationManager"
        // Default location for New Delhi
        private const val DEFAULT_LATITUDE = 28.6139
        private const val DEFAULT_LONGITUDE = 77.2090
        private const val LOCATION_TIMEOUT_MS = 10000L // 10 seconds timeout
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateDistanceMeters(10f)
            .setMaxUpdateDelayMillis(10000L)
            .build()
    }

    /**
     * Get current location with comprehensive fallback strategy
     */
    suspend fun getCurrentLocation(): LocationData {
        Log.d(TAG, "🌍 === STARTING LOCATION FETCH PROCESS ===")

        return try {
            // Step 1: Check permissions explicitly
            val hasPermission = hasLocationPermission()
            Log.d(TAG, "📋 Permission Check: ${if (hasPermission) "✅ GRANTED" else "❌ DENIED"}")

            if (!hasPermission) {
                Log.w(TAG, "❌ Location permission not granted, using default location")
                logPermissionDetails()
                return getDefaultLocation()
            }

            // Step 2: Check if location services are enabled
            val isEnabled = isLocationEnabled()
            Log.d(TAG, "🛰️ Location Services: ${if (isEnabled) "✅ ENABLED" else "❌ DISABLED"}")

            if (!isEnabled) {
                Log.w(TAG, "❌ Location services disabled, using default location")
                return getDefaultLocation()
            }

            // Step 3: Try to get last known location first (faster)
            Log.d(TAG, "🔄 Attempting to get last known location...")
            val lastKnownLocation = getLastKnownLocationSafe()
            if (lastKnownLocation != null) {
                Log.i(TAG, "⚡ Got last known location: ${lastKnownLocation.latitude}, ${lastKnownLocation.longitude}")
                return lastKnownLocation
            }

            // Step 4: Get fresh current location
            Log.d(TAG, "🎯 Fetching fresh current location...")
            val currentLocation = getCurrentLocationFromGPSSafe()

            if (currentLocation != null) {
                Log.i(TAG, "🎉 Fresh location obtained: ${currentLocation.latitude}, ${currentLocation.longitude}")
                return currentLocation
            } else {
                Log.w(TAG, "⚠️ Fresh location failed, using default")
                return getDefaultLocation()
            }

        } catch (e: Exception) {
            Log.e(TAG, "💥 Error getting current location: ${e.message}", e)
            getDefaultLocation()
        }.also { location ->
            Log.d(TAG, "🏁 === FINAL LOCATION RESULT ===")
            Log.d(TAG, "📍 Latitude: ${location.latitude}")
            Log.d(TAG, "📍 Longitude: ${location.longitude}")
            Log.d(TAG, "🏁 === END LOCATION PROCESS ===")
        }
    }

    /**
     * Get last known location with explicit permission check
     */
    private suspend fun getLastKnownLocationSafe(): LocationData? = suspendCancellableCoroutine { continuation ->
        try {
            // Explicit permission check before calling location APIs
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

                Log.w(TAG, "❌ No location permissions for last known location")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            Log.d(TAG, "📡 Requesting last known location...")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        Log.d(TAG, "📌 Last known location found: ${it.latitude}, ${it.longitude}")
                        // Check if location is not too old (within 5 minutes)
                        val locationAge = System.currentTimeMillis() - it.time
                        if (locationAge < 300000) { // 5 minutes
                            Log.d(TAG, "✅ Last known location is recent (${locationAge / 1000}s old)")
                            continuation.resume(LocationData(it.latitude, it.longitude))
                        } else {
                            Log.d(TAG, "⏰ Last known location is too old (${locationAge / 1000}s), will get fresh location")
                            continuation.resume(null)
                        }
                    } ?: run {
                        Log.d(TAG, "📭 No last known location available")
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "❌ Failed to get last known location: ${exception.message}")
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception getting last known location: ${e.message}")
            continuation.resume(null)
        }
    }

    /**
     * Get current location from GPS with explicit permission check
     */
    private suspend fun getCurrentLocationFromGPSSafe(): LocationData? =
        withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                try {
                    // Explicit permission check before calling GPS
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED) {

                        Log.w(TAG, "❌ No location permissions for GPS fetch")
                        continuation.resume(null)
                        return@suspendCancellableCoroutine
                    }

                    val cancellationTokenSource = CancellationTokenSource()
                    Log.d(TAG, "🛰️ Requesting fresh GPS location with high accuracy...")

                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).addOnCompleteListener { task ->
                        try {
                            if (task.isSuccessful) {
                                val location = task.result
                                if (location != null) {
                                    Log.i(TAG, "🎯 Fresh GPS location obtained!")
                                    Log.i(TAG, "   ➤ Latitude: ${location.latitude}")
                                    Log.i(TAG, "   ➤ Longitude: ${location.longitude}")
                                    Log.i(TAG, "   ➤ Accuracy: ${location.accuracy}m")
                                    Log.i(TAG, "   ➤ Provider: ${location.provider}")
                                    Log.i(TAG, "   ➤ Time: ${location.time}")
                                    continuation.resume(LocationData(location.latitude, location.longitude))
                                } else {
                                    Log.w(TAG, "🚫 GPS returned null location")
                                    continuation.resume(null)
                                }
                            } else {
                                Log.e(TAG, "❌ GPS task failed: ${task.exception?.message}")
                                continuation.resume(null)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "💥 Exception in GPS callback: ${e.message}")
                            continuation.resume(null)
                        }
                    }

                    continuation.invokeOnCancellation {
                        Log.d(TAG, "🚫 GPS location request cancelled")
                        cancellationTokenSource.cancel()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "💥 Exception in getCurrentLocationFromGPSSafe: ${e.message}")
                    continuation.resume(null)
                }
            }
        }

    /**
     * Alternative method using LocationCallback with explicit permission check
     */
    suspend fun getCurrentLocationWithCallbackSafe(): LocationData = suspendCancellableCoroutine { continuation ->
        // Explicit permission check before using callback
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

            Log.w(TAG, "❌ No permissions for callback location")
            continuation.resume(getDefaultLocation())
            return@suspendCancellableCoroutine
        }

        if (!isLocationEnabled()) {
            Log.w(TAG, "❌ Location services disabled for callback")
            continuation.resume(getDefaultLocation())
            return@suspendCancellableCoroutine
        }

        var resumed = false
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (!resumed) {
                    resumed = true
                    locationResult.lastLocation?.let { location ->
                        Log.i(TAG, "🎯 Callback location: ${location.latitude}, ${location.longitude}")
                        fusedLocationClient.removeLocationUpdates(this)
                        continuation.resume(LocationData(location.latitude, location.longitude))
                    } ?: run {
                        Log.w(TAG, "🚫 Callback returned null location")
                        fusedLocationClient.removeLocationUpdates(this)
                        continuation.resume(getDefaultLocation())
                    }
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable && !resumed) {
                    Log.w(TAG, "📍 Location not available via callback")
                    resumed = true
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(getDefaultLocation())
                }
            }
        }

        try {
            Log.d(TAG, "🔄 Starting location updates with callback...")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            // Set timeout
            continuation.invokeOnCancellation {
                Log.d(TAG, "🚫 Location callback cancelled")
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

            // Manual timeout handling
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (!resumed) {
                    Log.w(TAG, "⏰ Location callback timed out")
                    resumed = true
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    continuation.resume(getDefaultLocation())
                }
            }, LOCATION_TIMEOUT_MS)

        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception starting location updates: ${e.message}")
            if (!resumed) {
                continuation.resume(getDefaultLocation())
            }
        }
    }

    /**
     * Check if location permission is granted with explicit permission check
     */
    private fun hasLocationPermission(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Check if location services are enabled
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as SystemLocationManager
        val gpsEnabled = try {
            locationManager.isProviderEnabled(SystemLocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Log.w(TAG, "Error checking GPS provider: ${e.message}")
            false
        }

        val networkEnabled = try {
            locationManager.isProviderEnabled(SystemLocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Log.w(TAG, "Error checking Network provider: ${e.message}")
            false
        }

        Log.d(TAG, "🛰️ GPS Provider: ${if (gpsEnabled) "✅ ENABLED" else "❌ DISABLED"}")
        Log.d(TAG, "🌐 Network Provider: ${if (networkEnabled) "✅ ENABLED" else "❌ DISABLED"}")

        return gpsEnabled || networkEnabled
    }

    /**
     * Get default location (New Delhi coordinates)
     */
    private fun getDefaultLocation(): LocationData {
        Log.d(TAG, "🏠 Using default location: New Delhi ($DEFAULT_LATITUDE, $DEFAULT_LONGITUDE)")
        return LocationData(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }

    /**
     * Public method to check if location permissions are available
     */
    fun hasPermissions(): Boolean = hasLocationPermission()

    /**
     * Get detailed permission information for debugging
     */
    private fun logPermissionDetails() {
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "📋 PERMISSION DETAILS:")
        Log.d(TAG, "   ➤ ACCESS_FINE_LOCATION: ${if (fineLocation) "✅ GRANTED" else "❌ DENIED"}")
        Log.d(TAG, "   ➤ ACCESS_COARSE_LOCATION: ${if (coarseLocation) "✅ GRANTED" else "❌ DENIED"}")
        Log.d(TAG, "   ➤ Overall Permission: ${if (hasLocationPermission()) "✅ GRANTED" else "❌ DENIED"}")
    }

    /**
     * Enhanced method that tries multiple strategies
     */
    suspend fun getBestAvailableLocation(): LocationData {
        Log.d(TAG, "🎯 === STARTING COMPREHENSIVE LOCATION FETCH ===")

        logPermissionDetails()

        // Early return if no permissions
        if (!hasLocationPermission()) {
            Log.w(TAG, "❌ No location permissions - returning default location immediately")
            return getDefaultLocation()
        }

        if (!isLocationEnabled()) {
            Log.w(TAG, "❌ Location services disabled - returning default location")
            return getDefaultLocation()
        }

        // Strategy 1: Try last known location first
        Log.d(TAG, "📡 Strategy 1: Checking last known location...")
        val lastKnown = getLastKnownLocationSafe()
        if (lastKnown != null) {
            Log.i(TAG, "⚡ Success with last known location: ${lastKnown.latitude}, ${lastKnown.longitude}")
            return lastKnown
        }

        // Strategy 2: Try fresh GPS with timeout
        Log.d(TAG, "🛰️ Strategy 2: Fresh GPS location...")
        val freshLocation = getCurrentLocationFromGPSSafe()
        if (freshLocation != null) {
            Log.i(TAG, "🎉 Success with fresh GPS: ${freshLocation.latitude}, ${freshLocation.longitude}")
            return freshLocation
        }

        // Strategy 3: Try callback method as last resort
        Log.d(TAG, "🔄 Strategy 3: Callback-based location...")
        return getCurrentLocationWithCallbackSafe()
    }

    /**
     * Get required permissions array for requesting permissions
     */
    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Check location settings and provide detailed status
     */
    fun getLocationStatus(): LocationStatus {
        val hasPermission = hasLocationPermission()
        val isEnabled = isLocationEnabled()

        // Safe check for last known location
        val hasLastKnown = if (hasPermission) {
            try {
                var hasLast = false
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) {

                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        hasLast = location != null
                    }
                }
                hasLast
            } catch (e: Exception) {
                Log.w(TAG, "Error checking last known location: ${e.message}")
                false
            }
        } else {
            false
        }

        return LocationStatus(
            hasPermission = hasPermission,
            isLocationEnabled = isEnabled,
            hasLastKnownLocation = hasLastKnown,
            canGetCurrentLocation = hasPermission && isEnabled
        )
    }

    /**
     * Simple method to test if we can get location without errors
     */
    suspend fun canGetLocation(): Boolean {
        return try {
            hasLocationPermission() && isLocationEnabled()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if can get location: ${e.message}")
            false
        }
    }
}

/**
 * Data class to represent location status
 */
data class LocationStatus(
    val hasPermission: Boolean,
    val isLocationEnabled: Boolean,
    val hasLastKnownLocation: Boolean,
    val canGetCurrentLocation: Boolean
) {
    fun getStatusMessage(): String = when {
        !hasPermission -> "Location permission required"
        !isLocationEnabled -> "Please enable location services"
        !canGetCurrentLocation -> "Location services unavailable"
        else -> "Location services ready"
    }

    fun isFullyReady(): Boolean = hasPermission && isLocationEnabled && canGetCurrentLocation
}