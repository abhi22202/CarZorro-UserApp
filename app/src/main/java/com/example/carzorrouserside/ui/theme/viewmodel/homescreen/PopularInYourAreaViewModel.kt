package com.example.carzorrouserside.ui.theme.viewmodel.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.PopularProviderItem
import com.example.carzorrouserside.data.repository.homescreen.PopularAreaRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class PopularAreaUiState(
    val isLoading: Boolean = false,
    val providers: List<PopularProviderItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PopularAreaViewModel @Inject constructor(
    private val repository: PopularAreaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PopularAreaUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchPopularProviders()
    }

    fun fetchPopularProviders() {
        repository.getPopularInArea().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = PopularAreaUiState(isLoading = true)
                }
                is Resource.Success -> {
                    _uiState.value = PopularAreaUiState(providers = result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _uiState.value = PopularAreaUiState(error = result.message ?: "An unknown error occurred")
                }
            }
        }.launchIn(viewModelScope)
    }
}