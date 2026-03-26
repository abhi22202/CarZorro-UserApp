package com.example.carzorrouserside.ui.theme.viewmodel.register


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.register.RegisterRequest
import com.example.carzorrouserside.data.model.register.SignUpOtpUiState
import com.example.carzorrouserside.data.repository.register.UserAuthRegisterRepository

import com.example.carzorrouserside.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SignUpOtpViewModel @Inject constructor(
    private val userRegistrationRepository: UserAuthRegisterRepository
) : ViewModel() {

    private val TAG = "UserRegistrationOtpViewModel"

    private val _uiState = MutableStateFlow(SignUpOtpUiState())
    val uiState: StateFlow<SignUpOtpUiState> = _uiState.asStateFlow()

    /**
     * Verify OTP for user registration completion
     */
    fun verifyRegistrationOtp(phoneNumber: String, otpCode: String, userId: Int) {
        viewModelScope.launch {
            Log.d(TAG, "=== REGISTRATION OTP VERIFICATION STARTED ===")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Phone: $phoneNumber")
            Log.d(TAG, "OTP: $otpCode")
            Log.d(TAG, "================================================")

            // Validate input
            if (otpCode.length != 4 || !otpCode.all { it.isDigit() }) {
                Log.e(TAG, "❌ Invalid OTP format: $otpCode")
                _uiState.value = _uiState.value.copy(
                    verificationErrorMessage = "Please enter a valid 4-digit OTP"
                )
                return@launch
            }

            if (userId <= 0) {
                Log.e(TAG, "❌ Invalid user ID: $userId")
                _uiState.value = _uiState.value.copy(
                    verificationErrorMessage = "Invalid user session. Please try registering again."
                )
                return@launch
            }

            // Start loading state
            _uiState.value = _uiState.value.copy(
                isLoadingVerification = true,
                verificationErrorMessage = null,
                successMessage = null
            )

            try {
                when (val result = userRegistrationRepository.verifyRegistrationOtp(userId, otpCode)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "✅ OTP verification successful")
                        Log.d(TAG, "Response: ${result.data}")

                        _uiState.value = _uiState.value.copy(
                            isLoadingVerification = false,
                            isVerificationSuccessful = true,
                            authenticationToken = result.data.authToken ?: "registration_completed_$userId",
                            successMessage = result.data.message
                        )
                    }

                    is NetworkResult.Error -> {
                        Log.e(TAG, "❌ OTP verification failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoadingVerification = false,
                            verificationErrorMessage = result.message
                        )
                    }

                    is NetworkResult.Loading -> {
                        // Should not happen in this context, but handle gracefully
                        Log.d(TAG, "🔄 Still loading...")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error during verification: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoadingVerification = false,
                    verificationErrorMessage = "An unexpected error occurred. Please try again."
                )
            }
        }
    }

    /**
     * Resend OTP by calling the registration API again
     */
    fun resendRegistrationOtp(originalRegisterRequest: RegisterRequest) {
        viewModelScope.launch {
            Log.d(TAG, "=== REGISTRATION OTP RESEND STARTED ===")
            Log.d(TAG, "Phone: ${originalRegisterRequest.phone}")
            Log.d(TAG, "=====================================")

            // Start loading state
            _uiState.value = _uiState.value.copy(
                isLoadingResend = true,
                resendErrorMessage = null
            )

            try {
                when (val result = userRegistrationRepository.resendRegistrationOtp(originalRegisterRequest)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "✅ OTP resend successful")
                        Log.d(TAG, "New OTP: ${result.data.data?.otp}")

                        _uiState.value = _uiState.value.copy(
                            isLoadingResend = false,
                            isResendSuccessful = true,
                            newOtpFromServer = result.data.data?.otp?.toString() ?: "",
                            successMessage = result.data.message
                        )
                    }

                    is NetworkResult.Error -> {
                        Log.e(TAG, "❌ OTP resend failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoadingResend = false,
                            resendErrorMessage = result.message
                        )
                    }

                    is NetworkResult.Loading -> {
                        // Should not happen in this context, but handle gracefully
                        Log.d(TAG, "🔄 Still loading resend...")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error during resend: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoadingResend = false,
                    resendErrorMessage = "Failed to resend OTP. Please try again."
                )
            }
        }
    }

    /**
     * Clear verification error message
     */
    fun clearVerificationError() {
        _uiState.value = _uiState.value.copy(verificationErrorMessage = null)
    }

    /**
     * Clear resend error message
     */
    fun clearResendError() {
        _uiState.value = _uiState.value.copy(resendErrorMessage = null)
    }

    /**
     * Clear resend success state
     */
    fun clearResendSuccess() {
        _uiState.value = _uiState.value.copy(
            isResendSuccessful = false,
            successMessage = null
        )
    }

    /**
     * Clear all error states
     */
    fun clearAllErrors() {
        _uiState.value = _uiState.value.copy(
            verificationErrorMessage = null,
            resendErrorMessage = null
        )
    }

    /**
     * Reset the complete state (useful when navigating away)
     */
    fun resetState() {
        _uiState.value = SignUpOtpUiState()
    }
}