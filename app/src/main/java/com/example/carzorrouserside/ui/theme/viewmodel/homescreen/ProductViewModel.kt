package com.example.carzorrouserside.ui.theme.viewmodel.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.ProductUiState
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
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _homeProductsState = MutableStateFlow(ProductUiState())
    val homeProductsState: StateFlow<ProductUiState> = _homeProductsState.asStateFlow()

    init {
        // Load home products (8 items) on initialization
        loadHomeProducts()
    }

    // Load products for home screen (limited to 8 items)
    fun loadHomeProducts() {
        viewModelScope.launch {
            productRepository.getProducts(
                page = 1,
                perPage = 8,
                searchQuery = null
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _homeProductsState.update {
                            it.copy(isLoading = true, error = null)
                        }
                    }
                    is Resource.Success -> {
                        resource.data?.let { response ->
                            _homeProductsState.update {
                                it.copy(
                                    isLoading = false,
                                    products = response.data.products,
                                    error = null,
                                    currentPage = response.data.currentPage,
                                    totalPages = response.data.lastPage,
                                    hasNextPage = response.data.nextPageUrl != null
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _homeProductsState.update {
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

    // Load all products with pagination and search
    fun loadProducts(
        page: Int = 1,
        searchQuery: String = "",
        append: Boolean = false
    ) {
        viewModelScope.launch {
            productRepository.getProducts(
                page = page,
                perPage = 20,
                searchQuery = searchQuery.takeIf { it.isNotBlank() }
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                error = null,
                                searchQuery = searchQuery
                            )
                        }
                    }
                    is Resource.Success -> {
                        resource.data?.let { response ->
                            val newProducts = if (append && page > 1) {
                                _uiState.value.products + response.data.products
                            } else {
                                response.data.products
                            }

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    products = newProducts,
                                    error = null,
                                    currentPage = response.data.currentPage,
                                    totalPages = response.data.lastPage,
                                    hasNextPage = response.data.nextPageUrl != null,
                                    searchQuery = searchQuery
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Unknown error occurred",
                                searchQuery = searchQuery
                            )
                        }
                    }
                }
            }
        }
    }

    // Load next page for pagination
    fun loadNextPage() {
        val currentState = _uiState.value
        if (!currentState.isLoading && currentState.hasNextPage) {
            loadProducts(
                page = currentState.currentPage + 1,
                searchQuery = currentState.searchQuery,
                append = true
            )
        }
    }

    // Search products
    fun searchProducts(query: String) {
        loadProducts(page = 1, searchQuery = query, append = false)
    }

    // Retry loading products
    fun retryLoadProducts() {
        val currentState = _uiState.value
        loadProducts(
            page = 1,
            searchQuery = currentState.searchQuery,
            append = false
        )
    }

    // Retry loading home products
    fun retryLoadHomeProducts() {
        loadHomeProducts()
    }

    // Clear search and reload
    fun clearSearch() {
        loadProducts(page = 1, searchQuery = "", append = false)
    }

    // Refresh products (pull to refresh)
    fun refreshProducts() {
        val currentState = _uiState.value
        loadProducts(
            page = 1,
            searchQuery = currentState.searchQuery,
            append = false
        )
    }

    // Refresh home products
    fun refreshHomeProducts() {
        loadHomeProducts()
    }
}