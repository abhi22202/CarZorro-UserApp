package com.example.carzorrouserside.ui.viewmodel.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.booking.OrderDetail
import com.example.carzorrouserside.data.repository.BookingRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OrderDetailUiState(
    val isLoading: Boolean = false,
    val orderDetail: OrderDetail? = null,
    val error: String? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val repository: BookingRepository,
    savedStateHandle: SavedStateHandle // Injected to get navigation arguments
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Retrieve the orderId passed during navigation
        val orderId: Int? = savedStateHandle["orderId"]
        if (orderId != null) {
            fetchOrderDetails(orderId)
        } else {
            _uiState.update { it.copy(error = "Order ID is missing.") }
        }
    }

    private fun fetchOrderDetails(orderId: Int) {
        repository.getOrderDetails(orderId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, orderDetail = result.data?.data)
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