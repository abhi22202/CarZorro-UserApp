package com.example.carzorrouserside.ui.theme.screens.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.vendor.FavouriteVendorDto
import com.example.carzorrouserside.data.repository.vendor.FavouriteVendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed interface to represent the different UI states
sealed interface FavouriteUiState {
    data object Loading : FavouriteUiState
    data class Success(val vendors: List<FavouriteVendorDto>) : FavouriteUiState
    data class Error(val message: String) : FavouriteUiState
}

@HiltViewModel
class FavouriteVendorViewModel @Inject constructor(
    private val repository: FavouriteVendorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavouriteUiState>(FavouriteUiState.Loading)
    val uiState: StateFlow<FavouriteUiState> = _uiState.asStateFlow()

    init {
        fetchFavouriteVendors()
    }

    fun fetchFavouriteVendors() {
        viewModelScope.launch {
            _uiState.value = FavouriteUiState.Loading
            repository.getFavouriteVendors()
                .onSuccess { vendors ->
                    _uiState.value = FavouriteUiState.Success(vendors)
                }
                .onFailure { error ->
                    _uiState.value = FavouriteUiState.Error(error.message ?: "An unknown error occurred")
                }
        }
    }
}