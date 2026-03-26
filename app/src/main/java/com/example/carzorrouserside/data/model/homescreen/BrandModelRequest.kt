package com.example.carzorrouserside.data.model.homescreen

data class BrandResponse(
    val success: Boolean,
    val message: String,
    val data: List<Brand>
)

data class Brand(
    val id: Int,
    val brand_name: String,
    val brand_img_url: String
)

// Model API Response Models
data class ModelResponse(
    val success: Boolean,
    val message: String,
    val data: List<CarModel>
)

data class CarModel(
    val id: Int,
    val brand_id: Int,
    val model_name: String,
    val model_img_url: String?
)

// UI State Models
data class BrandSelectionUiState(
    val isLoading: Boolean = false,
    val brands: List<Brand> = emptyList(),
    val error: String? = null
)

data class ModelSelectionUiState(
    val isLoading: Boolean = false,
    val models: List<CarModel> = emptyList(),
    val error: String? = null,
    val selectedBrandName: String = ""
)