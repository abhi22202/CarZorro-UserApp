package com.example.carzorrouserside.data.api.loginscreen



import com.example.carzorrouserside.data.model.loginscreen.SendOtpRequest
import com.example.carzorrouserside.data.model.loginscreen.SendOtpResponse
import com.example.carzorrouserside.data.model.loginscreen.VerifyOtpRequest
import com.example.carzorrouserside.data.model.loginscreen.VerifyOtpResponse
import com.example.carzorrouserside.data.model.profile.EditProfileRequest
import com.example.carzorrouserside.data.model.profile.EditProfileImageResponse
import com.example.carzorrouserside.data.model.profile.EditProfileResponse
import com.example.carzorrouserside.data.model.profile.UserBasicDetailsResponse
import com.example.carzorrouserside.data.model.profile.WalletHistoryResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

data class FcmTokenRequest(
    val user_id: Int,
    val device_type: String,
    val device_id: String,
    val fcm_token: String
)
data class GenericSuccessResponse(
    @SerializedName("message")
    val message: String?
)

interface UserAuthApiService {
    @POST("v1/user/login/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>
    @POST("v1/user/login/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>
    @POST("v1/user/update-device-token")
    suspend fun updateFcmToken(
        @Body fcmTokenRequest: FcmTokenRequest
        // Add @Header("Authorization") if needed and not handled by an interceptor
    ): Response<GenericSuccessResponse> // Use the success response type
    
    @GET("v1/user/auth/profile/basic-details")
    suspend fun getUserBasicDetails(): Response<UserBasicDetailsResponse>
    
    @POST("v1/user/auth/profile/edit-profile")
    suspend fun editProfile(
        @Body request: EditProfileRequest
    ): Response<EditProfileResponse>
    
    @Multipart
    @POST("v1/user/auth/profile/edit-image")
    suspend fun editProfileImage(
        @Part profilePic: MultipartBody.Part
    ): Response<EditProfileImageResponse>
    
    @GET("v1/user/auth/profile/wallet-history")
    suspend fun getWalletHistory(
        @Query("page") page: Int = 1
    ): Response<WalletHistoryResponse>
}
