package com.example.carzorrouserside.ui.theme.viewmodel.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.register.RegisterRequest
import com.example.carzorrouserside.data.model.register.RegisterResponse
import com.example.carzorrouserside.data.repository.register.UserAuthRegisterRepository
import com.example.carzorrouserside.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: UserAuthRegisterRepository
) : ViewModel() {

    private val _registrationState = MutableStateFlow<NetworkResult<RegisterResponse>?>(null)
    val registrationState: StateFlow<NetworkResult<RegisterResponse>?> = _registrationState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun registerUser(
        fullName: String,
        email: String,
        birthDate: String,
        phoneNumber: String,
        gender: String = "male" // Default value, can be made dynamic
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _registrationState.value = NetworkResult.Loading()

            val formattedDate = convertDateToApiFormat(birthDate)
            val request = RegisterRequest(
                fullName = fullName.trim(),
                email = email.trim(),
                dob = formattedDate,
                phone = phoneNumber.trim(),
                gender = gender
            )

            val result = repository.initiateUserRegistration(request)
            _registrationState.value = result
            _isLoading.value = false
        }
    }

    private fun convertDateToApiFormat(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            // If parsing fails, return current date or handle error
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }

    fun clearRegistrationState() {
        _registrationState.value = null
    }

    fun validateInputs(
        fullName: String,
        email: String,
        birthDate: String,
        phoneNumber: String
    ): Boolean {
        return fullName.isNotBlank() &&
                email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                birthDate.isNotBlank() &&
                phoneNumber.length == 10 &&
                phoneNumber.all { it.isDigit() }
    }
}
