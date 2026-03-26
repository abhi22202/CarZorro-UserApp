package com.example.carzorrouserside.data.repository

import android.util.Log
import com.example.carzorrouserside.data.api.BookingApiService
import com.example.carzorrouserside.data.model.booking.BookingDetailsData
import com.example.carzorrouserside.data.model.booking.BookingDetailsResponse
import com.example.carzorrouserside.data.model.booking.BookingListingData
import com.example.carzorrouserside.data.model.booking.CancelBookingRequest
import com.example.carzorrouserside.data.model.booking.CancelReason
import com.example.carzorrouserside.data.model.booking.OfferActionRequest
import com.example.carzorrouserside.data.model.booking.CheckCouponRequest
import com.example.carzorrouserside.data.model.booking.CheckCouponResponse
import com.example.carzorrouserside.data.model.booking.CouponData
import com.example.carzorrouserside.data.model.booking.BookingPaymentRequest
import com.example.carzorrouserside.data.model.booking.BookingPaymentResponse
import com.example.carzorrouserside.data.model.booking.BookingPaymentData
import com.example.carzorrouserside.data.model.booking.BookingPaymentConfirmRequest
import com.example.carzorrouserside.data.model.booking.BookingPaymentConfirmResponse
import com.example.carzorrouserside.data.model.register.ErrorResponse
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.domain.repository.BookingRepository
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val apiService: BookingApiService,
    private val gson: Gson,
    private val userPreferencesManager: UserPreferencesManager
) : BookingRepository {
    private val TAG = "BookingRepositoryImpl"

    override suspend fun getBookingListing(status: String?): Result<BookingListingData> {
        return try {
            val response = apiService.getBookingListing(status)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Log.d(TAG, "✅ Successfully fetched booking listing with status: $status")
                    Result.success(body.data)
                } else {
                    val errorMessage = body?.message ?: "Failed to fetch booking listing"
                    Log.e(TAG, "❌ API returned unsuccessful response: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = when (response.code()) {
                    500 -> "Server error. Please try again later."
                    else -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Failed to fetch booking listing"
                }
                Log.e(TAG, "❌ API Error (${response.code()}): $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                500 -> "Server error. Please try again later."
                else -> "Network error: ${e.message()}"
            }
            Log.e(TAG, "❌ HttpException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            val errorMessage = "Network connection error. Please check your internet connection."
            Log.e(TAG, "❌ IOException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getBookingCancelReasons(): Result<List<CancelReason>> {
        return try {
            val response = apiService.getCancelReasons()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val reasons = body.data ?: emptyList()
                    Log.d(TAG, "✅ Successfully fetched ${reasons.size} cancel reasons")
                    Result.success(reasons)
                } else {
                    val errorMessage = body?.message ?: "Failed to fetch cancel reasons"
                    Log.e(TAG, "❌ API returned unsuccessful response: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = when (response.code()) {
                    500 -> "Server error. Please try again later."
                    else -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Failed to fetch cancel reasons"
                }
                Log.e(TAG, "❌ API Error (${response.code()}): $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                500 -> "Server error. Please try again later."
                else -> "Network error: ${e.message()}"
            }
            Log.e(TAG, "❌ HttpException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            val errorMessage = "Network connection error. Please check your internet connection."
            Log.e(TAG, "❌ IOException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelBooking(request: CancelBookingRequest): Result<Unit> {
        return try {
            // Note: When canceling before vendor accepts, reason is optional
            // Validation is handled by the API (422 response if required)
            // We allow the request to proceed even without a reason

            val response = apiService.cancelBooking(request)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Booking cancelled successfully")
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    422 -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Invalid data provided. Please check your input."
                    500 -> "Server error. Please try again later."
                    else -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Failed to cancel booking"
                }
                Log.e(TAG, "❌ API Error (${response.code()}): $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                422 -> parseErrorResponse(e.response()?.errorBody()?.string())
                    ?: "Invalid data provided. Please check your input."
                500 -> "Server error. Please try again later."
                else -> "Network error: ${e.message()}"
            }
            Log.e(TAG, "❌ HttpException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            val errorMessage = "Network connection error. Please check your internet connection."
            Log.e(TAG, "❌ IOException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptBookingOffer(request: OfferActionRequest): Result<Unit> {
        return try {
            val response = apiService.acceptBookingOffer(request)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Booking offer accepted successfully")
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> {
                        val message = parseErrorResponse(response.errorBody()?.string())
                        if (message?.contains("Booking Accepted by Other.", ignoreCase = true) == true) {
                            "Booking Accepted by Other."
                        } else {
                            message ?: "Invalid request. Please check your input."
                        }
                    }
                    422 -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Invalid data provided. Please check your input."
                    500 -> "Server error. Please try again later."
                    else -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Failed to accept booking offer"
                }
                Log.e(TAG, "❌ API Error (${response.code()}): $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> {
                    val message = parseErrorResponse(e.response()?.errorBody()?.string())
                    if (message?.contains("Booking Accepted by Other.", ignoreCase = true) == true) {
                        "Booking Accepted by Other."
                    } else {
                        message ?: "Invalid request. Please check your input."
                    }
                }
                422 -> parseErrorResponse(e.response()?.errorBody()?.string())
                    ?: "Invalid data provided. Please check your input."
                500 -> "Server error. Please try again later."
                else -> "Network error: ${e.message()}"
            }
            Log.e(TAG, "❌ HttpException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            val errorMessage = "Network connection error. Please check your internet connection."
            Log.e(TAG, "❌ IOException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun declineBookingOffer(request: OfferActionRequest): Result<Unit> {
        return try {
            val response = apiService.declineBookingOffer(request)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Booking offer declined successfully")
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> {
                        val message = parseErrorResponse(response.errorBody()?.string())
                        if (message?.contains("Booking Accepted by Other.", ignoreCase = true) == true) {
                            "Booking Accepted by Other."
                        } else {
                            message ?: "Invalid request. Please check your input."
                        }
                    }
                    422 -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Invalid data provided. Please check your input."
                    500 -> "Server error. Please try again later."
                    else -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Failed to decline booking offer"
                }
                Log.e(TAG, "❌ API Error (${response.code()}): $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> {
                    val message = parseErrorResponse(e.response()?.errorBody()?.string())
                    if (message?.contains("Booking Accepted by Other.", ignoreCase = true) == true) {
                        "Booking Accepted by Other."
                    } else {
                        message ?: "Invalid request. Please check your input."
                    }
                }
                422 -> parseErrorResponse(e.response()?.errorBody()?.string())
                    ?: "Invalid data provided. Please check your input."
                500 -> "Server error. Please try again later."
                else -> "Network error: ${e.message()}"
            }
            Log.e(TAG, "❌ HttpException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            val errorMessage = "Network connection error. Please check your internet connection."
            Log.e(TAG, "❌ IOException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getBookingDetails(bookingId: Int): Result<BookingDetailsData> {
        return try {
            val response = apiService.getBookingDetails(bookingId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Log.d(TAG, "✅ Booking details fetched successfully for booking ID: $bookingId")
                    Result.success(body.data)
                } else {
                    val errorMessage = body?.message ?: "Failed to fetch booking details"
                    Log.e(TAG, "❌ API returned unsuccessful response: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = when (response.code()) {
                    422 -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Invalid booking ID provided."
                    500 -> "Server error. Please try again later."
                    else -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Error fetching booking details"
                }
                Log.e(TAG, "❌ API Error (${response.code()}): $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                422 -> parseErrorResponse(e.response()?.errorBody()?.string())
                    ?: "Invalid booking ID provided."
                500 -> "Server error. Please try again later."
                else -> "Network error: ${e.message()}"
            }
            Log.e(TAG, "❌ HttpException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            val errorMessage = "Network connection error. Please check your internet connection."
            Log.e(TAG, "❌ IOException: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun checkCoupon(request: CheckCouponRequest): Result<CouponData> {
        return try {
            Log.d(TAG, "🎫 Checking coupon: ${gson.toJson(request)}")
            Log.d(TAG, "🌐 Endpoint: POST /v1/user/auth/booking/check-coupon")
            val response = apiService.checkCoupon(request)
            Log.d(TAG, "📥 Coupon response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            Log.d(TAG, "📥 Response headers: ${response.headers()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status && body.data != null) {
                    Log.d(TAG, "✅ Coupon validated successfully: ${body.data.couponId}")
                    Result.success(body.data)
                } else {
                    val errorMessage = body?.message ?: "Invalid coupon code"
                    Log.e(TAG, "❌ Coupon validation failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Try to get error message from response body
                val errorBodyString = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }
                
                // Log first 500 chars of error body to avoid logging huge HTML pages
                val errorPreview = errorBodyString?.take(500) ?: "null"
                Log.e(TAG, "❌ Coupon API Error (${response.code()}): $errorPreview")
                
                val errorMessage = when (response.code()) {
                    400 -> {
                        // Try to parse as JSON first, then fallback to plain string
                        val parsedError = parseErrorResponse(errorBodyString)
                        if (parsedError != null) {
                            parsedError
                        } else {
                            val body = response.body()
                            body?.message ?: errorBodyString?.takeIf { it.isNotBlank() } ?: "Invalid or expired coupon"
                        }
                    }
                    404 -> {
                        // 404 might mean endpoint not found or coupon not found
                        // Check if it's an HTML error page (endpoint not found) vs JSON error (coupon not found)
                        if (errorBodyString?.contains("<!DOCTYPE html>", ignoreCase = true) == true || 
                            errorBodyString?.contains("<html>", ignoreCase = true) == true) {
                            "Coupon validation endpoint not found. Please contact support."
                        } else {
                            val parsedError = parseErrorResponse(errorBodyString)
                            parsedError ?: "Coupon not found. Please check the code and try again."
                        }
                    }
                    422 -> {
                        val parsedError = parseErrorResponse(errorBodyString)
                        parsedError ?: errorBodyString?.takeIf { it.isNotBlank() } ?: "Invalid data provided"
                    }
                    500 -> {
                        val body = response.body()
                        body?.message ?: errorBodyString?.takeIf { it.isNotBlank() } ?: "Server error. Please try again later."
                    }
                    else -> {
                        val parsedError = parseErrorResponse(errorBodyString)
                        parsedError ?: errorBodyString?.takeIf { it.isNotBlank() } ?: "Failed to check coupon"
                    }
                }
                Log.e(TAG, "❌ Final coupon error message: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception checking coupon: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun makeBookingPayment(request: BookingPaymentRequest): Result<BookingPaymentData> {
        return try {
            val token = userPreferencesManager.getJwtToken()
            val userId = userPreferencesManager.getUserId()
            Log.d(TAG, "💰 Making booking payment: bookingId=${request.bookingId}, option=${request.option}, couponId=${request.couponId}")
            Log.d(TAG, "📤 Request: ${gson.toJson(request)}")
            Log.d(TAG, "🔐 Auth check: userId=$userId, hasToken=${token != null}, tokenLength=${token?.length ?: 0}")
            
            // Verify booking ownership and status before payment
            try {
                val bookingDetailsResponse = apiService.getBookingDetails(request.bookingId)
                if (bookingDetailsResponse.isSuccessful) {
                    val bookingData = bookingDetailsResponse.body()?.data
                    if (bookingData != null) {
                        Log.d(TAG, "📋 Booking details: bookingUserId=${bookingData.userId}, currentUserId=$userId, bookingStatus=${bookingData.bookingStatus}")
                        if (bookingData.userId != userId) {
                            Log.e(TAG, "❌ Booking ownership mismatch! Booking belongs to user ${bookingData.userId}, but current user is $userId")
                            return Result.failure(Exception("This booking does not belong to you."))
                        }
                        Log.d(TAG, "✅ Booking ownership verified - booking status: ${bookingData.bookingStatus}")
                    }
                } else {
                    Log.w(TAG, "⚠️ Could not fetch booking details: ${bookingDetailsResponse.code()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Could not verify booking ownership: ${e.message}")
                // Continue with payment attempt anyway
            }
            
            // Don't include user_id in body - backend should extract from JWT token
            // API spec says "No parameters" needed, so only send booking_id, option, and coupon_id
            val paymentRequest = BookingPaymentRequest(
                bookingId = request.bookingId,
                option = request.option,
                couponId = request.couponId,
                userId = null // Don't send user_id - backend extracts from JWT
            )
            Log.d(TAG, "📤 Final payment request: ${gson.toJson(paymentRequest)}")
            Log.d(TAG, "🔐 Token being sent: ${token?.take(50)}...")
            
            // Authorization header is added automatically by AuthInterceptor
            val response = apiService.makeBookingPayment(paymentRequest)
            Log.d(TAG, "📥 Payment response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Empty payment response"))

                // ✅ correct success flag
                if (!body.success) {
                    return Result.failure(Exception(body.message))
                }

                if (request.option == "pay_online") {
                    val orderId = body.data?.orderId
                    val amount = body.data?.amount ?: 0.0

                    if (orderId.isNullOrBlank() || amount <= 0) {
                        return Result.failure(Exception("Invalid Razorpay order data"))
                    }

                    return Result.success(
                        BookingPaymentData(
                            bookingId = request.bookingId,
                            option = request.option,
                            orderId = orderId,
                            amount = amount
                        )
                    )
                }

                if (request.option == "pay_after_service") {
                    return Result.success(
                        BookingPaymentData(
                            bookingId = request.bookingId,
                            option = request.option,
                            orderId = null,
                            amount = body.data?.amount ?: 0.0
                        )
                    )
                }

                return Result.failure(Exception("Unsupported payment option"))
            }


            else {
                // Try to get error message from response body
                val errorBodyString = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }
                
                Log.e(TAG, "❌ Payment API Error (${response.code()}): $errorBodyString")
                
                val errorMessage = when (response.code()) {
                    400, 401, 403 -> {
                        // Try to parse as JSON first, then fallback to plain string
                        val parsedError = parseErrorResponse(errorBodyString)
                        if (parsedError != null) {
                            parsedError
                        } else {
                            // If parsing failed, use the raw string if available
                            errorBodyString?.takeIf { it.isNotBlank() } ?: "Authentication failed. Please try again."
                        }
                    }
                    422 -> parseErrorResponse(errorBodyString)
                        ?: errorBodyString?.takeIf { it.isNotBlank() } ?: "Invalid data provided"
                    404 -> "Payment endpoint not found. Please check your connection."
                    500 -> {
                        val body = response.body()
                        body?.message ?: errorBodyString?.takeIf { it.isNotBlank() } ?: "Server error. Please try again later."
                    }
                    else -> {
                        val parsedError = parseErrorResponse(errorBodyString)
                        parsedError ?: errorBodyString?.takeIf { it.isNotBlank() } ?: "Failed to process payment"
                    }
                }
                Log.e(TAG, "❌ Final error message: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun confirmBookingPayment(request: BookingPaymentConfirmRequest): Result<Unit> {
        return try {
            Log.d(TAG, "✅ Confirming booking payment: orderId=${request.orderId}, paymentId=${request.paymentId}")
            // Authorization header is added automatically by AuthInterceptor
            val response = apiService.confirmBookingPayment(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Log.d(TAG, "✅ Payment confirmed successfully")
                    Result.success(Unit)
                } else {
                    val errorMessage = body?.message ?: "Failed to confirm payment"
                    Log.e(TAG, "❌ Payment confirmation failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = when (response.code()) {
                    422 -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Invalid data provided"
                    500 -> {
                        val body = response.body()
                        body?.message ?: "Server error. Please try again later."
                    }
                    else -> parseErrorResponse(response.errorBody()?.string())
                        ?: "Failed to confirm payment"
                }
                Log.e(TAG, "❌ API Error (${response.code()}): $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Parse error response from API
     * Handles both JSON error responses and plain string error messages
     */
    private fun parseErrorResponse(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) {
            return null
        }
        
        return try {
            // Try to parse as JSON first
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message
        } catch (e: Exception) {
            // If JSON parsing fails, check if it's a plain string error message
            try {
                // Try to extract message from common error formats
                if (errorBody.contains("message", ignoreCase = true)) {
                    // Try to parse as JSON with message field
                    val jsonObject = gson.fromJson(errorBody, Map::class.java) as? Map<*, *>
                    (jsonObject?.get("message") as? String)?.takeIf { it.isNotBlank() }
                } else {
                    // If it looks like a plain error message, return it directly
                    errorBody.trim().takeIf { it.isNotBlank() && !it.startsWith("{") && !it.startsWith("[") }
                } ?: null
            } catch (e2: Exception) {
                // If all parsing fails, return the raw string if it's a reasonable length
                if (errorBody.length < 500) {
                    errorBody.trim()
                } else {
                    null
                }
            }
        }
    }
}

