package com.example.carzorrouserside.data.model

data class ServiceCategory(
    val name: String,

    val carImageResId: Int
)

data class ServiceProvider(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val rating: String
)
