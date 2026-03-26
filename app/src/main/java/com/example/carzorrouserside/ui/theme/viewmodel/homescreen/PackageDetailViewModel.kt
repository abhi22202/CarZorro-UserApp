package com.example.carzorrouserside.ui.theme.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.PackageDetailData
import com.example.carzorrouserside.data.repository.homescreen.PackageRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch // Make sure this is imported
import javax.inject.Inject

data class PackageDetailUiState(
    val isLoading: Boolean = true,
    val packageDetail: PackageDetailData? = null,
    val error: String? = null
)

@HiltViewModel
class PackageDetailViewModel @Inject constructor(
    private val repository: PackageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackageDetailUiState())
    val uiState: StateFlow<PackageDetailUiState> = _uiState.asStateFlow()

    private val packageId: Int = checkNotNull(savedStateHandle["packageId"])

    init {
        fetchPackageDetails()
    }

    fun fetchPackageDetails() {
        // Wrap the repository call in viewModelScope.launch
        viewModelScope.launch {
            repository.getPackageDetails(packageId).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, packageDetail = result.data)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}