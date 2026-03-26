package com.example.carzorrouserside.data.model.car

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T
)
data class CarListingItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("brand_id")
    val brandId: Int?,

    @SerializedName("brand_model_id")
    val modelId: Int?,

    @SerializedName("user_id")
    val userId: Int?,

    @SerializedName("brand_name")
    val brand: String?,

    @SerializedName("brand_model")
    val model: String?,

    @SerializedName("car_type")
    val carType: String?,

    @SerializedName("fuel_type")
    val fuelType: String,

    @SerializedName("license_plate")
    val registrationNumber: String?,

    @SerializedName("car_image")
    val imageUrl: String?
)
data class EditCarRequest(
    @SerializedName("id")
    val id: Int,

    @SerializedName("brand_id")
    val brandId: Int,

    @SerializedName("brand_model_id")
    val brandModelId: Int,

    @SerializedName("license_plate")
    val licensePlate: String,

    @SerializedName("fuel_type")
    val fuelType: String
)

data class EditCarResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Unit> = emptyList()
)
