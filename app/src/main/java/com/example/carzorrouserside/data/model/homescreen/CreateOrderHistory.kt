package com.example.carzorrouserside.data.model.homescreen


import com.google.gson.annotations.SerializedName

// Create Order Request
data class CreateOrderRequest(
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int
)

// Create Order Response
data class CreateOrderResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: OrderData
)

data class OrderData(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("product_order_id")
    val productOrderId: Int
)

// Verify Order Request
data class VerifyOrderRequest(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("pay_id")
    val payId: String,
    @SerializedName("product_order_id")
    val productOrderId: String
)

// Verify Order Response
data class VerifyOrderResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Any? = null // Can be null or contain additional verification data
)

// UI State for Order Management
data class OrderUiState(
    val isCreatingOrder: Boolean = false,
    val isVerifyingOrder: Boolean = false,
    val orderData: OrderData? = null,
    val createOrderError: String? = null,
    val verifyOrderError: String? = null,
    val isOrderVerified: Boolean = false,
    val verificationMessage: String? = null
)

// Payment flow state
data class PaymentFlowState(
    val productId: Int,
    val quantity: Int,
    val amount: Int,
    val orderId: String,
    val productOrderId: Int,
    val razorpayKey: String = "rzp_test_voGDMqEZY5d9ux"// Replace with your actual key
)