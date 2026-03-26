package com.example.carzorrouserside.ui.theme.viewmodel.session


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.AddOnService
import com.example.carzorrouserside.data.model.BookingData
import com.example.carzorrouserside.data.token.BookingSessionManager
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.domain.repository.BookingRepository
import com.example.carzorrouserside.data.repository.vendor.VendorDetailsRepository
import com.example.carzorrouserside.data.repository.profile.ProfileRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingSummaryViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val bookingSessionManager: BookingSessionManager,
    private val bookingRepository: BookingRepository,
    private val vendorDetailsRepository: VendorDetailsRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    companion object {
        private const val TAG = "BookingSummaryViewModel"
    }

    private val _uiState = MutableStateFlow(BookingSummaryUiState())
    val uiState: StateFlow<BookingSummaryUiState> = _uiState.asStateFlow()

    private val _bookingData = MutableStateFlow<BookingData?>(null)
    val bookingData: StateFlow<BookingData?> = _bookingData.asStateFlow()
    
    private var currentBookingId: Int? = null
    private var _bookingDetails = MutableStateFlow<com.example.carzorrouserside.data.model.booking.BookingDetailsData?>(null)
    val bookingDetails: StateFlow<com.example.carzorrouserside.data.model.booking.BookingDetailsData?> = _bookingDetails.asStateFlow()
    
    private var _userDetails = MutableStateFlow<com.example.carzorrouserside.data.model.profile.UserBasicDetails?>(null)
    val userDetails: StateFlow<com.example.carzorrouserside.data.model.profile.UserBasicDetails?> = _userDetails.asStateFlow()

    init {
        Log.d(TAG, "🚀 BookingSummaryViewModel initialized")
        checkAuthenticationState()
        loadBookingData()
        fetchUserDetails()
    }
    
    /**
     * Fetch user basic details including wallet balance
     */
    private fun fetchUserDetails() {
        if (!userPreferencesManager.validateAuthenticationState()) {
            Log.d(TAG, "User not authenticated, skipping user details fetch")
            return
        }
        
        viewModelScope.launch {
            when (val result = profileRepository.getUserBasicDetails()) {
                is Resource.Success -> {
                    _userDetails.value = result.data
                    Log.d(TAG, "✅ User details fetched: ${result.data?.fullName}, Wallet: ${result.data?.walletBalance}")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Failed to fetch user details: ${result.message}")
                }
                is Resource.Loading -> {
                    // Loading state
                }
            }
        }
    }

    /**
     * Initialize with booking ID and fetch booking details from API
     */
    fun initializeBooking(bookingId: Int) {
        Log.d(TAG, "🎯 Initializing booking for booking ID: $bookingId")
        currentBookingId = bookingId
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            bookingRepository.getBookingDetails(bookingId).fold(
                onSuccess = { bookingDetails ->
                    Log.d(TAG, "✅ Booking details fetched successfully")
                    Log.d(TAG, "📦 Raw API Response:")
                    Log.d(TAG, "   ├─ Booking ID: ${bookingDetails.id}")
                    Log.d(TAG, "   ├─ Booking Status: ${bookingDetails.bookingStatus}")
                    
                    // Check if vendor object exists in response
                    if (bookingDetails.vendor != null) {
                        Log.d(TAG, "   ├─ Vendor Object Found:")
                        Log.d(TAG, "      ├─ Vendor ID: ${bookingDetails.vendor.id}")
                        Log.d(TAG, "      ├─ Vendor Name: ${bookingDetails.vendor.name}")
                        Log.d(TAG, "      ├─ Vendor Rating: ${bookingDetails.vendor.rating}")
                        Log.d(TAG, "      └─ Vendor Phone: ${bookingDetails.vendor.phone}")
                    } else {
                        Log.d(TAG, "   ├─ Vendor Object: NULL")
                        // Fallback to individual fields if vendor object is not present
                        Log.d(TAG, "   ├─ Vendor ID (fallback): ${bookingDetails.vendorId}")
                        Log.d(TAG, "   ├─ Vendor Name (fallback): ${bookingDetails.vendorName}")
                        Log.d(TAG, "   ├─ Vendor Rating (fallback): ${bookingDetails.vendorRating}")
                    }
                    
                    // Use vendor object if available, otherwise fallback to individual fields or fetch
                    val vendorId = bookingDetails.vendor?.id ?: bookingDetails.vendorId
                    val vendorName = bookingDetails.vendor?.name ?: bookingDetails.vendorName
                    
                    // If vendor object is missing and we have vendorId, try to fetch vendor details
                    if (bookingDetails.vendor == null && vendorId != null && vendorId > 0 && 
                        (vendorName.isNullOrBlank() || vendorName.equals("N/A", ignoreCase = true))) {
                        Log.d(TAG, "🔄 Vendor object missing, fetching from vendor API for vendorId: $vendorId")
                        fetchVendorDetails(vendorId, bookingDetails)
                    } else {
                        _bookingDetails.value = bookingDetails
                        // Refresh user details if not already loaded
                        if (_userDetails.value == null) {
                            fetchUserDetails()
                        }
                        val bookingData = convertToBookingData(bookingDetails, _userDetails.value)
                        _bookingData.value = bookingData
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        logBookingDetails(bookingData)
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "❌ Failed to fetch booking details: ${exception.message}", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to fetch booking details"
                    )
                }
            )
        }
    }
    
    /**
     * Initialize with vendor ID (legacy method for backward compatibility)
     */
    fun initializeBookingWithVendorId(vendorId: Int) {
        Log.d(TAG, "🎯 Initializing booking for vendor ID: $vendorId (legacy method)")

        // Create sample booking data (replace with actual data from your source)
        val bookingData = createSampleBookingData(vendorId)
        _bookingData.value = bookingData

        Log.d(TAG, "✅ Booking data initialized")
        logBookingDetails(bookingData)
    }
    
    /**
     * Convert API booking details to BookingData model
     */
    private fun convertToBookingData(
        details: com.example.carzorrouserside.data.model.booking.BookingDetailsData,
        userDetails: com.example.carzorrouserside.data.model.profile.UserBasicDetails? = null
    ): BookingData {
        val address = details.address
        val addressString = address?.let {
            buildString {
                if (!it.flatNo.isNullOrBlank()) append("${it.flatNo}, ")
                if (!it.landmark.isNullOrBlank()) append("${it.landmark}, ")
                append("${it.city}, ${it.state}, ${it.country}")
            }
        } ?: "Address not available"
        
        // Format date time - prefer service_time or date_time over created_time
        val dateTimeString = details.serviceTime ?: details.dateTime ?: details.createdTime
        val formattedDateTime = try {
            if (!dateTimeString.isNullOrBlank()) {
                // Parse and format the date time string
                val instant = java.time.Instant.parse(dateTimeString.replace(" ", "T") + "Z")
                val dateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy 'At' hh:mm a")
                dateTime.format(formatter)
            } else ""
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse date time: $dateTimeString", e)
            dateTimeString ?: ""
        }
        
        // Extract vendor details from vendor object (preferred) or fallback to individual fields
        val vendor = details.vendor
        val vendorId = vendor?.id ?: details.vendorId ?: 0
        val vendorName = when {
            vendor?.name?.isNotBlank() == true -> vendor.name
            details.vendorName?.isNotBlank() == true && !details.vendorName.equals("N/A", ignoreCase = true) -> details.vendorName
            else -> "Vendor"
        }
        
        // Parse vendor rating - can be String or Double in the response
        val vendorRating = when {
            vendor?.rating != null -> {
                try {
                    vendor.rating.toDoubleOrNull()?.toFloat() ?: 0.0f
                } catch (e: Exception) {
                    0.0f
                }
            }
            details.vendorRating != null -> details.vendorRating.toFloat()
            else -> 0.0f
        }
        
        // Vendor reviews - not directly in vendor object, use fallback if available
        val vendorReviews = details.vendorReviews ?: 0
        
        return BookingData(
            vendorId = vendorId,
            serviceType = details.serviceType ?: "Doorstep service",
            carType = "Car", // Not provided in API response - could be fetched separately
            serviceName = when (details.serviceType?.lowercase()) {
                "doorstep" -> "Doorstep Car Service"
                "workshop" -> "Workshop Service"
                else -> "Car Service"
            },
            serviceDescription = "Professional car service booking", // Not provided in API response
            rating = 4.7f, // Not provided in API response
            duration = "35mins", // Not provided in API response
            originalPrice = details.bookingUserCurrentPrice,
            discountedPrice = details.bookingUserCurrentPrice,
            discountPercentage = 0,
            dateTime = formattedDateTime,
            vendorName = vendorName,
            vendorRating = vendorRating,
            vendorReviews = vendorReviews,
            vendorDistance = "N/A", // Not provided in API response
            vendorLocation = address?.city ?: "N/A",
            customerName = userDetails?.fullName ?: "Customer",
            customerAddress = addressString,
            customerPhone = userDetails?.phone ?: "+91 0000000000",
            addOnServices = emptyList(),
            priceDetails = com.example.carzorrouserside.data.model.PriceDetails(
                basePrice = details.bookingUserCurrentPrice,
                discount = 0.0,
                taxes = 0.0,
                platformFee = 0.0,
                couponDiscount = 0.0
            ),
            totalAmount = details.bookingUserCurrentPrice,
            savings = 0.0
        )
    }

    /**
     * Check if user is authenticated
     */
    private fun checkAuthenticationState() {
        viewModelScope.launch {
            val isAuthenticated = userPreferencesManager.validateAuthenticationState()
            val userId = userPreferencesManager.getUserId()
            val userPhone = userPreferencesManager.getUserPhone()

            Log.d(TAG, "🔍 Authentication Check:")
            Log.d(TAG, "   ├─ Authenticated: $isAuthenticated")
            Log.d(TAG, "   ├─ User ID: ${userId ?: "NULL"}")
            Log.d(TAG, "   └─ Phone: ${userPhone ?: "NULL"}")

            _uiState.value = _uiState.value.copy(
                isUserAuthenticated = isAuthenticated,
                userId = userId,
                userPhone = userPhone
            )
        }
    }

    /**
     * Handle proceed to pay button click
     */
    fun onProceedToPayClicked(): PaymentAction {
        Log.d(TAG, "💰 Proceed to pay clicked")

        val currentBookingData = _bookingData.value
        if (currentBookingData == null) {
            Log.e(TAG, "❌ No booking data available")
            return PaymentAction.ERROR("No booking data available")
        }

        return if (_uiState.value.isUserAuthenticated) {
            Log.d(TAG, "✅ User is authenticated - proceeding to payment")

            // Save booking for payment continuation
            bookingSessionManager.savePendingBooking(currentBookingData)
            bookingSessionManager.updateBookingFlowState("READY_FOR_PAYMENT")

            PaymentAction.PROCEED_TO_PAYMENT(currentBookingData)
        } else {
            Log.d(TAG, "❌ User not authenticated - redirecting to login")

            // Save booking for after login
            bookingSessionManager.savePendingBooking(currentBookingData)
            bookingSessionManager.updateBookingFlowState("AUTH_REQUIRED")

            PaymentAction.REDIRECT_TO_LOGIN(currentBookingData)
        }
    }

    /**
     * Handle authentication success - user returned from login
     */
    fun onAuthenticationSuccess() {
        Log.d(TAG, "🎉 Authentication successful - user returned from login")

        checkAuthenticationState()

        // Check if there's a pending booking
        bookingSessionManager.getPendingBooking()?.let { pendingBooking ->
            Log.d(TAG, "📋 Restoring pending booking data")
            _bookingData.value = pendingBooking
            bookingSessionManager.updateBookingFlowState("READY_FOR_PAYMENT")
        }
    }

    /**
     * Load booking data (from session if available)
     */
    private fun loadBookingData() {
        bookingSessionManager.getPendingBooking()?.let { pendingBooking ->
            Log.d(TAG, "📋 Loading existing booking data from session")
            _bookingData.value = pendingBooking
        }
    }

    /**
     * Add or remove addon service
     */
    fun toggleAddonService(serviceName: String, price: Double) {
        val currentBooking = _bookingData.value ?: return

        val updatedAddons = currentBooking.addOnServices.map { addon ->
            if (addon.name == serviceName) {
                addon.copy(isAdded = !addon.isAdded)
            } else {
                addon
            }
        }

        // Recalculate total
        val addedServicesTotal = updatedAddons.filter { it.isAdded }.sumOf { it.price }
        val newTotal = currentBooking.priceDetails.let {
            it.basePrice + it.taxes + it.platformFee + it.discount + it.couponDiscount
        } + addedServicesTotal

        val updatedBooking = currentBooking.copy(
            addOnServices = updatedAddons,
            totalAmount = newTotal
        )

        _bookingData.value = updatedBooking

        // Update session
        bookingSessionManager.savePendingBooking(updatedBooking)

        Log.d(TAG, "🔄 Updated addon service: $serviceName, Added: ${updatedAddons.find { it.name == serviceName }?.isAdded}")
    }

    /**
     * Clear booking session (after successful payment or cancellation)
     */
    fun clearBookingSession() {
        Log.d(TAG, "🧹 Clearing booking session")
        bookingSessionManager.clearPendingBooking()
        _bookingData.value = null
    }

    /**
     * Debug current state
     */
    fun debugCurrentState() {
        Log.d(TAG, "🔍 === BOOKING SUMMARY STATE ===")
        Log.d(TAG, "📱 UI State: ${_uiState.value}")
        Log.d(TAG, "📱 Booking Data: ${_bookingData.value?.let { "Vendor ID: ${it.vendorId}" } ?: "NULL"}")
        userPreferencesManager.debugCurrentState()
        bookingSessionManager.debugSessionState()
        Log.d(TAG, "🔍 === END BOOKING SUMMARY STATE ===")
    }

    private fun createSampleBookingData(vendorId: Int): BookingData {
        return BookingData(
            vendorId = vendorId,
            addOnServices = listOf(
                AddOnService("Seat Cleaning", 3456.0, false),
                AddOnService("Engine Cleaning", 2500.0, false)
            )
        )
    }

    /**
     * Cancel booking with reason
     */
    fun cancelBooking(
        bookingId: Int? = null,
        reasonId: Int? = null,
        reasonText: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val actualBookingId = bookingId ?: currentBookingId
        if (actualBookingId == null || actualBookingId <= 0) {
            onError("Booking ID not available")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = com.example.carzorrouserside.data.model.booking.CancelBookingRequest(
                booking_id = actualBookingId,
                cancel_reason_id = reasonId,
                cancel_reason_text = reasonText
            )

            bookingRepository.cancelBooking(request).fold(
                onSuccess = {
                    Log.d(TAG, "✅ Booking cancelled successfully")
                    clearBookingSession()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: "Failed to cancel booking"
                    Log.e(TAG, "❌ Failed to cancel booking: $errorMessage", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                    onError(errorMessage)
                }
            )
        }
    }

    /**
     * Fetch vendor details when missing from booking details
     */
    private fun fetchVendorDetails(
        vendorId: Int,
        bookingDetails: com.example.carzorrouserside.data.model.booking.BookingDetailsData
    ) {
        viewModelScope.launch {
            vendorDetailsRepository.getVendorDetails(vendorId).collect { result ->
                result.fold(
                    onSuccess = { vendorData ->
                        Log.d(TAG, "✅ Vendor details fetched successfully")
                        Log.d(TAG, "   ├─ Vendor Name: ${vendorData.vendor.name}")
                        Log.d(TAG, "   ├─ Business Name: ${vendorData.business.businessName ?: vendorData.business.businessNameAlt}")
                        Log.d(TAG, "   ├─ Rating: ${vendorData.rating}")
                        Log.d(TAG, "   └─ Reviews: ${vendorData.reviewCount}")
                        
                        // Create BookingVendor object from fetched vendor data
                        val bookingVendor = com.example.carzorrouserside.data.model.booking.BookingVendor(
                            id = vendorData.vendor.id,
                            name = vendorData.vendor.name,
                            email = vendorData.vendor.email,
                            phone = vendorData.vendor.phone,
                            gender = null, // Not available in vendor details response
                            profilePic = vendorData.vendor.profilePic,
                            cinNumber = null, // Not available in vendor details response
                            cinDocument = null,
                            aadharNumber = null,
                            aadharDocument = null,
                            gstinNumber = null,
                            gstinDocument = null,
                            panNumber = null,
                            panDocument = null,
                            status = vendorData.vendor.status,
                            vendorCompletionStage = null,
                            approvedBy = null,
                            approvedAt = null,
                            businessMethod = null,
                            businessSubMethod = null,
                            createdAt = null,
                            updatedAt = null,
                            securityDepositAmount = null,
                            securityDepositLeft = null,
                            walletBalance = null,
                            penaltyBalance = null,
                            referralCode = null,
                            rating = vendorData.rating.toString() // Convert Double to String
                        )
                        
                        // Create updated booking details with vendor object
                        val updatedBookingDetails = bookingDetails.copy(
                            vendor = bookingVendor,
                            // Keep individual fields for backward compatibility
                            vendorName = vendorData.vendor.name,
                            vendorRating = vendorData.rating,
                            vendorReviews = vendorData.reviewCount,
                            vendorId = vendorData.vendor.id
                        )
                        
                        _bookingDetails.value = updatedBookingDetails
                        val bookingData = convertToBookingData(updatedBookingDetails, _userDetails.value)
                        _bookingData.value = bookingData
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        logBookingDetails(bookingData)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "❌ Failed to fetch vendor details: ${exception.message}", exception)
                        // Fallback: use booking details as-is even without vendor info
                        _bookingDetails.value = bookingDetails
                        val bookingData = convertToBookingData(bookingDetails, _userDetails.value)
                        _bookingData.value = bookingData
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        logBookingDetails(bookingData)
                    }
                )
            }
        }
    }

    /**
     * Get cancellation reasons from API
     */
    fun getCancelReasons(
        onSuccess: (List<com.example.carzorrouserside.data.model.booking.CancelReason>) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            bookingRepository.getBookingCancelReasons().fold(
                onSuccess = { reasons ->
                    Log.d(TAG, "✅ Fetched ${reasons.size} cancellation reasons")
                    onSuccess(reasons)
                },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: "Failed to fetch cancellation reasons"
                    Log.e(TAG, "❌ Failed to fetch cancellation reasons: $errorMessage", exception)
                    onError(errorMessage)
                }
            )
        }
    }

    /**
     * Check coupon validity
     */
    fun checkCoupon(
        couponCode: String,
        vendorId: Int,
        userAddressId: Int,
        carType: String,
        featureType: String? = null,
        onSuccess: (com.example.carzorrouserside.data.model.booking.CouponData) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val request = com.example.carzorrouserside.data.model.booking.CheckCouponRequest(
                couponCode = couponCode,
                vendorId = vendorId,
                userAddressId = userAddressId,
                carType = carType,
                featureType = featureType
            )

            bookingRepository.checkCoupon(request).fold(
                onSuccess = { couponData ->
                    Log.d(TAG, "✅ Coupon validated: ${couponData.couponId}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(couponData)
                },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: "Invalid coupon code"
                    Log.e(TAG, "❌ Coupon validation failed: $errorMessage", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                    onError(errorMessage)
                }
            )
        }
    }

    /**
     * Make payment for booking (Pay on service or Online)
     */
    fun makeBookingPayment(
        bookingId: Int?,
        paymentOption: String, // "pay_online", "pay_after_service", or "pay_with_wallet"
        couponId: Int? = null,
        onSuccess: (com.example.carzorrouserside.data.model.booking.BookingPaymentData) -> Unit,
        onError: (String) -> Unit
    ) {
        val actualBookingId = bookingId ?: currentBookingId
        if (actualBookingId == null || actualBookingId <= 0) {
            onError("Booking ID not available")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val request = com.example.carzorrouserside.data.model.booking.BookingPaymentRequest(
                bookingId = actualBookingId,
                option = paymentOption,
                couponId = couponId
            )

            bookingRepository.makeBookingPayment(request).fold(
                onSuccess = { paymentData ->
                    Log.d(TAG, "✅ Payment processed: ${paymentData.orderId ?: "N/A"}")
                    // For pay_after_service, use booking amount if payment data amount is 0
                    val finalPaymentData = if (paymentOption == "pay_after_service" && paymentData.amount == 0.0) {
                        val bookingAmount = _bookingData.value?.totalAmount ?: 0.0
                        paymentData.copy(amount = bookingAmount)
                    } else {
                        paymentData
                    }
                    if (paymentOption == "pay_after_service") {
                        clearBookingSession()
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(finalPaymentData)
                },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: "Failed to process payment"
                    Log.e(TAG, "❌ Payment processing failed: $errorMessage", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                    onError(errorMessage)
                }
            )
        }
    }

    /**
     * Confirm payment after Razorpay success
     */
    fun confirmBookingPayment(
        bookingId: Int?,
        orderId: String,
        paymentId: String,
        onSuccess: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        val actualBookingId = bookingId ?: currentBookingId
        if (actualBookingId == null || actualBookingId <= 0) {
            onError("Booking ID not available")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val request = com.example.carzorrouserside.data.model.booking.BookingPaymentConfirmRequest(
                bookingId = actualBookingId,
                orderId = orderId,
                paymentId = paymentId
            )

            bookingRepository.confirmBookingPayment(request).fold(
                onSuccess = {
                    Log.d(TAG, "✅ Payment confirmed successfully")
                    val paymentAmount = _bookingData.value?.totalAmount ?: 0.0
                    clearBookingSession()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(paymentAmount)
                },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: "Failed to confirm payment"
                    Log.e(TAG, "❌ Payment confirmation failed: $errorMessage", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                    onError(errorMessage)
                }
            )
        }
    }

    private fun logBookingDetails(bookingData: BookingData) {
        Log.d(TAG, "📋 Booking Details:")
        Log.d(TAG, "   ├─ Vendor ID: ${bookingData.vendorId}")
        Log.d(TAG, "   ├─ Service: ${bookingData.serviceName}")
        Log.d(TAG, "   ├─ Vendor: ${bookingData.vendorName}")
        Log.d(TAG, "   ├─ Date: ${bookingData.dateTime}")
        Log.d(TAG, "   └─ Total: ₹${bookingData.totalAmount}")
    }
}

/**
 * UI State for BookingSummaryScreen
 */
data class BookingSummaryUiState(
    val isUserAuthenticated: Boolean = false,
    val userId: Int? = null,
    val userPhone: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Sealed class for payment actions
 */
sealed class PaymentAction {
    data class PROCEED_TO_PAYMENT(val bookingData: BookingData) : PaymentAction()
    data class REDIRECT_TO_LOGIN(val bookingData: BookingData) : PaymentAction()
    data class ERROR(val message: String) : PaymentAction()
}