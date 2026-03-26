package com.example.carzorrouserside.data.repository.car

import com.example.carzorrouserside.data.api.homescreen.CarApiService
import com.example.carzorrouserside.data.model.car.AddCarRequest
import com.example.carzorrouserside.data.model.car.AddCarResponse
import com.example.carzorrouserside.data.model.car.CarListingItem
import com.example.carzorrouserside.data.model.car.EditCarRequest
import com.example.carzorrouserside.data.model.car.EditCarResponse
import com.example.carzorrouserside.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class CarRepository @Inject constructor(
    private val apiService: CarApiService
) {
    fun getCarListing(): Flow<Resource<List<CarListingItem>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getCarListing()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.data))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "An unknown error occurred"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Couldn't reach the server. Check your internet connection."))
        }
    }
    fun addCar(addCarRequest: AddCarRequest): Flow<Resource<AddCarResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.addCar(addCarRequest)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = if (errorBody.isNullOrEmpty()) {
                    "An unknown error occurred"
                } else {
                    try {
                        val jsonObj = JSONObject(errorBody)
                        jsonObj.getString("message")
                    } catch (e: Exception) {
                        response.message()
                    }
                }
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Couldn't reach the server. Check your internet connection."))
        }
    }

    fun editCar(editCarRequest: EditCarRequest): Flow<Resource<EditCarResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.editCar(editCarRequest)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = if (errorBody.isNullOrEmpty()) {
                    "An unknown error occurred"
                } else {
                    try {
                        JSONObject(errorBody).getString("message")
                    } catch (e: Exception) {
                        response.message()
                    }
                }
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Couldn't reach the server. Check your internet connection."))
        }
    }
}