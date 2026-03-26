package com.example.carzorrouserside.ui.theme.viewmodel.homescreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.Brand
import com.example.carzorrouserside.data.model.homescreen.BrandSelectionUiState
import com.example.carzorrouserside.data.model.homescreen.CarModel
import com.example.carzorrouserside.data.model.homescreen.ModelSelectionUiState
import com.example.carzorrouserside.data.repository.homescreen.BrandModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrandSelectionViewModel @Inject constructor(
    private val repository: BrandModelRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<BrandSelectionUiState>()
    val uiState: LiveData<BrandSelectionUiState> = _uiState

    private var searchJob: Job? = null

    init {
        _uiState.value = BrandSelectionUiState()
        loadBrands()
    }

    fun loadBrands() {
        _uiState.value = _uiState.value?.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = repository.getBrands()

            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        brands = response.data,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load brands"
                    )
                }
            )
        }
    }

    fun searchBrands(query: String) {
        // Cancel previous search job
        searchJob?.cancel()

        if (query.isBlank()) {
            // If search query is empty, load all brands
            loadBrands()
            return
        }

        searchJob = viewModelScope.launch {
            // Add small delay to avoid too many API calls while typing
            delay(300)

            _uiState.value = _uiState.value?.copy(isLoading = true, error = null)

            val result = repository.searchBrands(query)

            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        brands = response.data,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to search brands"
                    )
                }
            )
        }
    }

    fun retry() {
        loadBrands()
    }
}
