package com.example.carzorrouserside.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.ApiPackage
import com.example.carzorrouserside.data.repository.homescreen.PackageRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PackageType {
    POPULAR, ALL
}

data class PackageUiState(
    val isLoading: Boolean = false,
    val packages: List<ApiPackage> = emptyList(),
    val error: String? = null,
    val endReached: Boolean = false,
    val isPaginating: Boolean = false,
    val isSearching: Boolean = false
)

@HiltViewModel
class PackageViewModel @Inject constructor(
    private val repository: PackageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackageUiState())
    val uiState: StateFlow<PackageUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var currentPackageType: PackageType = PackageType.POPULAR

    private var searchJob: Job? = null

    init {
        loadPackages(currentPackageType, isRefresh = true)
    }

    fun loadPackages(type: PackageType, isRefresh: Boolean = false) {
        if ((_uiState.value.isLoading || _uiState.value.isPaginating) && !isRefresh) return

        if (isRefresh) {
            currentPage = 1
            currentPackageType = type
            _uiState.update { it.copy(packages = emptyList(), endReached = false, error = null) }
        }

        if (type != currentPackageType && !isRefresh) return

        viewModelScope.launch {
            val flow = when (type) {
                PackageType.POPULAR -> repository.getPopularPackages(perPage = 10, page = currentPage)
                PackageType.ALL -> repository.getAllPackages(perPage = 10, page = currentPage)
            }

            flow.onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update {
                            if (currentPage == 1) it.copy(isLoading = true, error = null, isSearching = false)
                            else it.copy(isPaginating = true, error = null, isSearching = false)
                        }
                    }
                    is Resource.Success -> {
                        val newPackages = result.data?.packages ?: emptyList()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isPaginating = false,
                                packages = if (isRefresh) newPackages else it.packages + newPackages,
                                endReached = newPackages.isEmpty() || result.data?.currentPage == result.data?.lastPage
                            )
                        }
                        if (!uiState.value.endReached && newPackages.isNotEmpty()) {
                            currentPage++
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isPaginating = false,
                                error = result.message
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            if (query.isBlank()) {
                loadPackages(currentPackageType, isRefresh = true)
            } else {
                executeSearch(query)
            }
        }
    }

    private fun executeSearch(query: String) {
        viewModelScope.launch {
            repository.searchPackages(query).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true, error = null, isSearching = true, packages = emptyList())
                        }
                    }
                    is Resource.Success -> {
                        val searchResults = result.data?.packages ?: emptyList()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                packages = searchResults,
                                endReached = true,
                                isPaginating = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                                packages = emptyList()
                            )
                        }
                    }
                }
            }
        }
    }
}