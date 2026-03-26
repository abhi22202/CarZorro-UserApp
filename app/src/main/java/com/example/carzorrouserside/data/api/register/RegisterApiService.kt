package com.example.carzorrouserside.data.api.register

import com.example.carzorrouserside.data.model.register.RegisterRequest
import com.example.carzorrouserside.data.model.register.RegisterResponse
import com.example.carzorrouserside.data.model.register.SignUpOtpVerificationRequest
import com.example.carzorrouserside.data.model.register.SignUpOtpVerificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserAuthRegisterApiService {
    /**
     * Register a new user and get initial OTP
     */
    @POST("v1/user/register/send-otp")
    suspend fun initiateUserRegistration(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    /**
     * Verify OTP for user registration completion
     */
    @POST("v1/user/register/verify-otp")
    suspend fun verifyRegistrationOtp(
        @Body request: SignUpOtpVerificationRequest
    ): Response<SignUpOtpVerificationResponse>

    /**
     * Resend OTP by calling the same registration endpoint again
     */
    @POST("v1/user/register/send-otp")
    suspend fun resendRegistrationOtp(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>


}