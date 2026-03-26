package com.example.carzorrouserside.data.api.homescreen

import com.example.carzorrouserside.data.model.car.AddCarRequest
import com.example.carzorrouserside.data.model.car.AddCarResponse
import com.example.carzorrouserside.data.model.car.BaseResponse
import com.example.carzorrouserside.data.model.car.CarListingItem
import com.example.carzorrouserside.data.model.car.EditCarRequest
import com.example.carzorrouserside.data.model.car.EditCarResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CarApiService {

    @GET("v1/user/auth/car/listing")
    suspend fun getCarListing(): Response<BaseResponse<List<CarListingItem>>>

    @POST("v1/user/auth/car/add")
    suspend fun addCar(@Body addCarRequest: AddCarRequest): Response<AddCarResponse>

    @POST("v1/user/auth/car/edit")
    suspend fun editCar(@Body editCarRequest: EditCarRequest): Response<EditCarResponse>
}
