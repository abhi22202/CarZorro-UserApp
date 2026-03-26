package com.example.carzorrouserside.ui.theme.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.profile.WalletTransaction
import com.example.carzorrouserside.data.repository.profile.ProfileRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = false,
    val transactions: List<WalletTransaction> = emptyList(),
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val total: Int = 0,
    val error: String? = null,
    val isPaginating: Boolean = false,
    val hasNextPage: Boolean = false
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        fetchWalletHistory()
    }

    fun fetchWalletHistory(page: Int = 1, append: Boolean = false) {
        viewModelScope.launch {
            if (append && page > 1) {
                _uiState.value = _uiState.value.copy(isPaginating = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            when (val result = repository.getWalletHistory(page)) {
                is Resource.Success -> {
                    val paginationData = result.data
                    val newTransactions = paginationData?.transactions ?: emptyList()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        transactions = if (append) _uiState.value.transactions + newTransactions else newTransactions,
                        currentPage = paginationData?.currentPage ?: 1,
                        lastPage = paginationData?.lastPage ?: 1,
                        total = paginationData?.total ?: 0,
                        hasNextPage = (paginationData?.currentPage ?: 1) < (paginationData?.lastPage ?: 1),
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (!currentState.isPaginating && currentState.hasNextPage && !currentState.isLoading) {
            fetchWalletHistory(page = currentState.currentPage + 1, append = true)
        }
    }

    fun refresh() {
        fetchWalletHistory(page = 1, append = false)
    }
}

