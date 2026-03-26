package com.example.carzorrouserside.ui.theme.viewmodel.homescreen


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.OrderUiState
import com.example.carzorrouserside.data.model.homescreen.PaymentFlowState
import com.example.carzorrouserside.data.repository.homescreen.OrderRepository

import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val _paymentFlowState = MutableStateFlow<PaymentFlowState?>(null)
    val paymentFlowState: StateFlow<PaymentFlowState?> = _paymentFlowState.asStateFlow()

    // Razorpay configuration - Replace with your actual key
    companion object {
        const val RAZORPAY_KEY = "rzp_test_voGDMqEZY5d9ux"
    }

    fun initiateOrderCreation(productId: Int, quantity: Int) {
        viewModelScope.launch {
            orderRepository.createOrder(productId, quantity).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                isCreatingOrder = true,
                                createOrderError = null,
                                orderData = null
                            )
                        }
                    }
                    is Resource.Success -> {
                        resource.data?.let { response ->
                            _uiState.update {
                                it.copy(
                                    isCreatingOrder = false,
                                    orderData = response.data,
                                    createOrderError = null
                                )
                            }

                            // Set up payment flow state for Razorpay
                            _paymentFlowState.update {
                                PaymentFlowState(
                                    productId = productId,
                                    quantity = quantity,
                                    amount = response.data.amount,
                                    orderId = response.data.orderId,
                                    productOrderId = response.data.productOrderId,
                                    razorpayKey = RAZORPAY_KEY
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isCreatingOrder = false,
                                createOrderError = resource.message ?: "Failed to create order"
                            )
                        }
                    }
                }
            }
        }
    }

    fun verifyPayment(payId: String) {
        val currentPaymentFlow = _paymentFlowState.value
        if (currentPaymentFlow == null) {
            _uiState.update {
                it.copy(
                    verifyOrderError = "Payment flow data not found. Please try again."
                )
            }
            return
        }

        viewModelScope.launch {
            orderRepository.verifyOrder(
                orderId = currentPaymentFlow.orderId,
                payId = payId,
                productOrderId = currentPaymentFlow.productOrderId.toString()
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                isVerifyingOrder = true,
                                verifyOrderError = null
                            )
                        }
                    }
                    is Resource.Success -> {
                        resource.data?.let { response ->
                            _uiState.update {
                                it.copy(
                                    isVerifyingOrder = false,
                                    isOrderVerified = true,
                                    verificationMessage = response.message,
                                    verifyOrderError = null
                                )
                            }

                            // Clear payment flow state after successful verification
                            _paymentFlowState.update { null }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isVerifyingOrder = false,
                                verifyOrderError = resource.message ?: "Failed to verify payment"
                            )
                        }
                    }
                }
            }
        }
    }

    fun handlePaymentFailure(errorMessage: String) {
        _uiState.update {
            it.copy(
                isCreatingOrder = false,
                isVerifyingOrder = false,
                createOrderError = "Payment failed: $errorMessage"
            )
        }
        // Clear payment flow state on failure
        _paymentFlowState.update { null }
    }

    fun clearOrderData() {
        _uiState.update {
            OrderUiState()
        }
        _paymentFlowState.update { null }
    }

    fun clearErrors() {
        _uiState.update {
            it.copy(
                createOrderError = null,
                verifyOrderError = null
            )
        }
    }

    fun clearVerificationMessage() {
        _uiState.update {
            it.copy(
                isOrderVerified = false,
                verificationMessage = null
            )
        }
    }

    // Retry methods
    fun retryCreateOrder(productId: Int, quantity: Int) {
        initiateOrderCreation(productId, quantity)
    }

    fun retryVerifyPayment(payId: String) {
        verifyPayment(payId)
    }
}