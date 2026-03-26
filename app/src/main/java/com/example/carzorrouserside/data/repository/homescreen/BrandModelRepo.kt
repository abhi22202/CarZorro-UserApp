package com.example.carzorrouserside.data.repository.homescreen

import com.example.carzorrouserside.data.api.homescreen.BrandModelApiService
import com.example.carzorrouserside.data.model.homescreen.BrandResponse
import com.example.carzorrouserside.data.model.homescreen.ModelResponse
import com.example.carzorrouserside.data.token.UserPreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrandModelRepository @Inject constructor(
    private val apiService: BrandModelApiService,
    private val userPreferencesManager: UserPreferencesManager
) {

    suspend fun getBrands(): Result<BrandResponse> {
        return try {
            val token = userPreferencesManager.getJwtToken()
            if (token == null) {
                return Result.failure(Exception("Authentication token not available"))
            }

            val response = apiService.getBrands("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch brands"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchBrands(searchQuery: String): Result<BrandResponse> {
        return try {
            val token = userPreferencesManager.getJwtToken()
            if (token == null) {
                return Result.failure(Exception("Authentication token not available"))
            }

            val response = apiService.searchBrands("Bearer $token", searchQuery)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to search brands"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getModels(brandId: Int): Result<ModelResponse> {
        return try {
            val token = userPreferencesManager.getJwtToken()
            if (token == null) {
                return Result.failure(Exception("Authentication token not available"))
            }

            val response = apiService.getModels("Bearer $token", brandId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch models"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchModels(brandId: Int, searchQuery: String): Result<ModelResponse> {
        return try {
            val token = userPreferencesManager.getJwtToken()
            if (token == null) {
                return Result.failure(Exception("Authentication token not available"))
            }

            val response = apiService.searchModels("Bearer $token", brandId, searchQuery)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to search models"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}