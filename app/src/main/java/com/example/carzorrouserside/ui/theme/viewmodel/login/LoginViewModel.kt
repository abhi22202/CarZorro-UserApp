package com.example.carzorrouserside.ui.theme.viewmodel.login


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.loginscreen.UserLoginUiState
import com.example.carzorrouserside.data.repository.login.UserAuthRepository
import com.example.carzorrouserside.util.Resource
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.internal.Contexts.getApplication
import android.provider.Settings


import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserLoginViewModel @Inject constructor(
    private val userAuthRepository: UserAuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "UserLoginViewModel"
    }

    private val _uiState = MutableStateFlow(UserLoginUiState())
    val uiState: StateFlow<UserLoginUiState> = _uiState.asStateFlow()

    fun sendOtp(phoneNumber: String) {
        Log.d(TAG, "🚀 sendOtp called with phone: $phoneNumber")

        viewModelScope.launch {
            Log.d(TAG, "📝 Setting loading state to true")
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                otpSent = false // Reset OTP sent flag
            )

            // Validation directly in ViewModel
            when {
                phoneNumber.isBlank() -> {
                    Log.e(TAG, "❌ Validation failed: Phone number is blank")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Phone number cannot be empty"
                    )
                    return@launch
                }
                phoneNumber.length != 10 -> {
                    Log.e(TAG, "❌ Validation failed: Phone number length is ${phoneNumber.length}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Phone number must be 10 digits"
                    )
                    return@launch
                }
                !phoneNumber.all { it.isDigit() } -> {
                    Log.e(TAG, "❌ Validation failed: Phone number contains non-digits")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Phone number must contain only digits"
                    )
                    return@launch
                }
            }

            Log.d(TAG, "✅ Validation passed, calling repository...")

            // Call repository directly
            when (val result = userAuthRepository.sendOtp(phoneNumber)) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ Repository returned success!")
                    Log.d(TAG, "📋 Success data: ${result.data}")

                    val serverOtp = result.data?.data?.otp?.toString()
                    Log.d(TAG, "🔢 Extracted server OTP: $serverOtp")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otpSent = true,
                        message = result.data?.message,
                        serverOtp = serverOtp,
                        error = null
                    )

                    Log.d(TAG, "📝 Updated UI state - otpSent: true, message: ${result.data?.message}")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Repository returned error: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                        otpSent = false
                    )
                }
                is Resource.Loading -> {
                    Log.d(TAG, "⏳ Repository returned loading")
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

    fun updateFcmToken(userId: Int) {
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                val deviceId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )

                Log.d(TAG, "📱 Device ID: $deviceId, 🔑 FCM Token: $fcmToken")

                val result = userAuthRepository.updateFcmToken(
                    userId = userId,
                    fcmToken = fcmToken,
                    deviceId = deviceId
                )

                when (result) {
                    is Resource.Success -> Log.d(TAG, "✅ Token updated successfully: ${result.data}")
                    is Resource.Error -> Log.e(TAG, "❌ Failed to update token: ${result.message}")
                    is Resource.Loading -> Log.d(TAG, "⏳ Updating token...")
                }

            } catch (e: Exception) {
                Log.e(TAG, "🔥 Error fetching token: ${e.message}")
            }
        }
    }

    fun clearError() {
        Log.d(TAG, "🧹 Clearing error state")
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearOtpSentState() {
        Log.d(TAG, "🧹 Clearing OTP sent state")
        _uiState.value = _uiState.value.copy(otpSent = false)
    }

    fun clearStates() {
        Log.d(TAG, "🧹 Clearing all states")
        _uiState.value = UserLoginUiState()
    }
}