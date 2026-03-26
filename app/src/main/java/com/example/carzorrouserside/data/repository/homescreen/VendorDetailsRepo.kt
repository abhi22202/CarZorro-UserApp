package com.example.carzorrouserside.data.repository.vendor

import android.util.Log
import com.example.carzorrouserside.data.api.vendor.VendorApiService
import com.example.carzorrouserside.data.model.vendor.FavoriteVendorRequest
import com.example.carzorrouserside.data.model.vendor.FavoriteVendorResponse
import com.example.carzorrouserside.data.model.vendor.VendorData
import com.example.carzorrouserside.data.model.vendor.VendorPackage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class VendorDetailsRepository @Inject constructor(
    private val apiService: VendorApiService,
    private val gson: Gson
) {
    fun getVendorDetails(vendorId: Int): Flow<Result<VendorData>> = flow {
        try {
            Log.d("VendorDetailsRepo", "Fetching vendor details for vendorId: $vendorId")
            val response = apiService.getVendorDetails(vendorId.toString())
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("VendorDetailsRepo", "Response successful: success=${body?.success}, hasData=${body?.data != null}")
                
                if (body?.success == true && body.data != null) {
                    Log.d("VendorDetailsRepo", "Vendor details fetched successfully: ${body.data.vendor.name}")
                    emit(Result.success(body.data))
                } else {
                    val errorMsg = body?.message ?: "Failed to get vendor details"
                    Log.e("VendorDetailsRepo", "API returned success=false: $errorMsg")
                    emit(Result.failure(Throwable(errorMsg)))
                }
            } else {
                // Try to parse error response body
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    if (errorBody != null) {
                        val errorResponse = gson.fromJson(errorBody, Map::class.java)
                        errorResponse["message"] as? String ?: response.message()
                    } else {
                        response.message()
                    }
                } catch (e: Exception) {
                    response.message()
                }
                
                Log.e("VendorDetailsRepo", "API Error: ${response.code()} - $errorMessage")
                emit(Result.failure(Throwable("Error ${response.code()}: $errorMessage")))
            }
        } catch (e: HttpException) {
            Log.e("VendorDetailsRepo", "HTTP Exception: ${e.message}", e)
            emit(Result.failure(Throwable("Network error: ${e.message}")))
        } catch (e: IOException) {
            Log.e("VendorDetailsRepo", "IO Exception: ${e.message}", e)
            emit(Result.failure(Throwable("Connection error: ${e.message}")))
        } catch (e: Exception) {
            Log.e("VendorDetailsRepo", "Unexpected error: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getVendorPackages(vendorId: Int): Flow<Result<List<VendorPackage>>> = flow {
        try {
            Log.d("VendorDetailsRepo", "Fetching vendor packages for vendorId: $vendorId")
            val response = apiService.getVendorPackages(vendorId.toString())
            
            if (response.isSuccessful) {
                val body = response.body()
                // --- FIXED POTENTIAL BUG HERE ---
                // Added '&& body.data != null' to prevent a crash if the API returns success but no data
                if (body?.success == true && body.data != null) {
                    Log.d("VendorDetailsRepo", "Vendor packages fetched successfully: ${body.data.size} packages")
                    emit(Result.success(body.data))
                } else {
                    val errorMsg = body?.message ?: "Failed to get vendor packages"
                    Log.e("VendorDetailsRepo", "API returned success=false: $errorMsg")
                    emit(Result.failure(Throwable(errorMsg)))
                }
            } else {
                // Try to parse error response body
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    if (errorBody != null) {
                        val errorResponse = gson.fromJson(errorBody, Map::class.java)
                        errorResponse["message"] as? String ?: response.message()
                    } else {
                        response.message()
                    }
                } catch (e: Exception) {
                    response.message()
                }
                
                Log.e("VendorDetailsRepo", "API Error: ${response.code()} - $errorMessage")
                emit(Result.failure(Throwable("Error ${response.code()}: $errorMessage")))
            }
        } catch (e: HttpException) {
            Log.e("VendorDetailsRepo", "HTTP Exception: ${e.message}", e)
            emit(Result.failure(Throwable("Network error: ${e.message}")))
        } catch (e: IOException) {
            Log.e("VendorDetailsRepo", "IO Exception: ${e.message}", e)
            emit(Result.failure(Throwable("Connection error: ${e.message}")))
        } catch (e: Exception) {
            Log.e("VendorDetailsRepo", "Unexpected error: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun toggleFavoriteVendor(token: String, vendorId: Int): Flow<Result<FavoriteVendorResponse>> = flow {
        try {
            val request = FavoriteVendorRequest(vendorId = vendorId)
            val authHeader = "Bearer $token"
            val response = apiService.toggleFavoriteStatus(authHeader, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Result.success(body))
                } else {
                    emit(Result.failure(Throwable(body?.message ?: "Failed to update favorite status")))
                }
            } else {
                emit(Result.failure(Throwable("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}