package com.example.carzorrouserside.data.model.car

import com.google.gson.annotations.SerializedName

data class AddCarRequest(
    @SerializedName("brand_id")
    val brandId: Int,

    @SerializedName("brand_model_id")
    val brandModelId: Int,

    @SerializedName("license_plate")
    val licensePlate: String,

    @SerializedName("fuel_type")
    val fuelType: String
)

data class AddCarResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Any>
)