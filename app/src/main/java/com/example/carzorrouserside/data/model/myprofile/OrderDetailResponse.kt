package com.example.carzorrouserside.data.model.booking

import com.google.gson.annotations.SerializedName

// Main response wrapper
data class OrderDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: OrderDetail?
)

// The main object containing all order details
data class OrderDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("user_id") val userId: Int?, // Can be null based on your sample
    @SerializedName("vendor_id") val vendorId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: String,
    @SerializedName("total_amount") val totalAmount: String,
    @SerializedName("order_status") val orderStatus: String,
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("ordered_at") val orderedAt: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("product") val product: ProductDetail
)

// The nested product object inside the order detail
data class ProductDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("supplier_id") val supplierId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("price") val price: String,
    @SerializedName("discount_price") val discountPrice: String?,
    @SerializedName("stock_quantity") val stockQuantity: Int,
    @SerializedName("status") val status: String
)