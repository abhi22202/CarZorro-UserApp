package com.example.carzorrouserside.data.repository.profile

import android.util.Log
import com.example.carzorrouserside.data.api.loginscreen.UserAuthApiService
import com.example.carzorrouserside.data.model.profile.EditProfileRequest
import com.example.carzorrouserside.data.model.profile.UserBasicDetails
import com.example.carzorrouserside.data.model.profile.WalletHistoryPaginationData
import com.example.carzorrouserside.util.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: UserAuthApiService
) {
    companion object {
        private const val TAG = "ProfileRepository"
    }

    suspend fun getUserBasicDetails(): Resource<UserBasicDetails> {
        return try {
            Log.d(TAG, "Fetching user basic details")
            val response = api.getUserBasicDetails()

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Log.d(TAG, "Successfully fetched user basic details: ${body.data.fullName}")
                    Resource.Success(body.data)
                } else {
                    val errorMessage = body?.message ?: "Failed to fetch user details"
                    Log.e(TAG, "API returned unsuccessful response: $errorMessage")
                    Resource.Error(errorMessage)
                }
            } else {
                Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
                val errorMessage = when (response.code()) {
                    401 -> "Session expired. Please login again."
                    500 -> "Server error occurred. Please try again later."
                    else -> "Network error occurred. Please check your connection."
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching user basic details", e)
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "No internet connection."
                else -> "An unexpected error occurred."
            }
            Resource.Error(errorMessage)
        }
    }

    suspend fun editProfile(request: EditProfileRequest): Resource<Boolean> {
        return try {
            Log.d(TAG, "Updating user profile")
            Log.d(TAG, "EditProfileRequest - fullName: ${request.fullName}, email: ${request.email}, phone: ${request.phone}, altPhone: ${request.altPhone}, dob: ${request.dob}, gender: ${request.gender}")
            val response = api.editProfile(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "Successfully updated user profile: ${body.message}")
                    Resource.Success(true)
                } else {
                    val errorMessage = body?.message ?: "Failed to update profile"
                    Log.e(TAG, "API returned unsuccessful response: $errorMessage")
                    Resource.Error(errorMessage)
                }
            } else {
                Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
                val errorMessage = when (response.code()) {
                    401 -> "Session expired. Please login again."
                    500 -> "Server error occurred. Please try again later."
                    else -> "Network error occurred. Please check your connection."
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating profile", e)
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "No internet connection."
                else -> "An unexpected error occurred."
            }
            Resource.Error(errorMessage)
        }
    }

    suspend fun editProfileImage(imageFile: File): Resource<Boolean> {
        return try {
            Log.d(TAG, "Uploading profile image: ${imageFile.name}")
            
            // Validate file size (2MB max)
            val fileSizeInMB = imageFile.length() / (1024.0 * 1024.0)
            if (fileSizeInMB > 2.0) {
                Log.e(TAG, "Image file size exceeds 2MB limit: ${fileSizeInMB}MB")
                return Resource.Error("Image size must be less than 2MB")
            }
            
            // Create request body for the file
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            
            // Create MultipartBody.Part
            val profilePicPart = MultipartBody.Part.createFormData(
                "profile_pic",
                imageFile.name,
                requestFile
            )
            
            val response = api.editProfileImage(profilePicPart)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d(TAG, "Successfully uploaded profile image: ${body.message}")
                    Resource.Success(true)
                } else {
                    val errorMessage = body?.message ?: "Failed to upload profile image"
                    Log.e(TAG, "API returned unsuccessful response: $errorMessage")
                    Resource.Error(errorMessage)
                }
            } else {
                Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
                val errorMessage = when (response.code()) {
                    401 -> "Session expired. Please login again."
                    500 -> "Server error occurred. Please try again later."
                    else -> "Network error occurred. Please check your connection."
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while uploading profile image", e)
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "No internet connection."
                else -> "An unexpected error occurred: ${e.message}"
            }
            Resource.Error(errorMessage)
        }
    }

    suspend fun getWalletHistory(page: Int = 1): Resource<WalletHistoryPaginationData> {
        return try {
            Log.d(TAG, "Fetching wallet history for page: $page")
            val response = api.getWalletHistory(page)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Log.d(TAG, "Successfully fetched wallet history: ${body.data.transactions.size} transactions")
                    Resource.Success(body.data)
                } else {
                    val errorMessage = body?.message ?: "Failed to fetch wallet history"
                    Log.e(TAG, "API returned unsuccessful response: $errorMessage")
                    Resource.Error(errorMessage)
                }
            } else {
                Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
                val errorMessage = when (response.code()) {
                    401 -> "Session expired. Please login again."
                    500 -> "Server error occurred. Please try again later."
                    else -> "Network error occurred. Please check your connection."
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching wallet history", e)
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "No internet connection."
                else -> "An unexpected error occurred."
            }
            Resource.Error(errorMessage)
        }
    }
}

