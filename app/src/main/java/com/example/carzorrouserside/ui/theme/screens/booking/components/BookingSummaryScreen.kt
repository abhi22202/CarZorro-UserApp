package com.example.carzorrouserside.ui.theme.screens.booking.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalContext as LocalContextAlias
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.BookingData
import android.util.Log
import android.widget.Toast
import com.example.carzorrouserside.ui.theme.appPrimary

import com.example.carzorrouserside.ui.theme.viewmodel.session.BookingSummaryUiState
import com.example.carzorrouserside.ui.theme.viewmodel.session.BookingSummaryViewModel
import com.example.carzorrouserside.ui.theme.viewmodel.session.PaymentAction
import com.example.carzorrouserside.MainActivity
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.HomepageServiceViewModel
import com.example.carzorrouserside.data.token.UserPreferencesManager
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryScreen(
    vendorId: Int = 0,
    bookingId: Int? = null,
    onBackPressed: () -> Unit,
    onProceedToPay: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onCancelSuccess: () -> Unit = onBackPressed,
    viewModel: BookingSummaryViewModel = hiltViewModel(),
    homepageServiceViewModel: HomepageServiceViewModel = hiltViewModel(),
    userPreferencesManager: UserPreferencesManager? = null,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val coroutineScope = rememberCoroutineScope()
    
    // Get UserPreferencesManager from EntryPoint if not provided
    val preferencesManager = userPreferencesManager ?: EntryPointAccessors.fromApplication(
        context.applicationContext,
        UserPreferencesManagerEntryPoint::class.java
    ).userPreferencesManager()
    val scrollState = rememberScrollState()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedCouponId by remember { mutableStateOf<Int?>(null) }
    var appliedCouponDiscount by remember { mutableStateOf<Double?>(null) }
    var appliedCouponCode by remember { mutableStateOf<String?>(null) }
    var showCouponDialog by remember { mutableStateOf(false) }
    
    // Error message state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bookingData by viewModel.bookingData.collectAsStateWithLifecycle()
    val bookingDetails by viewModel.bookingDetails.collectAsStateWithLifecycle()
    val userDetails by viewModel.userDetails.collectAsStateWithLifecycle()
    
    // Super coins state
    var useSuperCoins by remember { mutableStateOf(false) }

    // Razorpay payment flow state
    data class BookingPaymentFlowState(
        val orderId: String,
        val amount: Double,
        val razorpayKey: String,
        val bookingId: Int?
    )
    var paymentFlowState by remember { mutableStateOf<BookingPaymentFlowState?>(null) }

    // Initialize booking data - prefer bookingId over vendorId
    LaunchedEffect(bookingId, vendorId) {
        when {
            bookingId != null && bookingId > 0 -> {
                viewModel.initializeBooking(bookingId)
            }
            vendorId > 0 -> {
                // Legacy support: use vendor ID
                viewModel.initializeBookingWithVendorId(vendorId)
            }
        }
    }

    // Handle authentication success (when user returns from login)
    LaunchedEffect(uiState.isUserAuthenticated) {
        if (uiState.isUserAuthenticated && uiState.userId != null) {
            viewModel.onAuthenticationSuccess()
        }
    }

    // Handle Razorpay payment flow - use orderId as key to prevent multiple triggers
    LaunchedEffect(paymentFlowState?.orderId) {
        val flowState = paymentFlowState
        if (flowState == null) {
            Log.d("BookingSummary", "Payment flow state is null, waiting...")
            return@LaunchedEffect
        }
        
        Log.d("BookingSummary", "🔄 LaunchedEffect triggered - Payment flow state: orderId=${flowState.orderId}, amount=${flowState.amount}")
            
            // Validate payment flow state before proceeding
            if (flowState.orderId.isBlank()) {
                Log.e("BookingSummary", "❌ Invalid payment flow state: orderId is blank")
                paymentFlowState = null
                errorMessage = "Invalid order ID. Please try again."
                Toast.makeText(context, "Invalid order ID. Please try again.", Toast.LENGTH_LONG).show()
                return@LaunchedEffect
            }
            
            if (flowState.amount <= 0) {
                Log.e("BookingSummary", "❌ Invalid payment flow state: amount is ${flowState.amount}")
                paymentFlowState = null
                errorMessage = "Invalid payment amount. Please try again."
                Toast.makeText(context, "Invalid payment amount. Please try again.", Toast.LENGTH_LONG).show()
                return@LaunchedEffect
            }
            
            activity?.let { mainActivity ->
                Log.d("BookingSummary", "✅ Valid payment flow state - Starting Razorpay payment")
                Log.d("BookingSummary", "Order ID: ${flowState.orderId}")
                Log.d("BookingSummary", "Amount: ${flowState.amount}")
                Log.d("BookingSummary", "Booking ID: ${flowState.bookingId}")

                // Set payment callbacks first (like product purchase)
                mainActivity.setPaymentCallbacks(
                    onSuccess = { payId ->
                        Log.d("BookingSummary", "Razorpay payment success: payId=$payId")
                        payId?.let {
                            viewModel.confirmBookingPayment(
                                bookingId = flowState.bookingId,
                                orderId = flowState.orderId,
                                paymentId = it,
                                onSuccess = { confirmedAmount ->
                                    Log.d("BookingSummary", "Payment confirmed: amount=$confirmedAmount")
                                    // Clear payment flow state
                                    paymentFlowState = null
                                    
                                    // Refresh active booking status after payment success
                                    val userId = preferencesManager.getUserId()
                                    val token = preferencesManager.getJwtToken()
                                    if (userId != null && token != null) {
                                        Log.d("BookingSummary", "Refreshing active booking status after payment")
                                        homepageServiceViewModel.checkActiveBooking(userId, token)
                                    }
                                    
                                    // Navigate to confirmation screen
                                    navController?.navigate(
                                        Routes.bookingConfirmationScreen(confirmedAmount, "online")
                                    ) {
                                        popUpTo(0) { inclusive = true }
                                    } ?: run {
                                        Log.w("BookingSummary", "NavController is null after payment confirmation")
                                        onProceedToPay()
                                    }
                                },
                                onError = { error ->
                                    Log.e("BookingSummary", "Payment confirmation error: $error")
                                    // Check if error message indicates success despite error status
                                    // Some APIs return error status with success message
                                    if (error.contains("successfully", ignoreCase = true) || 
                                        error.contains("succesfully", ignoreCase = true) ||
                                        error.contains("payment made", ignoreCase = true) ||
                                        error.contains("payment has been", ignoreCase = true)) {
                                        Log.d("BookingSummary", "Error message indicates success, treating as success")
                                        paymentFlowState = null
                                        errorMessage = null
                                        
                                        // Refresh active booking status after payment success
                                        val userId = preferencesManager.getUserId()
                                        val token = preferencesManager.getJwtToken()
                                        if (userId != null && token != null) {
                                            Log.d("BookingSummary", "Refreshing active booking status after payment")
                                            homepageServiceViewModel.checkActiveBooking(userId, token)
                                        }
                                        
                                        // Navigate to confirmation screen
                                        val amount = bookingData?.totalAmount ?: 0.0
                                        navController?.navigate(
                                            Routes.bookingConfirmationScreen(amount, "online")
                                        ) {
                                            popUpTo(0) { inclusive = true }
                                        } ?: run {
                                            Log.w("BookingSummary", "NavController is null after payment confirmation")
                                            onProceedToPay()
                                        }
                                    } else {
                                        paymentFlowState = null
                                        errorMessage = error
                                        Toast.makeText(context, "Payment confirmation failed: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        } ?: run {
                            Log.e("BookingSummary", "Payment ID is null")
                            paymentFlowState = null
                            errorMessage = "Payment ID not received"
                            Toast.makeText(context, "Payment ID not received", Toast.LENGTH_LONG).show()
                        }
                    },
                    onError = { code, response ->
                        Log.e("BookingSummary", "Razorpay payment failed: code=$code, response=$response")
                        // Only show error if it's not a user cancellation (code 0)
                        if (code != 0) {
                            paymentFlowState = null
                            errorMessage = "Payment failed: $response"
                            Toast.makeText(context, "Payment failed: $response", Toast.LENGTH_LONG).show()
                        } else {
                            Log.d("BookingSummary", "Payment cancelled by user")
                            paymentFlowState = null
                            // Don't show error for user cancellation
                        }
                    }
                )

                // Start Razorpay payment (simplified - no extra delays like product purchase)
                mainActivity.startRazorpayPayment(
                    orderId = flowState.orderId,
                    razorpayKey = flowState.razorpayKey,
                    amount = flowState.amount
                )
            } ?: run {
                Log.e("BookingSummary", "MainActivity is null")
                paymentFlowState = null
                errorMessage = "Unable to start payment. Please try again."
                Toast.makeText(context, "Unable to start payment. Please try again.", Toast.LENGTH_LONG).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Booking Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showCancelDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel Booking",
                        tint = Color.Red
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Cancel Booking Dialog
        if (showCancelDialog) {
            com.example.carzorrouserside.ui.theme.screens.booking.CancelBookingDialog(
                onDismissRequest = { showCancelDialog = false },
                onSubmit = { selectedReasonId, customReason ->
                    viewModel.cancelBooking(
                        bookingId = bookingId,
                        reasonId = selectedReasonId,
                        reasonText = customReason?.takeIf { it.isNotBlank() },
                        onSuccess = {
                            showCancelDialog = false
                            onCancelSuccess() // Navigate to homepage after cancellation
                        },
                        onError = { error ->
                            // Error handling - could show a snackbar
                            showCancelDialog = false
                        }
                    )
                },
                viewModel = viewModel
            )
        }

        // Coupon Dialog - handled in BookingSummaryContent

        // Payment Method Dialog
        if (showPaymentDialog && bookingData != null) {
            PaymentMethodDialog(
                onDismissRequest = { showPaymentDialog = false },
                walletBalance = userDetails?.walletBalance ?: 0.0,
                onNextClick = { selectedMethod ->
                    showPaymentDialog = false
                    
                    // Log payment method selection
                    Log.d("BookingSummary", "Payment method selected: $selectedMethod, bookingId parameter: $bookingId")
                    
                    when (selectedMethod) {
                        "Pay on service" -> {
                            Log.d("BookingSummary", "Processing Pay on Service payment")
                            viewModel.makeBookingPayment(
                                bookingId = bookingId,
                                paymentOption = "pay_after_service",
                                couponId = selectedCouponId,
                                onSuccess = { paymentData ->
                                    Log.d("BookingSummary", "✅ Pay on Service success: amount=${paymentData.amount}")
                                    // Clear any error messages
                                    errorMessage = null
                                    
                                    // Get the amount - use paymentData amount or fallback to booking total
                                    val amount = if (paymentData.amount > 0) {
                                        paymentData.amount
                                    } else {
                                        bookingData?.totalAmount ?: 0.0
                                    }
                                    
                                    Log.d("BookingSummary", "Navigating to confirmation screen with amount: $amount")
                                    
                                    // Refresh active booking status after payment success
                                    val userId = preferencesManager.getUserId()
                                    val token = preferencesManager.getJwtToken()
                                    if (userId != null && token != null) {
                                        Log.d("BookingSummary", "Refreshing active booking status after payment")
                                        homepageServiceViewModel.checkActiveBooking(userId, token)
                                    }
                                    
                                    // Navigate to confirmation screen - use coroutine to ensure navigation happens
                                    coroutineScope.launch {
                                        delay(100) // Small delay to ensure state is updated
                                        try {
                                            navController?.navigate(
                                                Routes.bookingConfirmationScreen(amount, "pay_after_service")
                                            ) {
                                                popUpTo(0) { inclusive = true }
                                            } ?: run {
                                                Log.w("BookingSummary", "NavController is null, calling onProceedToPay")
                                                onProceedToPay()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("BookingSummary", "Navigation error: ${e.message}", e)
                                            // Fallback navigation
                                            onProceedToPay()
                                        }
                                    }
                                },
                                onError = { error ->
                                    Log.e("BookingSummary", "Pay on Service error: $error")
                                    // Check if error message indicates success despite error status
                                    // Some APIs return error status with success message
                                    if (error.contains("successfully", ignoreCase = true) || 
                                        error.contains("payment has been", ignoreCase = true)) {
                                        Log.d("BookingSummary", "Error message indicates success, navigating to confirmation")
                                        // Try to get amount from booking data
                                        val amount = bookingData?.totalAmount ?: 0.0
                                        errorMessage = null
                                        navController?.navigate(
                                            Routes.bookingConfirmationScreen(amount, "pay_after_service")
                                        ) {
                                            popUpTo(0) { inclusive = true }
                                        } ?: run {
                                            Log.w("BookingSummary", "NavController is null, calling onProceedToPay")
                                            onProceedToPay()
                                        }
                                    } else {
                                        errorMessage = error
                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        }
                        "Pay with Super Coins" -> {
                            Log.d("BookingSummary", "Processing Wallet/Super Coins payment")
                            viewModel.makeBookingPayment(
                                bookingId = bookingId,
                                paymentOption = "pay_with_wallet",
                                couponId = selectedCouponId,
                                onSuccess = { paymentData ->
                                    Log.d("BookingSummary", "Wallet payment success: amount=${paymentData.amount}")
                                    
                                    // Refresh active booking status after payment success
                                    val userId = preferencesManager.getUserId()
                                    val token = preferencesManager.getJwtToken()
                                    if (userId != null && token != null) {
                                        Log.d("BookingSummary", "Refreshing active booking status after payment")
                                        homepageServiceViewModel.checkActiveBooking(userId, token)
                                    }
                                    
                                    navController?.navigate(
                                        Routes.bookingConfirmationScreen(paymentData.amount, "wallet")
                                    ) {
                                        popUpTo(0) { inclusive = true }
                                    } ?: run {
                                        Log.w("BookingSummary", "NavController is null, calling onProceedToPay")
                                        onProceedToPay()
                                    }
                                },
                                onError = { error ->
                                    Log.e("BookingSummary", "Wallet payment error: $error")
                                    errorMessage = error
                                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                        "Online Payment" -> {
                            Log.d("BookingSummary", "Processing Online Payment")
                            viewModel.makeBookingPayment(
                                bookingId = bookingId,
                                paymentOption = "pay_online",
                                couponId = selectedCouponId,
                                onSuccess = { paymentData ->
                                    Log.d("BookingSummary", "Payment API success: orderId=${paymentData.orderId}, amount=${paymentData.amount}")
                                    
                                    // Validate that orderId is present before proceeding
                                    val orderId = paymentData.orderId
                                    if (orderId.isNullOrBlank()) {
                                        Log.e("BookingSummary", "❌ No order ID returned from payment API")
                                        errorMessage = "No order ID received from payment API. Please try again."
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        return@makeBookingPayment
                                    }
                                    
                                    // Get Razorpay key from constants (same as product payment)
                                    val razorpayKey = com.example.carzorrouserside.ui.theme.viewmodel.homescreen.OrderViewModel.RAZORPAY_KEY
                                    
                                    // Use actual booking amount - fallback to bookingData if paymentData amount is 0 or invalid
                                    val actualAmount = if (paymentData.amount > 0) {
                                        paymentData.amount
                                    } else {
                                        bookingData?.totalAmount ?: 0.0
                                    }
                                    
                                    // Validate amount is greater than 0
                                    if (actualAmount <= 0) {
                                        Log.e("BookingSummary", "❌ Invalid amount: $actualAmount")
                                        errorMessage = "Invalid payment amount. Please try again."
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        return@makeBookingPayment
                                    }
                                    
                                    Log.d("BookingSummary", "✅ Valid payment data received - Order ID: $orderId, Amount: $actualAmount")
                                    Log.d("BookingSummary", "Using amount for Razorpay: $actualAmount (from paymentData: ${paymentData.amount}, from booking: ${bookingData?.totalAmount})")
                                    
                                    // Set payment flow state to trigger Razorpay ONLY after successful validation
                                    Log.d("BookingSummary", "Setting payment flow state to trigger Razorpay")
                                    // Clear previous state first, then set new state with a small delay
                                    paymentFlowState = null
                                    coroutineScope.launch {
                                        delay(100) // Small delay to ensure state is cleared and LaunchedEffect resets
                                        // Set new payment flow state - LaunchedEffect will trigger automatically
                                        paymentFlowState = BookingPaymentFlowState(
                                            orderId = orderId,
                                            amount = actualAmount,
                                            razorpayKey = razorpayKey,
                                            bookingId = bookingId
                                        )
                                        Log.d("BookingSummary", "✅ Payment flow state set - LaunchedEffect should trigger Razorpay")
                                    }
                                },
                                onError = { error ->
                                    Log.e("BookingSummary", "❌ Payment API error: $error")
                                    // Check if error message indicates success despite error status
                                    // Some APIs return error status with success message
                                    if (error.contains("successfully", ignoreCase = true) || 
                                        error.contains("succesfully", ignoreCase = true) ||
                                        error.contains("payment made", ignoreCase = true) ||
                                        error.contains("payment has been", ignoreCase = true)) {
                                        Log.d("BookingSummary", "Error message indicates success, treating as success")
                                        errorMessage = null
                                        
                                        // Refresh active booking status after payment success
                                        val userId = preferencesManager.getUserId()
                                        val token = preferencesManager.getJwtToken()
                                        if (userId != null && token != null) {
                                            Log.d("BookingSummary", "Refreshing active booking status after payment")
                                            homepageServiceViewModel.checkActiveBooking(userId, token)
                                        }
                                        
                                        // Navigate to confirmation screen
                                        val amount = bookingData?.totalAmount ?: 0.0
                                        coroutineScope.launch {
                                            delay(100) // Small delay to ensure state is updated
                                            try {
                                                navController?.navigate(
                                                    Routes.bookingConfirmationScreen(amount, "online")
                                                ) {
                                                    popUpTo(0) { inclusive = true }
                                                } ?: run {
                                                    Log.w("BookingSummary", "NavController is null, calling onProceedToPay")
                                                    onProceedToPay()
                                                }
                                            } catch (e: Exception) {
                                                Log.e("BookingSummary", "Navigation error: ${e.message}", e)
                                                onProceedToPay()
                                            }
                                        }
                                    } else {
                                        errorMessage = error
                                        Toast.makeText(context, "Payment error: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }

        // Content based on booking data availability
        bookingData?.let { currentBookingData ->
            BookingSummaryContent(
                bookingData = currentBookingData,
                uiState = uiState,
                scrollState = scrollState,
                userDetails = userDetails,
                useSuperCoins = useSuperCoins,
                onUseSuperCoinsChange = { useSuperCoins = it },
                selectedCouponId = selectedCouponId,
                appliedCouponDiscount = appliedCouponDiscount,
                appliedCouponCode = appliedCouponCode,
                showCouponDialog = showCouponDialog,
                onShowCouponDialogChange = { showCouponDialog = it },
                onAddAddonService = { serviceName, price ->
                    viewModel.toggleAddonService(serviceName, price)
                },
                onProceedToPay = {
                    when (val action = viewModel.onProceedToPayClicked()) {
                        is PaymentAction.PROCEED_TO_PAYMENT -> {
                            // User is authenticated, show payment method dialog
                            showPaymentDialog = true
                        }
                        is PaymentAction.REDIRECT_TO_LOGIN -> {
                            // User needs to login first
                            onNavigateToLogin()
                        }
                        is PaymentAction.ERROR -> {
                            // Handle error (you can show a snackbar here)
                            // For now, just log it
                        }
                    }
                },
                onCouponApplied = { couponId, discount, code ->
                    selectedCouponId = couponId
                    appliedCouponDiscount = discount
                    appliedCouponCode = code
                },
                onCouponRemoved = {
                    selectedCouponId = null
                    appliedCouponDiscount = null
                    appliedCouponCode = null
                },
                viewModel = viewModel,
                bookingDetails = bookingDetails
            )
        } ?: run {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = appPrimary)
            }
        }
    }
}

@Composable
private fun BookingSummaryContent(
    bookingData: BookingData,
    uiState: BookingSummaryUiState,
    scrollState: ScrollState,
    userDetails: com.example.carzorrouserside.data.model.profile.UserBasicDetails?,
    useSuperCoins: Boolean,
    onUseSuperCoinsChange: (Boolean) -> Unit,
    selectedCouponId: Int?,
    appliedCouponDiscount: Double?,
    appliedCouponCode: String?,
    showCouponDialog: Boolean,
    onShowCouponDialogChange: (Boolean) -> Unit,
    onAddAddonService: (String, Double) -> Unit,
    onProceedToPay: () -> Unit,
    onCouponApplied: (Int, Double, String) -> Unit,
    onCouponRemoved: () -> Unit,
    viewModel: BookingSummaryViewModel,
    bookingDetails: com.example.carzorrouserside.data.model.booking.BookingDetailsData?
) {
    val context = LocalContext.current
    
    // Coupon Dialog
    if (showCouponDialog) {
        CouponInputDialog(
            onDismissRequest = { onShowCouponDialogChange(false) },
            onApplyCoupon = { couponCode ->
                onShowCouponDialogChange(false)
                // Get required data from booking
                val vendorId = bookingData.vendorId
                val userAddressId = bookingDetails?.userAddressId ?: 0
                val carType = bookingData.carType
                val featureType = bookingData.serviceType
                
                if (userAddressId == 0) {
                    Toast.makeText(context, "Address information not available", Toast.LENGTH_LONG).show()
                    return@CouponInputDialog
                }
                
                viewModel.checkCoupon(
                    couponCode = couponCode,
                    vendorId = vendorId,
                    userAddressId = userAddressId,
                    carType = carType,
                    featureType = featureType,
                    onSuccess = { couponData ->
                        Log.d("BookingSummary", "Coupon applied: ${couponData.couponId}, discount: ${couponData.discountPercentage}%")
                        // Calculate discount amount
                        val discountAmount = (bookingData.totalAmount * couponData.discountPercentage / 100).coerceAtMost(couponData.discountUpto)
                        onCouponApplied(couponData.couponId, discountAmount, couponCode)
                        Toast.makeText(context, "Coupon applied successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Log.e("BookingSummary", "Coupon error: $error")
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Authentication Status Banner (for debugging)
            if (uiState.isUserAuthenticated) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Green
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = "✅ Logged in as User ID: ${uiState.userId}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            uiState.userPhone?.let {
                                Text(
                                    text = "Phone: $it",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_person_24),
                            contentDescription = null,
                            tint = Color.Red
                        )
                        Text(
                            text = "🔒 Guest Mode - Login required for payment",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Service Type Section
            SectionTitle(title = "Service Type")
            Text(
                text = bookingData.serviceType,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Car Type Section
            SectionTitle(title = "Car Type")
            Text(
                text = bookingData.carType,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Service Details Card
            ServiceDetailsCard(bookingData = bookingData)

            // Date Section
            SectionTitle(title = "Date")
            Text(
                text = bookingData.dateTime,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Duration Section
            SectionTitle(title = "Duration")
            Text(
                text = bookingData.duration,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // About Vendor Section
            AboutVendorSection(bookingData = bookingData)

            // About Customer Section
            AboutCustomerSection(bookingData = bookingData)

            // Special Deals Card
            SpecialDealsCard()

            // Add-on Services
            bookingData.addOnServices.forEach { addon ->
                AddonServiceItem(
                    serviceName = addon.name,
                    price = "₹ ${addon.price}",
                    isAdded = addon.isAdded,
                    onAddClick = { onAddAddonService(addon.name, addon.price) }
                )
            }

            // Exclusive Cashback Card
            ExclusiveCashbackCard()

            // Pay using super coins
            SuperCoinsSection(
                walletBalance = userDetails?.walletBalance ?: 0.0,
                isChecked = useSuperCoins,
                onCheckedChange = onUseSuperCoinsChange
            )

            // Price Detail Section
            PriceDetailSection(
                bookingData = bookingData,
                appliedCouponDiscount = appliedCouponDiscount,
                appliedCouponCode = appliedCouponCode,
                onCouponClick = {
                    if (appliedCouponCode != null) {
                        // Remove coupon
                        onCouponRemoved()
                        Toast.makeText(context, "Coupon removed", Toast.LENGTH_SHORT).show()
                    } else {
                        // Show coupon dialog
                        onShowCouponDialogChange(true)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Section
        BottomSection(
            savings = bookingData.savings,
            isAuthenticated = uiState.isUserAuthenticated,
            onProceedToPay = onProceedToPay
        )
    }
}

@Composable
private fun ServiceDetailsCard(bookingData: BookingData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bookingData.serviceName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row {
                        repeat(5) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_star_24),
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = bookingData.rating.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.tabler_clock),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = bookingData.duration,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Text(
                    text = bookingData.serviceDescription,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "₹ ${bookingData.discountedPrice}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "₹ ${bookingData.originalPrice}",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    )

                    Text(
                        text = "${bookingData.discountPercentage}% OFF",
                        color = Color.Green,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Service Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AboutVendorSection(bookingData: BookingData) {
    SectionTitle(title = "About Vendor")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = bookingData.vendorName,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_star_24),
                    contentDescription = null,
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = bookingData.vendorRating.toString(),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Text(
                    text = "(${bookingData.vendorReviews} Reviews)",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Text(
            text = bookingData.vendorDistance,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = bookingData.vendorLocation,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun AboutCustomerSection(bookingData: BookingData) {
    SectionTitle(title = "About Customer")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = bookingData.customerName,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Text(
                text = bookingData.customerAddress,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        IconButton(
            onClick = { /* Handle phone call */ }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Call",
                    tint = appPrimary
                )

                Text(
                    text = bookingData.customerPhone,
                    color = appPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SpecialDealsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0E6FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFE6E6))
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.gift),
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "Special Deals Unlocked",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "Frequently Get Service Together",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ExclusiveCashbackCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appPrimary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.premium),
                contentDescription = null,
                tint = appPrimary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "Exclusive 10% cashback benefit!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Row {
                    Text(
                        text = "You will earn ",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "100 coins cashback",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = appPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SuperCoinsSection(
    walletBalance: Double = 0.0,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = walletBalance > 0,
            colors = CheckboxDefaults.colors(
                checkedColor = appPrimary
            )
        )

        Column(
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text(
                text = "Pay using super coins",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Text(
                text = "Available balance: ₹${String.format("%.2f", walletBalance)}",
                color = if (walletBalance > 0) Color.Gray else Color.Gray.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PriceDetailSection(
    bookingData: BookingData,
    appliedCouponDiscount: Double? = null,
    appliedCouponCode: String? = null,
    onCouponClick: () -> Unit = {}
) {
    SectionTitle(title = "Price Detail")

    val priceDetails = bookingData.priceDetails

    PriceRow(label = "Price", amount = "₹${priceDetails.basePrice}")
    PriceRow(label = "Discount (5% off)", amount = "₹${priceDetails.discount}", isPositive = false)
    PriceRow(label = "Taxes", amount = "₹${priceDetails.taxes}", isNegative = true)
    PriceRow(label = "Platform Fee", amount = "₹${priceDetails.platformFee}")

    // Coupon Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (appliedCouponCode != null && appliedCouponDiscount != null) {
            // Show applied coupon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCouponClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.gift),
                    contentDescription = null,
                    tint = Color.Green,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = appliedCouponCode,
                    color = Color.Green,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Text(
                    text = " (Tap to remove)",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(
                text = "-₹${String.format("%.2f", appliedCouponDiscount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Green
            )
        } else {
            OutlinedButton(
                onClick = onCouponClick,
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.gift),
                        contentDescription = null,
                        tint = appPrimary,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = "Coupon",
                        color = Color.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Text(
                text = "₹${priceDetails.couponDiscount}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }

    Divider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = Color.LightGray
    )

    // Total Amount
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Total Amount",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "₹${bookingData.totalAmount}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PriceRow(
    label: String,
    amount: String,
    isPositive: Boolean = true,
    isNegative: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Text(
            text = amount,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = when {
                !isPositive -> Color.Green
                isNegative -> Color.Red
                else -> Color.Black
            }
        )
    }
}

@Composable
private fun BottomSection(
    savings: Double,
    isAuthenticated: Boolean,
    onProceedToPay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // Savings Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFECFFEE))
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.Green,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "You Will Save ₹$savings On This Order",
                    color = Color.Green,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Pay Button
        Button(
            onClick = onProceedToPay,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appPrimary
            )
        ) {
            Text(
                text = if (isAuthenticated) "Proceed to pay" else "Login to continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun AddonServiceItem(
    serviceName: String,
    price: String,
    isAdded: Boolean = false,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = serviceName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )

                Text(
                    text = price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAdded) Color.Red else appPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isAdded) R.drawable.baseline_remove_24 else R.drawable.baseline_add_24
                    ),
                    contentDescription = if (isAdded) "Remove" else "Add",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = if (isAdded) "Remove" else "Add",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodDialog(
    onDismissRequest: () -> Unit,
    onNextClick: (String) -> Unit,
    walletBalance: Double = 0.0
) {
    val paymentOptions = mutableListOf("Pay on service", "Online Payment")
    if (walletBalance > 0) {
        paymentOptions.add(0, "Pay with Super Coins") // Add at the beginning if wallet has balance
    }
    var selectedOption by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                paymentOptions.forEach { option ->
                    PaymentOptionRow(
                        text = option,
                        selected = selectedOption == option,
                        onClick = { selectedOption = option }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onNextClick(selectedOption) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    enabled = selectedOption.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9D8CFF),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        "Next",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (selected) Color(0xFF9D8CFF) else Color(0xFFDBE0E2),
                RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(if (selected) Color(0xFF9D8CFF).copy(alpha = 0.05f) else Color.White)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color(0xFF1A1C1E)
        )
        Checkbox(
            checked = selected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF9D8CFF),
                uncheckedColor = Color(0xFF49454F)
            ),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CouponInputDialog(
    onDismissRequest: () -> Unit,
    onApplyCoupon: (String) -> Unit
) {
    var couponCode by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Apply Coupon",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                OutlinedTextField(
                    value = couponCode,
                    onValueChange = { couponCode = it.uppercase().trim() },
                    label = { Text("Enter coupon code") },
                    placeholder = { Text("e.g., SAVE10") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appPrimary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text(
                            "Cancel",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = { 
                            if (couponCode.isNotBlank()) {
                                onApplyCoupon(couponCode)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        enabled = couponCode.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appPrimary,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            "Apply",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UserPreferencesManagerEntryPoint {
    fun userPreferencesManager(): UserPreferencesManager
}