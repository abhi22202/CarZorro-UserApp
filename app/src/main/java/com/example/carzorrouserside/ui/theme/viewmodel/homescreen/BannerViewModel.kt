package com.example.carzorrouserside.ui.theme.viewmodel.homescreen


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.Banner
import com.example.carzorrouserside.data.model.homescreen.BannerUiState
import com.example.carzorrouserside.data.repository.homescreen.BannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BannerViewModel @Inject constructor(
    private val bannerRepository: BannerRepository
) : ViewModel() {

    companion object {
        private const val TAG = "BannerViewModel"
    }

    private val _uiState = MutableStateFlow(BannerUiState())
    val uiState: StateFlow<BannerUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "🚀 BannerViewModel initialized")
        loadBanners()
    }

    /**
     * Load banners from repository
     */
    fun loadBanners() {
        Log.d(TAG, "🔄 Loading banners...")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            bannerRepository.getHomepageBanners()
                .catch { exception ->
                    Log.e(TAG, "❌ Error in banner flow: ${exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { banners ->
                            Log.d(TAG, "✅ Banners loaded successfully: ${banners.size} items")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                banners = banners,
                                error = null
                            )
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "❌ Failed to load banners: ${exception.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load banners"
                            )
                        }
                    )
                }
        }
    }

    /**
     * Retry loading banners
     */
    fun retryLoadBanners() {
        Log.d(TAG, "🔄 Retrying to load banners...")
        loadBanners()
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     *
     * Get banner by ID
     */
    fun getBannerById(id: Int): Banner? {
        return _uiState.value.banners.find { it.id == id }
    }

    /**
     * Check if location permissions are available
     */
    fun hasLocationPermissions(): Boolean {
        return bannerRepository.hasLocationPermissions()
    }
}