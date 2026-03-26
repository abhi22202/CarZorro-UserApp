package com.example.carzorrouserside.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.vendor.VendorData
import com.example.carzorrouserside.data.model.vendor.VendorPackage
import com.example.carzorrouserside.data.repository.vendor.VendorDetailsRepository
import com.example.carzorrouserside.data.token.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- UI STATE ---
data class VendorDetailsUiState(
    val isLoading: Boolean = false,
    val isFavoriting: Boolean = false, // For the favorite button's loading state
    val vendorData: VendorData? = null,
    val packages: List<VendorPackage> = emptyList(),
    val error: String? = null,
    val favoriteUpdateMessage: String? = null // For showing a Snackbar/Toast
)

@HiltViewModel
class VendorDetailsViewModel @Inject constructor(
    private val repository: VendorDetailsRepository,
    private val userPreferencesManager: UserPreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(VendorDetailsUiState())
    val uiState: StateFlow<VendorDetailsUiState> = _uiState.asStateFlow()

    private val vendorId: Int = checkNotNull(savedStateHandle["vendorId"])

    init {
        fetchVendorData()
    }

    fun fetchVendorData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val detailsFlow = repository.getVendorDetails(vendorId)
        val packagesFlow = repository.getVendorPackages(vendorId)

        detailsFlow.zip(packagesFlow) { detailsResult, packagesResult ->
            Pair(detailsResult, packagesResult)
        }.onEach { (detailsResult, packagesResult) ->
            val vendorDetails = detailsResult.getOrNull()
            val vendorPackages = packagesResult.getOrNull()

            val detailsError = detailsResult.exceptionOrNull()
            val packagesError = packagesResult.exceptionOrNull()

            val errorMessage = listOfNotNull(detailsError, packagesError)
                .joinToString(separator = "\n") { it.message ?: "An unexpected error occurred" }
                .ifEmpty { null }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    vendorData = vendorDetails,
                    packages = vendorPackages ?: emptyList(),
                    error = errorMessage
                )
            }
        }.launchIn(viewModelScope)
    }

    fun toggleFavoriteStatus() {
        viewModelScope.launch {
            // --- FIXED ERROR HERE ---
            // Changed getAuthToken().firstOrNull() to the correct function from your file: getJwtToken()
            val token = userPreferencesManager.getJwtToken()

            if (token.isNullOrEmpty()) {
                _uiState.update { it.copy(favoriteUpdateMessage = "You must be logged in to favorite.") }
                return@launch
            }

            _uiState.update { it.copy(isFavoriting = true, favoriteUpdateMessage = null) }

            repository.toggleFavoriteVendor(token, vendorId).collect { result ->
                _uiState.value.vendorData?.let { currentVendor ->
                    result.onSuccess { response ->
                        // Optimistically toggle the favorite state in the UI on success
                        val updatedVendor = currentVendor.copy(isFavourite = !(currentVendor.isFavourite ?: false))
                        _uiState.update {
                            it.copy(
                                isFavoriting = false,
                                vendorData = updatedVendor,
                                favoriteUpdateMessage = response.message
                            )
                        }
                    }
                    result.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isFavoriting = false,
                                favoriteUpdateMessage = error.message ?: "An unknown error occurred."
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearFavoriteMessage() {
        _uiState.update { it.copy(favoriteUpdateMessage = null) }
    }
}