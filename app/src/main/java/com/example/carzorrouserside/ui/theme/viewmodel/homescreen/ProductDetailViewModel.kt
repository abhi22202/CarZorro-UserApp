package com.example.carzorrouserside.ui.theme.viewmodel.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.ProductDetailUiState
import com.example.carzorrouserside.data.repository.homescreen.ProductRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProductDetail(productId: Int) {
        viewModelScope.launch {
            productRepository.getProductDetail(productId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true, error = null)
                        }
                    }
                    is Resource.Success -> {
                        resource.data?.let { response ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    productDetail = response.data,
                                    error = null
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Unknown error occurred"
                            )
                        }
                    }
                }
            }
        }
    }

    fun retryLoadProductDetail(productId: Int) {
        loadProductDetail(productId)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}