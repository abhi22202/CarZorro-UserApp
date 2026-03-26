package com.example.carzorrouserside.ui.theme.viewmodel.login


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.loginscreen.LogoutUiState
import com.example.carzorrouserside.data.repository.login.LogoutRepository

import com.example.carzorrouserside.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val logoutRepository: LogoutRepository
) : ViewModel() {

    companion object {
        private const val TAG = "LogoutViewModel"
    }

    private val _logoutState = MutableStateFlow<LogoutUiState>(LogoutUiState.Idle)
    val logoutState: StateFlow<LogoutUiState> = _logoutState.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    init {
        checkUserLoginStatus()
    }

    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "🚪 Logout initiated by user")
            _logoutState.value = LogoutUiState.Loading

            when (val result = logoutRepository.logout()) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "✅ Logout successful: ${result.data.message}")
                    _logoutState.value = LogoutUiState.Success
                    _isUserLoggedIn.value = false
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "❌ Logout failed: ${result.message}")
                    // Even on error, consider it successful for UX (user should be logged out locally)
                    _logoutState.value = LogoutUiState.Success
                    _isUserLoggedIn.value = false
                }
                is NetworkResult.Loading -> {
                    _logoutState.value = LogoutUiState.Loading
                }
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = LogoutUiState.Idle
    }

    private fun checkUserLoginStatus() {
        _isUserLoggedIn.value = logoutRepository.isUserLoggedIn()
        Log.d(TAG, "👤 User login status: ${_isUserLoggedIn.value}")
    }

    fun refreshUserLoginStatus() {
        checkUserLoginStatus()
    }
}