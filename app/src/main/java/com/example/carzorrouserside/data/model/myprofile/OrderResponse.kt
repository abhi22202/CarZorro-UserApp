// in package com.example.carzorrouserside.data.model.booking

package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

data class OrderListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: OrderData?
)

data class OrderData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("data") val orders: List<Order>,
    @SerializedName("total") val total: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("last_page") val lastPage: Int
)

data class Order(
    @SerializedName("id") val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: String,
    @SerializedName("total_amount") val totalAmount: String,
    @SerializedName("order_status") val orderStatus: String, // pending, processing, delivered, cancelled etc.
    @SerializedName("payment_status") val paymentStatus: String, // pending, paid
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("product") val product: Product
)

data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: String,
    @SerializedName("discount_price") val discountPrice: String?,
    @SerializedName("stock_quantity") val stockQuantity: Int
    // You can add an image field here if the API provides it
)