package com.example.carzorrouserside.ui.theme.viewmodel.homescreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.booking.OfferActionRequest
import com.example.carzorrouserside.data.model.homescreen.Bid
import com.example.carzorrouserside.data.model.homescreen.HomepageServiceUiState
import com.example.carzorrouserside.data.repository.homescreen.HomepageServiceRepository
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.domain.repository.BookingRepository
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomepageServiceViewModel @Inject constructor(
    private val repository: HomepageServiceRepository,
    private val bookingRepository: BookingRepository,
    private val userPreferencesManager: UserPreferencesManager // ✅ Injected via Hilt (singleton)
) : ViewModel() {

    companion object {
        private const val TAG = "HomepageServiceViewModel"
    }

    private val _uiState = MutableStateFlow(HomepageServiceUiState())
    val uiState: StateFlow<HomepageServiceUiState> = _uiState.asStateFlow()

    init {
        resetBookingState()
        loadServices()
    }

    // ✅ Fetch home screen services
    fun loadServices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = repository.getHomepageServices()) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ Services loaded: ${result.data?.size}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        services = result.data ?: emptyList(),
                        error = null
                    )
                }

                is Resource.Error -> {
                    Log.e(TAG, "❌ Failed to load services: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun retryLoadServices() {
        Log.d(TAG, "🔁 Retrying to load services")
        loadServices()
    }

    // ✅ Active booking checker with proper datastore integration
    fun checkActiveBooking(
        userId: Int,
        token: String
    ) {
        viewModelScope.launch {
            when (val result = repository.getActiveBooking(userId, token)) {
                is Resource.Success -> {
                    result.data?.let { response ->
                        if (response.success) {
                            val key = response.data?.key
                            Log.d(TAG, "✅ API Success — Key: $key")

                            when (key) {
                                "active_booking" -> {
                                    val bookingData = response.data
                                    val bids = bookingData?.bids ?: emptyList()
                                    val bookingStatus = bookingData?.booking?.booking_status
                                    val startOtp = bookingData?.booking?.startOtp
                                    
                                    // ✅ Prevent rebids if booking is accepted by vendor
                                    val isAccepted = bookingStatus?.lowercase() == "accepted" || 
                                                    bookingStatus?.lowercase() == "vendor_accepted"
                                    val shouldShowRebids = bids.isNotEmpty() && !isAccepted

                                    _uiState.value = _uiState.value.copy(
                                        backendResponded = true,
                                        hasActiveBooking = true,
                                        hasRebid = shouldShowRebids,
                                        activeBookingData = bookingData,
                                        activeBids = if (isAccepted) emptyList() else bids,
                                        bookingStatus = bookingStatus,
                                        activeBookingMessage = if (isAccepted)
                                            "Booking accepted by vendor"
                                        else if (bids.isNotEmpty())
                                            "Active booking with offers"
                                        else "Active booking found"
                                    )

                                    Log.d(TAG, "📦 Active Booking Found: ${bookingData?.booking}")
                                    Log.d(TAG, "💰 Bids Found: ${bids.size}")
                                    Log.d(TAG, "📋 Booking Status = $bookingStatus")
                                    Log.d(TAG,"Code = $startOtp")

                                    // ✅ Save booking status to DataStore
                                    if (!bookingStatus.isNullOrBlank()) {
                                        try {
                                            userPreferencesManager.saveBookingStatus(bookingStatus)
                                            Log.d(TAG, "💾 Booking status saved in DataStore: $bookingStatus")
                                        } catch (e: Exception) {
                                            Log.e(TAG, "❌ Failed to save booking status: ${e.message}")
                                        }
                                    }

                                    // ✅ Persist pending booking locally
                                    bookingData?.booking?.let { booking ->
                                        val bookingId = booking.id
                                        if (bookingId != null && bookingId > 0) {
                                            userPreferencesManager.savePendingBooking(
                                                pendingBookingId = bookingId,
                                                route = Routes.BOOKING_SCREEN,
                                                routeArgsJson = """{"isRequestSending":true}""",
                                                sheetType = "SET_PRICE"
                                            )
                                            Log.d(TAG, "♻️ Re-saved pending booking (id=$bookingId)")
                                        }
                                    }
                                }

                                "booking_rebid" -> {
                                    val bids = response.data?.bids ?: emptyList()
                                    _uiState.value = _uiState.value.copy(
                                        backendResponded = true,
                                        hasActiveBooking = false,
                                        hasRebid = true,
                                        activeBids = bids,
                                        activeBookingData = response.data,
                                        activeBookingMessage = "Rebid offers found"
                                    )
                                    Log.d(TAG, "🔁 Rebid Found — Total Bids: ${bids.size}")
                                }

                                "none" -> {
                                    _uiState.value = _uiState.value.copy(
                                        backendResponded = true,
                                        hasActiveBooking = false,
                                        hasRebid = false,
                                        activeBids = emptyList(),
                                        activeBookingData = null,
                                        activeBookingMessage = "No active booking"
                                    )
                                    Log.d(TAG, "🚫 No active booking — clearing stored pending booking!")

                                    // CLEAR EVERYTHING (very important)
                                    userPreferencesManager.clearPendingBooking()
                                    userPreferencesManager.saveBookingStatus("")
                                }

                                else -> Log.w(TAG, "⚠️ Unknown key received: $key")
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(
                                backendResponded = true,
                                activeBookingMessage = response.message,
                                hasActiveBooking = false,
                                hasRebid = false
                            )
                            Log.w(TAG, "❌ API returned success=false: ${response.message}")
                        }
                    }
                }

                is Resource.Error -> {
                    Log.e(TAG, "🔥 Error fetching active booking: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        backendResponded = true,
                        activeBookingMessage = result.message,
                        hasActiveBooking = false,
                        hasRebid = false
                    )
                }

                else -> Unit
            }
        }
    }
    fun isBackendBookingNone(): Boolean {
        return uiState.value.run {
            !hasActiveBooking &&
                    !hasRebid &&
                    (bookingStatus.isNullOrBlank() || bookingStatus == "none" ||
                            activeBookingData?.key == "none")
        }
    }

    fun resetBookingState() {
        _uiState.value = _uiState.value.copy(
            backendResponded = false,
            hasRebid = false,
            activeBookingData = null,
            activeBids = emptyList(),

        )
        Log.d(TAG, "🧹 Reset booking state on ViewModel init")
    }

    // ✅ Accept booking rebid offer
    fun acceptBookingOffer(
        bid: Bid,
        onSuccess: (Int, Int) -> Unit, // (bookingId, vendorId) for navigation
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "✅ Accepting booking offer - bookingId: ${bid.bookingId}, vendorId: ${bid.vendorId}")
                
                val request = OfferActionRequest(
                    booking_id = bid.bookingId,
                    vendor_id = bid.vendorId
                )

                val result = bookingRepository.acceptBookingOffer(request)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "✅ Booking offer accepted successfully")
                        
                        // Remove the accepted bid from the list
                        val updatedBids = _uiState.value.activeBids.filter { it.id != bid.id }
                        
                        // Update booking status to "accepted"
                        _uiState.value = _uiState.value.copy(
                            activeBids = updatedBids,
                            hasRebid = updatedBids.isNotEmpty(),
                            bookingStatus = "accepted"
                        )
                        
                        // Save booking status
                        userPreferencesManager.saveBookingStatus("accepted")
                        
                        // Navigate to booking summary with booking ID
                        onSuccess(bid.bookingId, bid.vendorId)
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("Booking Accepted by Other.", ignoreCase = true) == true -> {
                                "This booking has already been accepted by another user."
                            }
                            exception.message != null -> exception.message!!
                            else -> "Failed to accept booking offer. Please try again."
                        }
                        Log.e(TAG, "❌ Failed to accept booking offer: $errorMessage")
                        onError(errorMessage)
                    }
                )
            } catch (e: Exception) {
                val errorMessage = "An unexpected error occurred. Please try again."
                Log.e(TAG, "❌ Exception while accepting booking offer: ${e.message}", e)
                onError(errorMessage)
            }
        }
    }

    // ✅ Clear rebids when vendor accepts booking (called from FCM notification)
    fun clearRebidsForAcceptedBooking() {
        Log.d(TAG, "🚫 Clearing rebids - vendor accepted booking")
        _uiState.value = _uiState.value.copy(
            hasRebid = false,
            activeBids = emptyList(),
            bookingStatus = "accepted"
        )
        viewModelScope.launch {
            userPreferencesManager.saveBookingStatus("accepted")
        }
    }

    // ✅ Decline booking rebid offer
    fun declineBookingOffer(
        bid: Bid,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "❌ Declining booking offer - bookingId: ${bid.bookingId}, vendorId: ${bid.vendorId}")
                
                val request = OfferActionRequest(
                    booking_id = bid.bookingId,
                    vendor_id = bid.vendorId
                )

                val result = bookingRepository.declineBookingOffer(request)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "✅ Booking offer declined successfully")
                        
                        // Remove the declined bid from the list
                        val updatedBids = _uiState.value.activeBids.filter { it.id != bid.id }
                        
                        _uiState.value = _uiState.value.copy(
                            activeBids = updatedBids,
                            hasRebid = updatedBids.isNotEmpty()
                        )
                        
                        // If no more bids, clear rebid state
                        if (updatedBids.isEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                hasRebid = false,
                                activeBookingMessage = "No active rebid offers"
                            )
                        }
                        
                        onSuccess()
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("Booking Accepted by Other.", ignoreCase = true) == true -> {
                                "This booking has already been accepted by another user."
                            }
                            exception.message != null -> exception.message!!
                            else -> "Failed to decline booking offer. Please try again."
                        }
                        Log.e(TAG, "❌ Failed to decline booking offer: $errorMessage")
                        onError(errorMessage)
                    }
                )
            } catch (e: Exception) {
                val errorMessage = "An unexpected error occurred. Please try again."
                Log.e(TAG, "❌ Exception while declining booking offer: ${e.message}", e)
                onError(errorMessage)
            }
        }
    }

}
