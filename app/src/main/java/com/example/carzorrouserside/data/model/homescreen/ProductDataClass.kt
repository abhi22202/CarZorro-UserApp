package com.example.carzorrouserside.data.model.homescreen


import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ProductData
)

data class ProductData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("data") val products: List<Product>,
    @SerializedName("first_page_url") val firstPageUrl: String,
    @SerializedName("from") val from: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("last_page_url") val lastPageUrl: String,
    @SerializedName("links") val links: List<PageLink>,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    @SerializedName("path") val path: String,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    @SerializedName("to") val to: Int,
    @SerializedName("total") val total: Int
)

data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("supplier_id") val supplierId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("price") val price: String,
    @SerializedName("discount_price") val discountPrice: String,
    @SerializedName("stock_quantity") val stockQuantity: Int,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("image") val image: ProductImage?
)

data class ProductImage(
    @SerializedName("id") val id: Int,
    @SerializedName("supplier_id") val supplierId: Int,
    @SerializedName("supplier_product_id") val supplierProductId: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("product_images") val productImageUrl: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class PageLink(
    @SerializedName("url") val url: String?,
    @SerializedName("label") val label: String,
    @SerializedName("active") val active: Boolean
)

// UI State for Products
data class ProductUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val searchQuery: String = ""
)

data class ProductDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ProductDetail
)

data class ProductDetail(
    @SerializedName("id")
    val id: Int,
    @SerializedName("supplier_id")
    val supplierId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("category_id")
    val categoryId: Int,
    @SerializedName("price")
    val price: String,
    @SerializedName("discount_price")
    val discountPrice: String,
    @SerializedName("stock_quantity")
    val stockQuantity: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("images")
    val images: List<ProductDetailImage>,
    @SerializedName("highlights")
    val highlights: List<ProductHighlight>
)

data class ProductDetailImage(
    @SerializedName("id")
    val id: Int,
    @SerializedName("supplier_id")
    val supplierId: Int,
    @SerializedName("supplier_product_id")
    val supplierProductId: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("product_images")
    val productImages: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class ProductHighlight(
    @SerializedName("id")
    val id: Int,
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("key")
    val key: String,
    @SerializedName("value")
    val value: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// UI State for Product Detail
data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val productDetail: ProductDetail? = null,
    val error: String? = null
)