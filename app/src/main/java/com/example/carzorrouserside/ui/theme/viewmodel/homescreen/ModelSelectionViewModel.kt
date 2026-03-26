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
class ModelSelectionViewModel @Inject constructor(
    private val repository: BrandModelRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<ModelSelectionUiState>()
    val uiState: LiveData<ModelSelectionUiState> = _uiState

    private var searchJob: Job? = null
    private var currentBrandId: Int = 0

    init {
        _uiState.value = ModelSelectionUiState()
    }

    fun loadModels(brandId: Int, brandName: String) {
        currentBrandId = brandId
        _uiState.value = _uiState.value?.copy(
            isLoading = true,
            error = null,
            selectedBrandName = brandName
        )

        viewModelScope.launch {
            val result = repository.getModels(brandId)

            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        models = response.data,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load models"
                    )
                }
            )
        }
    }

    fun searchModels(query: String) {
        // Cancel previous search job
        searchJob?.cancel()

        if (query.isBlank()) {
            // If search query is empty, load all models for current brand
            loadModels(currentBrandId, _uiState.value?.selectedBrandName ?: "")
            return
        }

        searchJob = viewModelScope.launch {
            // Add small delay to avoid too many API calls while typing
            delay(300)

            _uiState.value = _uiState.value?.copy(isLoading = true, error = null)

            val result = repository.searchModels(currentBrandId, query)

            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        models = response.data,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to search models"
                    )
                }
            )
        }
    }

    fun retry(brandId: Int, brandName: String) {
        loadModels(brandId, brandName)
    }
}