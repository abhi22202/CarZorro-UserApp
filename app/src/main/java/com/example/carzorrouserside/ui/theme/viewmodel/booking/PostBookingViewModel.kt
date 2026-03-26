package com.example.carzorrouserside.ui.theme.viewmodel.booking

import android.os.Build
import com.example.carzorrouserside.data.token.UserPreferencesManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.BookingStartRequest
import com.example.carzorrouserside.data.model.BookingStartResponse
import com.example.carzorrouserside.data.model.booking.CancelBookingRequest
import com.example.carzorrouserside.data.repository.BookingRepository
import com.example.carzorrouserside.data.repository.homescreen.HomepageServiceRepository
import com.example.carzorrouserside.domain.repository.BookingRepository as DomainBookingRepository
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class PostBookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val domainBookingRepository: DomainBookingRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val savedStateHandle: SavedStateHandle,
    private val homepageServiceRepository: HomepageServiceRepository
) : ViewModel() {

    // ✅ Store values selected from UI
    var serviceType = MutableStateFlow("doorstep")
    var featureServiceId = MutableStateFlow<String?>(null)
    var amount = MutableStateFlow<String?>(null)
//
var addressId = MutableStateFlow<String?>(null)
    var vehicleId = MutableStateFlow<String?>(null)
    var waterAvailable = MutableStateFlow(false)
    var electricityAvailable = MutableStateFlow(false)

    // ✅ Date & Time
//    private val _selectedDate = MutableStateFlow<String?>(null)
//    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedVehicle = MutableStateFlow<String>("Select Your Vehicle")
    val selectedVehicle = _selectedVehicle.asStateFlow()
    var bookingId = mutableStateOf<Int?>(null)




//    private val _selectedTime = MutableStateFlow<String?>(null)
//    val selectedTime = _selectedTime.asStateFlow()
private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow<LocalTime?>(null)
    val selectedTime = _selectedTime.asStateFlow()

    fun setSelectedDate(date: LocalDate?) {
        _selectedDate.value = date
    }

    fun setSelectedTime(time: LocalTime?) {
        _selectedTime.value = time
    }


    private val _dateTime = MutableStateFlow<String?>(null)
    val dateTime = _dateTime.asStateFlow()

    private val _bookingStartState = MutableStateFlow<Resource<BookingStartResponse>?>(null)
    val bookingStartState = _bookingStartState.asStateFlow()

    val cancelMessage = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()

    // ✅ Setters from UI
    fun setSelectedVehicle(vehicleName: String) {
        _selectedVehicle.value = vehicleName
    }
    fun setServiceType(type: String) { serviceType.value = type }
    fun setFeatureServiceId(id: String?) { featureServiceId.value = id }
    fun setAmount(amountValue: String?) { amount.value = amountValue }
    fun setAddressId(id: String?) {  addressId.value = id
        // ✅ Persist across recompositions & navigation
        Log.d("BookingDebug", "setAddressId() called with id = $id")}
    fun setVehicleId(id: String?) { vehicleId.value = id }
    fun setWaterAvailable(available: Boolean) { waterAvailable.value = available }
    fun setElectricityAvailable(available: Boolean) { electricityAvailable.value = available }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setDateTime(date: LocalDate?, time: LocalTime?) {
        if (date == null || time == null) return

        // Save individually
        _selectedDate.value = date
        _selectedTime.value = time

        // Create final formatted string for API
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = LocalDateTime.of(date, time).format(formatter)

        _dateTime.value = formatted

        Log.d("BookingDebug", "📅 Saved date = $date, time = $time")
        Log.d("BookingDebug", "⏳ API formatted datetime = $formatted")
    }

    fun markBookingAsSendingRequest(bookingId: Int?) {
        viewModelScope.launch {
            val idToSave = bookingId ?: -1
            userPreferencesManager.savePendingBooking(
                pendingBookingId = idToSave,
                route = Routes.BOOKING_SCREEN,
                routeArgsJson = """{"isRequestSending":true}""",
                sheetType = "SET_PRICE"
            )
            Log.d("BookingVM", "💾 Marked booking as SENDING_REQUEST (id=$idToSave)")
        }
    }

    fun clearPendingBooking() {
        viewModelScope.launch {
            userPreferencesManager.clearPendingBooking()
            Log.d("BookingVM", "🧹 Cleared pending booking state")
        }
    }


    fun savePendingBookingToPrefs(
        pendingBookingId: Int,
        route: String,
        routeArgsJson: String? = null,
        sheetType: String = "BOOKING"
    ) {
        try {
            userPreferencesManager.savePendingBooking(
                pendingBookingId,
                route,
                routeArgsJson,
                sheetType
            )
            Log.d("BookingVM", "💾 Saved pending booking with sheet=$sheetType")
        } catch (e: Exception) {
            Log.e("BookingVM", "❌ Failed to save pending booking", e)
        }
    }

    fun startBooking() {
        Log.d("BookingDebug", "🔹 addressId before API = ${addressId.value}")
        Log.d("BookingVM", "🎯 startBooking() called - User clicked 'Find Vendors'")
        
        val addressIdInt = addressId.value?.toIntOrNull()
        Log.d("BookingVM", "📍 Address ID (String): ${addressId.value}, Address ID (Int): $addressIdInt")
        
        if (addressIdInt == null && addressId.value != null) {
            Log.e("BookingVM", "❌ ERROR: addressId '${addressId.value}' could not be converted to Int!")
        }
        
        val request = BookingStartRequest(
            serviceType = serviceType.value,
            featureServiceId = featureServiceId.value?.toIntOrNull(),
            dateTime = _dateTime.value, // ✅ Send final format yyyy-MM-dd HH:mm:ss
            amount = amount.value?.toIntOrNull(),
            userAddressId = addressIdInt,
            vehicleId = vehicleId.value?.toInt(),
            waterAvailable50m = if (waterAvailable.value) 1 else 0,
            electricityAvailable50m = if (electricityAvailable.value) 1 else 0
        )

        Log.d("BookingVM", "📦 Final Request → $request")
        Log.d("BookingVM", "📦 Request Details - serviceType: ${request.serviceType}, userAddressId: ${request.userAddressId}, vehicleId: ${request.vehicleId}, water: ${request.waterAvailable50m}, electricity: ${request.electricityAvailable50m}")
        Log.d("BookingVM", "📡 About to call startBooking API - Backend will send FCM to vendors after receiving this")

        viewModelScope.launch {
            val result = bookingRepository.startBooking(request)
            _bookingStartState.value = result

            when (result) {
                is Resource.Success -> {
                    val bookingIdValue = result.data?.data?.bookingId ?: 0
                    bookingId.value = bookingIdValue  // ✅ now .value refers to the ViewModel property

                    Log.d("BookingVM", "✅ Booking request sent successfully! id=$bookingId")

                    // Update pending booking with real ID
                    userPreferencesManager.savePendingBooking(
                        pendingBookingId = bookingIdValue,
                        route = Routes.BOOKING_SCREEN,
                        routeArgsJson = """{"isRequestSending":true}""",
                        sheetType = "SET_PRICE"
                    )
                }

                is Resource.Error -> {
                    val cleanMessage = try {
                        val raw = result.message ?: "Booking failed"
                        // if it's a JSON, extract the 'message' key
                        if (raw.trim().startsWith("{")) {
                            val json = JSONObject(raw)
                            json.optString("message", raw)
                        } else {
                            raw
                        }
                    } catch (e: Exception) {
                        "Booking failed"
                    }

                    Log.e("BookingVM", "❌ Failed to send booking request: $cleanMessage")

                }


                is Resource.Loading -> {
                    Log.d("BookingVM", "⏳ Sending booking request...")
                }
            }

        }
    }

    fun fetchActiveBookingId(
        onResult: (Int?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = userPreferencesManager.getUserId()
                val token = userPreferencesManager.getJwtToken()
                
                if (userId == null || token == null) {
                    Log.w("PostBookingVM", "⚠️ Missing userId or token - cannot fetch active booking")
                    onResult(null)
                    return@launch
                }
                
                Log.d("PostBookingVM", "🔄 Fetching active booking ID...")
                when (val result = homepageServiceRepository.getActiveBooking(userId, token)) {
                    is Resource.Success -> {
                        val activeBookingId = result.data?.data?.booking?.id
                        if (activeBookingId != null && activeBookingId > 0) {
                            Log.d("PostBookingVM", "✅ Found active booking ID: $activeBookingId")
                            bookingId.value = activeBookingId
                            onResult(activeBookingId)
                        } else {
                            Log.d("PostBookingVM", "ℹ️ No active booking found")
                            onResult(null)
                        }
                    }
                    is Resource.Error -> {
                        Log.e("PostBookingVM", "❌ Failed to fetch active booking: ${result.message}")
                        onResult(null)
                    }
                    is Resource.Loading -> {
                        // Still loading, will be handled by the result
                    }
                }
            } catch (e: Exception) {
                Log.e("PostBookingVM", "❌ Exception while fetching active booking: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun cancelBooking(
        bookingId: Int,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                Log.d("PostBookingVM", "🔄 Canceling booking - bookingId: $bookingId")
                
                // For canceling before vendor accepts, we don't need a reason
                // (cancel_reason_id and cancel_reason_text are optional)
                val request = CancelBookingRequest(
                    booking_id = bookingId,
                    cancel_reason_id = null,
                    cancel_reason_text = null
                )

                val result = domainBookingRepository.cancelBooking(request)
                result.fold(
                    onSuccess = {
                        Log.d("PostBookingVM", "✅ Booking cancelled successfully")
                        cancelMessage.value = "Booking cancelled successfully"
                        clearPendingBooking()
                        onResult(true)
                    },
                    onFailure = { exception ->
                        val errorMessage = exception.message ?: "Failed to cancel booking"
                        Log.e("PostBookingVM", "❌ Failed to cancel booking: $errorMessage")
                        cancelMessage.value = errorMessage
                        onResult(false)
                    }
                )
            } catch (e: Exception) {
                val errorMessage = "An unexpected error occurred: ${e.message}"
                Log.e("PostBookingVM", "❌ Exception while canceling booking: ${e.message}", e)
                cancelMessage.value = errorMessage
                onResult(false)
            } finally {
                isLoading.value = false
            }
        }
    }
}
