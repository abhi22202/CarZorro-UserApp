package com.example.carzorrouserside.ui.theme.viewmodel.login


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.loginscreen.OtpVerificationUiState
import com.example.carzorrouserside.data.repository.login.UserAuthRepository
import com.example.carzorrouserside.ui.theme.viewmodel.login.DeviceUtils.DeviceUtils

import com.example.carzorrouserside.util.NetworkResult
import com.example.carzorrouserside.util.Resource
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OtpVerificationViewModel @Inject constructor(
    private val authRepository: UserAuthRepository,
    application: Application
) : AndroidViewModel(application) {


    companion object {
        private const val TAG = "OtpVerificationViewModel"
    }

    private val _uiState = MutableStateFlow(OtpVerificationUiState())
    val uiState: StateFlow<OtpVerificationUiState> = _uiState.asStateFlow()
    fun resendOtp(phoneNumber: String) {
        viewModelScope.launch {
            Log.d(TAG, "🔄 Resending OTP for phone: $phoneNumber")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val cleanPhone = phoneNumber.replace("+91", "").replace(" ", "").trim()
            Log.d(TAG, "📱 Cleaned phone number: $cleanPhone")

            when (val result = authRepository.sendOtp(cleanPhone)) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ OTP resend successful!")

                    // Extract the new OTP from response
                    val newOtp = result.data?.data?.otp
                    Log.d(TAG, "🔢 New OTP received: $newOtp")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resendSuccess = true, // Changed from otpResent to resendSuccess
                        message = result.data?.message,
                        newOtp = newOtp?.toString() // Store the new OTP in UI state
                    )
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ OTP resend failed: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to resend OTP"
                    )
                }
                is Resource.Loading -> {
                    Log.d(TAG, "⏳ OTP resend loading")
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    fun clearResendSuccess() {
        Log.d(TAG, "🧹 Clearing resend success state")
        _uiState.value = _uiState.value.copy(resendSuccess = false)
    }
    fun verifyOtp(phone: String, otp: String) {
        Log.d(TAG, "🔐 verifyOtp called with phone: $phone, OTP: $otp")

        viewModelScope.launch {
            Log.d(TAG, "📝 Setting loading state to true")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.verifyOtp(phone, otp)) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "✅ OTP verification successful!")
                    Log.d(TAG, "📋 Success data: ${result.data}")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isVerificationSuccessful = true,
                        token = result.data.data?.token,
                        message = result.data.message
                    )

                    Log.d(TAG, "🎟️ Token received: ${result.data.data?.token}")
                    registerDeviceToken()
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "❌ OTP verification failed: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "⏳ OTP verification loading")
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    // --- Add this function ---
    private fun registerDeviceToken() {
        // Get the userId saved by the repository during verifyOtp
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            Log.e(TAG, "Cannot register FCM token, user ID not found after verifyOtp.")
            // Maybe queue this action or show a non-critical error?
            return
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "❌ Failed to get FCM token", task.exception)
                return@addOnCompleteListener
            }

            val fcmToken = task.result
            Log.d(TAG, "✅ FCM Token received: $fcmToken")

            // Step 2️⃣ Get device ID (you can implement this utility)
            val deviceId = DeviceUtils.getDeviceId(getApplication())
            Log.d(TAG, "📱 Device ID: $deviceId")

            // Step 3️⃣ Call repository to send token to backend
            viewModelScope.launch {
                when (val result = authRepository.updateFcmToken(
                    userId = currentUserId,
                    fcmToken = fcmToken,
                    deviceId = deviceId
                )) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ FCM token updated successfully on backend")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Failed to update FCM token: ${result.message}")
                    }
                    is Resource.Loading -> {
                        Log.d(TAG, "⏳ Sending FCM token to backend...")
                    }
                }
            }
    }
    // --- End of added function ---

    fun clearError() {
        Log.d(TAG, "🧹 Clearing error state")
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearStates() {
        Log.d(TAG, "🧹 Clearing all states")
        _uiState.value = OtpVerificationUiState()
    }
}}