package com.example.carzorrouserside.ui.theme.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.profile.EditProfileRequest
import com.example.carzorrouserside.data.model.profile.UserBasicDetails
import com.example.carzorrouserside.data.repository.profile.ProfileRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userBasicDetails: UserBasicDetails? = null,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val updateError: String? = null,
    val isUploadingImage: Boolean = false,
    val imageUploadSuccess: Boolean = false,
    val imageUploadError: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchUserBasicDetails()
    }

    fun fetchUserBasicDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = repository.getUserBasicDetails()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userBasicDetails = result.data,
                        error = null
                    )
                }
                is Resource.Error -> {
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

    fun refreshUserBasicDetails() {
        fetchUserBasicDetails()
    }

    fun editProfile(
        fullName: String?,
        email: String?,
        dob: String?,
        phone: String?,
        altPhone: String?,
        gender: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdating = true,
                updateError = null,
                updateSuccess = false
            )

            val request = EditProfileRequest(
                fullName = fullName?.takeIf { it.isNotBlank() },
                email = email?.takeIf { it.isNotBlank() },
                dob = dob?.takeIf { it.isNotBlank() },
                phone = phone?.takeIf { it.isNotBlank() },
                // Send alt_phone if it has a value (trimmed and not empty)
                altPhone = altPhone?.trim()?.takeIf { it.isNotEmpty() },
                gender = gender?.takeIf { it.isNotBlank() }
            )
            
            android.util.Log.d("ProfileViewModel", "EditProfileRequest - altPhone: ${request.altPhone}")

            when (val result = repository.editProfile(request)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateSuccess = true,
                        updateError = null
                    )
                    // Wait a bit for the API to process the update, then refresh user details
                    kotlinx.coroutines.delay(500)
                    fetchUserBasicDetails()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateError = result.message,
                        updateSuccess = false
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isUpdating = true)
                }
            }
        }
    }

    fun resetUpdateState() {
        _uiState.value = _uiState.value.copy(
            updateSuccess = false,
            updateError = null
        )
    }

    fun uploadProfileImage(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploadingImage = true,
                imageUploadError = null,
                imageUploadSuccess = false
            )

            when (val result = repository.editProfileImage(imageFile)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        imageUploadSuccess = true,
                        imageUploadError = null
                    )
                    // Refresh user details after successful image upload
                    fetchUserBasicDetails()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        imageUploadError = result.message,
                        imageUploadSuccess = false
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isUploadingImage = true)
                }
            }
        }
    }

    fun resetImageUploadState() {
        _uiState.value = _uiState.value.copy(
            imageUploadSuccess = false,
            imageUploadError = null
        )
    }
}

