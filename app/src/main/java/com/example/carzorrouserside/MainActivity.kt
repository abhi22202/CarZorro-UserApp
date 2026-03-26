package com.example.carzorrouserside

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.CarZorroTheme
import com.example.carzorrouserside.ui.theme.navigation.Navigation
import com.example.carzorrouserside.util.LocationManager
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import androidx.activity.viewModels
import androidx.collection.isNotEmpty
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.HomepageServiceViewModel


@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val PAYMENT_TIMEOUT_MS = 300000L
    }

    @Inject
    lateinit var locationManager: LocationManager
    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var navController: NavHostController

    // ... (rest of your variables and launchers are fine)
    private var onPaymentSuccess: ((String?) -> Unit)? = null
    private var onPaymentError: ((Int, String?) -> Unit)? = null
    private var checkoutInstance: Checkout? = null
    private var isPaymentInProgress = false
    private var paymentTimeoutJob: Job? = null
    private val homepageServiceViewModel: HomepageServiceViewModel by viewModels()
    private var hasHandledPendingBooking = false



    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handleLocationPermissionResult(permissions)
    }

    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "User returned from location settings")
        checkLocationStatusAfterSettings()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate started")

        handleIntentExtras(intent)

        enableEdgeToEdge()

        initializeRazorpay()

        setContent {
            CarZorroTheme {
                // **FIXED: Added Surface container**
                // This uses the 'background' color from the theme and fixes dark mode.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navController = rememberNavController()
                    // RED: Pass the navController to your Navigation composable
                    // RED: Ensure your Navigation composable uses this navController for its NavHost
                    Navigation(
                        userPreferencesManager = userPreferencesManager,
                        navController = navController
                    )

                }
            }
        }

        setupLocationPermissions()
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent called")
        if (intent != null) {
            handleIntentExtras(intent) // Re-check extras on new intent
        }
        // It's important to update the activity's intent to the new one
        setIntent(intent)
    }

    // RED: Add handleIntentExtras function to process notification data
    private fun handleIntentExtras(intent: Intent) {
        // Check if the intent has the "bookingId" extra we added in the service
        if (intent.hasExtra("bookingId")) {
            val bookingId = intent.getStringExtra("bookingId")
            Log.d(TAG, "Intent contains bookingId: $bookingId. Attempting navigation.")

            // Ensure NavController is initialized before navigating
            // (It should be initialized quickly by setContent, but check is safe)
            if (::navController.isInitialized && bookingId != null) {
                // Convert bookingId string to Int and navigate to order detail screen
                try {
                    val orderId = bookingId.toIntOrNull()
                    if (orderId != null && orderId > 0) {
                        // Use the correct route from Routes
                        val route = com.example.carzorrouserside.ui.theme.navigation.Routes.orderDetailScreen(orderId)
                        navController.navigate(route) {
                            launchSingleTop = true // Avoids stacking identical screens if already open
                            // Keep back stack so user can go back to previous screen
                        }
                        Log.d(TAG, "✅ Navigation triggered to order detail screen for booking: $orderId")
                    } else {
                        Log.e(TAG, "❌ Invalid bookingId format: '$bookingId'. Expected numeric ID.")
                    }
                } catch (e: IllegalArgumentException) {
                    // This can happen if the route string is wrong or arguments don't match
                    Log.e(TAG, "❌ Navigation failed: Invalid route or arguments", e)
                } catch (e: IllegalStateException) {
                    // This can happen if the NavController isn't fully attached to a NavHost graph yet
                    Log.e(TAG, "❌ Navigation failed: NavController not attached yet", e)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Navigation failed: Unexpected error", e)
                }

                // Clear the extra after handling to prevent re-navigation on config change/recreation
                intent.removeExtra("bookingId")

            } else {
                Log.w(TAG, "NavController not initialized or bookingId is null when trying to navigate from intent.")
                // If this happens often, consider storing bookingId temporarily and trying navigation later
            }
        } else {
            Log.d(TAG, "Intent does not contain 'bookingId' extra.")
        }
    }

    // ... (The rest of your MainActivity.kt file is correct and can remain as is)
    private fun initializeRazorpay() {
        try {
            Checkout.preload(applicationContext)
            Log.d(TAG, "Razorpay SDK preloaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preload Razorpay SDK", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Log.i(TAG, "Payment Success!")
        Log.d(TAG, "Razorpay Payment ID: $razorpayPaymentId")
        paymentTimeoutJob?.cancel()
        paymentTimeoutJob = null
        isPaymentInProgress = false
        try {
            onPaymentSuccess?.invoke(razorpayPaymentId)
        } catch (e: Exception) {
            Log.e(TAG, "Error in success callback", e)
        } finally {
            lifecycleScope.launch {
                delay(1000)
                cleanupCheckoutInstance()
            }
        }
    }

    override fun onPaymentError(code: Int, response: String?) {
        Log.e(TAG, "Payment Error!")
        Log.e(TAG, "Error Details:")
        Log.e(TAG, "  Code: $code")
        Log.e(TAG, "  Response: $response")
        when (code) {
            0 -> Log.e(TAG, "Payment cancelled by user")
            1 -> Log.e(TAG, "Payment failed")
            2 -> Log.e(TAG, "Network error")
            3 -> Log.e(TAG, "Invalid payment data")
            4 -> Log.e(TAG, "Payment timeout")
            else -> Log.e(TAG, "Unknown error code: $code")
        }
        paymentTimeoutJob?.cancel()
        paymentTimeoutJob = null
        isPaymentInProgress = false
        try {
            onPaymentError?.invoke(code, response)
        } catch (e: Exception) {
            Log.e(TAG, "Error in error callback", e)
        } finally {
            lifecycleScope.launch {
                delay(1000)
                cleanupCheckoutInstance()
            }
        }
    }

    fun setPaymentCallbacks(
        onSuccess: (String?) -> Unit,
        onError: (Int, String?) -> Unit
    ) {
        Log.d(TAG, "Setting payment callbacks")
        onPaymentSuccess = onSuccess
        onPaymentError = onError
    }

    fun startRazorpayPayment(
        orderId: String,
        razorpayKey: String,
        amount: Double
    ) {
        Log.d(TAG, "Starting Razorpay payment")
        Log.d(TAG, "Payment Details:")
        Log.d(TAG, "  Order ID: $orderId")
        Log.d(TAG, "  Amount: ₹$amount")
        Log.d(TAG, "  Key: ${razorpayKey.take(15)}...")
        if (isPaymentInProgress) {
            Log.w(TAG, "Payment already in progress")
            onPaymentError?.invoke(-1, "Payment already in progress")
            return
        }
        if (isDestroyed || isFinishing) {
            Log.w(TAG, "Activity is destroyed/finishing")
            onPaymentError?.invoke(-1, "Activity not available")
            return
        }
        startPayment(orderId, razorpayKey, amount)
    }

    private fun startPayment(
        orderId: String,
        razorpayKey: String,
        amount: Double
    ) {
        try {
            Log.d(TAG, "Initializing Razorpay payment...")
            isPaymentInProgress = true
            cleanupCheckoutInstance()
            checkoutInstance = Checkout().apply {
                setKeyID(razorpayKey)
            }
            // Convert amount to paise (smallest currency unit for INR)
            val amountInPaise = (amount * 100).toInt()
            
            val options = JSONObject().apply {
                put("name", "Carzorro")
                put("description", "Product Purchase")
                put("order_id", orderId)
                put("amount", amountInPaise) // Amount in paise
                put("currency", "INR")
                put("theme", JSONObject().apply {
                    put("color", "#9D8CFF") // Your app color
                })
                put("modal", JSONObject().apply {
                    put("escape", true)
                    put("backdropclose", false)
                })
            }
            Log.d(TAG, "Payment options:")
            Log.d(TAG, options.toString(2))
            setupPaymentTimeout()
            Log.d(TAG, "Launching Razorpay checkout...")
            checkoutInstance?.open(this, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting payment", e)
            isPaymentInProgress = false
            paymentTimeoutJob?.cancel()
            val errorMessage = when (e) {
                is JSONException -> {
                    Log.e(TAG, "JSON configuration error")
                    "Invalid payment configuration"
                }
                is IllegalStateException -> {
                    Log.e(TAG, "Razorpay not initialized properly")
                    "Razorpay initialization failed"
                }
                is IllegalArgumentException -> {
                    Log.e(TAG, "Invalid payment parameters")
                    "Invalid payment parameters"
                }
                else -> {
                    Log.e(TAG, "Unknown error: ${e.message}")
                    "Failed to start payment: ${e.message}"
                }
            }
            onPaymentError?.invoke(-1, errorMessage)
            cleanupCheckoutInstance()
        }
    }

    private fun setupPaymentTimeout() {
        paymentTimeoutJob?.cancel()
        paymentTimeoutJob = lifecycleScope.launch {
            delay(PAYMENT_TIMEOUT_MS)
            if (isPaymentInProgress) {
                Log.w(TAG, "Payment timeout reached")
                isPaymentInProgress = false
                onPaymentError?.invoke(-3, "Payment timeout - Please try again")
                cleanupCheckoutInstance()
            }
        }
    }

    private fun cleanupCheckoutInstance() {
        try {
            checkoutInstance = null
            Log.d(TAG, "Checkout instance cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up checkout", e)
        }
    }

    fun clearPaymentCallbacks() {
        Log.d(TAG, "Clearing payment callbacks")
        paymentTimeoutJob?.cancel()
        paymentTimeoutJob = null
        isPaymentInProgress = false
        onPaymentSuccess = null
        onPaymentError = null
        cleanupCheckoutInstance()
        Log.d(TAG, "Payment callbacks cleared")
    }

    fun isPaymentInProgress(): Boolean = isPaymentInProgress

    fun forceStopPayment() {
        Log.w(TAG, "Force stopping payment")
        paymentTimeoutJob?.cancel()
        paymentTimeoutJob = null
        isPaymentInProgress = false
        cleanupCheckoutInstance()
        onPaymentError?.invoke(-2, "Payment forcefully stopped")
    }

    private fun setupLocationPermissions() {
        Log.d(TAG, "Setting up location permissions...")
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1000)
            requestLocationPermissionsIfNeeded()
        }
    }

    private fun requestLocationPermissionsIfNeeded() {
        val locationStatus = locationManager.getLocationStatus()
        Log.i(TAG, "=== LOCATION SETUP CHECK ===")
        Log.i(TAG, "   Has Permission: ${locationStatus.hasPermission}")
        Log.i(TAG, "   Location Enabled: ${locationStatus.isLocationEnabled}")
        Log.i(TAG, "   Can Get Location: ${locationStatus.canGetCurrentLocation}")
        Log.i(TAG, "   Status: ${locationStatus.getStatusMessage()}")
        Log.i(TAG, "   Fully Ready: ${locationStatus.isFullyReady()}")
        when {
            !locationStatus.hasPermission -> {
                Log.i(TAG, "Need to request location permissions")
                requestLocationPermissions()
            }
            !locationStatus.isLocationEnabled -> {
                Log.w(TAG, "Location services disabled - will show dialog")
                showLocationServicesDialog()
            }
            locationStatus.isFullyReady() -> {
                Log.i(TAG, "Location is fully ready!")
                performInitialLocationTest()
            }
            else -> {
                Log.w(TAG, "Location setup incomplete")
            }
        }
    }

    private fun requestLocationPermissions() {
        when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                Log.d(TAG, "Showing permission rationale")
                showPermissionRationaleDialog()
            }
            else -> {
                Log.d(TAG, "Directly requesting permissions")
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun handleLocationPermissionResult(permissions: Map<String, Boolean>) {
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        Log.i(TAG, "=== PERMISSION RESULT ===")
        Log.i(TAG, "   Fine Location: ${if (fineLocationGranted) "GRANTED" else "DENIED"}")
        Log.i(TAG, "   Coarse Location: ${if (coarseLocationGranted) "GRANTED" else "DENIED"}")
        when {
            fineLocationGranted || coarseLocationGranted -> {
                Log.i(TAG, "Location permission granted!")
                checkLocationServicesAfterPermission()
            }
            else -> {
                Log.w(TAG, "All location permissions denied")
                showPermissionDeniedDialog()
            }
        }
    }

    private fun checkLocationServicesAfterPermission() {
        val locationStatus = locationManager.getLocationStatus()
        if (!locationStatus.isLocationEnabled) {
            Log.w(TAG, "Permissions granted but location services disabled")
            showLocationServicesDialog()
        } else {
            Log.i(TAG, "All location prerequisites met!")
            performInitialLocationTest()
        }
    }

    private fun performInitialLocationTest() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "=== INITIAL LOCATION TEST ===")
                val location = locationManager.getBestAvailableLocation()
                Log.i(TAG, "Initial Location Test Results:")
                Log.i(TAG, "   Latitude: ${location.latitude}")
                Log.i(TAG, "   Longitude: ${location.longitude}")
                if (location.latitude == 28.6139 && location.longitude == 77.209) {
                    Log.w(TAG, "Still using default Delhi location")
                    Log.w(TAG, "Tips: Make sure GPS is on and try moving outside if indoors")
                } else {
                    Log.i(TAG, "SUCCESS! Got real user location!")
                    Log.i(TAG, "Location services are working properly")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Initial location test failed: ${e.message}")
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        android.widget.Toast.makeText(
            this,
            "Location permission needed for better app experience. Granting permission...",
            android.widget.Toast.LENGTH_LONG
        ).show()
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showPermissionDeniedDialog() {
        android.widget.Toast.makeText(
            this,
            "Location permissions denied. You can enable them in Settings for better accuracy.",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    private fun showLocationServicesDialog() {
        android.widget.Toast.makeText(
            this,
            "Please enable location services in Settings for accurate location features.",
            android.widget.Toast.LENGTH_LONG
        ).show()
        openLocationSettings()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app settings: ${e.message}")
        }
    }

    private fun openLocationSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            locationSettingsLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening location settings: ${e.message}")
        }
    }

    private fun checkLocationStatusAfterSettings() {
        val locationStatus = locationManager.getLocationStatus()
        Log.d(TAG, "Checking status after settings return:")
        Log.d(TAG, "   Location Enabled: ${locationStatus.isLocationEnabled}")
        if (locationStatus.isLocationEnabled) {
            Log.i(TAG, "Location services now enabled!")
            performInitialLocationTest()
        } else {
            Log.w(TAG, "Location services still disabled after settings")
        }
    }
    override fun onStart() {
        super.onStart()
        // Reset the flag every time the activity comes to foreground
        hasHandledPendingBooking = false
        Log.d(TAG, "🔄 onStart → Reset hasHandledPendingBooking = false")
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity resumed")

        val postBookingViewModel: com.example.carzorrouserside.ui.theme.viewmodel.booking.PostBookingViewModel by viewModels()

        lifecycleScope.launch {
            val pending = userPreferencesManager.getPendingBooking()
            if (pending != null && !hasHandledPendingBooking) {
                Log.d(TAG, "🕐 Pending booking found: $pending")

                val isSending = pending.argsJson?.contains("\"isRequestSending\":true") == true

                // ✅ Wait longer to ensure NavHost is attached
                delay(800)

                if (::navController.isInitialized) {
                    // ✅ Only access graph if it’s actually been set
                    val hasGraph = try {
                        navController.graph.startDestinationRoute != null
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "⚠️ NavController graph not yet set — skipping navigation")
                        false
                    }

                    if (hasGraph && isSending) {
                        val currentRoute = navController.currentDestination?.route
                        if (currentRoute != Routes.BOOKING_SCREEN) {
                            val route = buildString {
                                append("${Routes.BOOKING_SCREEN}?sheet=${pending.sheetType}")
                                if (isSending) append("&isSending=true")
                            }

                            Log.d(TAG, "🚀 Navigating safely to Booking Screen: $route")

                            try {
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME_SCREEN) { inclusive = false }
                                }
                                hasHandledPendingBooking = true
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ Navigation failed: ${e.message}", e)
                            }
                        }
                    } else {
                        Log.w(TAG, "⚠️ NavGraph not set or booking not in sending state — skipping navigation")
                    }
                } else {
                    Log.w(TAG, "⚠️ NavController not initialized yet")
                }
            } else {
                Log.d(TAG, "✅ No pending booking found or already handled.")
            }
        }

        lifecycleScope.launch {
            try {
                val userId = userPreferencesManager.getUserId()
                val token = userPreferencesManager.getJwtToken()
                if (userId != null && token != null) {
                    homepageServiceViewModel.checkActiveBooking(userId, token)
                } else {
                    Log.w(TAG, "⚠️ Cannot call activeBooking — userId or token is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to call activeBooking on resume: ${e.message}")
            }
        }

        if (isPaymentInProgress) {
            Log.d(TAG, "💰 Payment in progress when app resumed")
        }
    }



//    override fun onResume() {
//
//        super.onResume()
//        Log.d(TAG, "MainActivity resumed")
//
//        // Call ActiveBooking API whenever app comes to foreground
//        lifecycleScope.launch {
//            try {
//                val userId = userPreferencesManager.getUserId()
//                val token = userPreferencesManager.getJwtToken()
//                Log.d(TAG, "🔄 Checking active booking as app resumed...")
//                if (userId != null && token != null) {
//                    Log.d(TAG, "🔄 Checking active booking as app resumed...")
//                    homepageServiceViewModel.checkActiveBooking(userId, token)
//                } else {
//                    Log.w(TAG, "⚠️ Cannot call activeBooking — userId or token is null")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "❌ Failed to call activeBooking on resume: ${e.message}")
//            }
//        }
//
//        if (isPaymentInProgress) {
//            Log.d(TAG, "Payment in progress when app resumed")
//        }
//    }



    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity paused")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity stopped")
        if (isPaymentInProgress) {
            Log.d(TAG, "App stopped during payment - user might be completing payment")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "MainActivity destroying")
        paymentTimeoutJob?.cancel()
        paymentTimeoutJob = null
        clearPaymentCallbacks()
        try {
            checkoutInstance = null
            System.gc()
        } catch (e: Exception) {
            Log.e(TAG, "Error during Razorpay cleanup", e)
        }
        super.onDestroy()
    }

    fun testLocationManually() {
        lifecycleScope.launch {
            Log.d(TAG, "=== MANUAL LOCATION TEST TRIGGERED ===")
            val canGetLocation = locationManager.canGetLocation()
            Log.d(TAG, "Can get location: $canGetLocation")
            if (canGetLocation) {
                val location = locationManager.getBestAvailableLocation()
                Log.i(TAG, "Manual test result: ${location.latitude}, ${location.longitude}")
                if (location.latitude != 28.6139) {
                    Log.i(TAG, "Real GPS coordinates obtained!")
                } else {
                    Log.w(TAG, "Still getting default coordinates")
                }
            } else {
                Log.w(TAG, "Cannot get location - check permissions and GPS")
            }
        }
    }
    private fun NavController.safeNavigate(route: String) {
        try {
            // ✅ Only navigate if graph is actually ready
            if (this.graph.nodes.isNotEmpty()) {
                this.navigate(route)
            } else {
                Log.w("NavController", "⚠️ NavGraph not ready yet. Skipping navigation to $route")
            }
        } catch (e: Exception) {
            Log.e("NavController", "❌ Navigation failed: ${e.message}")
        }
    }

}

