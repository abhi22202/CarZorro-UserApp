package com.example.carzorrouserside.data.repository.vendor

import com.example.carzorrouserside.data.api.vendor.VendorApiService
import com.example.carzorrouserside.data.model.vendor.FavouriteVendorDto
import javax.inject.Inject

class FavouriteVendorRepository @Inject constructor(
    private val apiService: VendorApiService
) {
    suspend fun getFavouriteVendors(): Result<List<FavouriteVendorDto>> {
        return try {
            val response = apiService.getFavouriteVendors()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error fetching data: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}