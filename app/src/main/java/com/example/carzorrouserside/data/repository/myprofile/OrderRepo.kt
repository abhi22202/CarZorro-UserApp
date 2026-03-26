// in package com.example.carzorrouserside.data.repository

package com.example.carzorrouserside.data.repository

import android.util.Log
import com.example.carzorrouserside.data.api.myprofile.OrderApiInterface
import com.example.carzorrouserside.data.model.BookingStartRequest
import com.example.carzorrouserside.data.model.BookingStartResponse
import com.example.carzorrouserside.data.model.booking.CancelBookingRequest
import com.example.carzorrouserside.data.model.booking.OrderDetailResponse
import com.example.carzorrouserside.data.model.booking.OrderListResponse
import com.example.carzorrouserside.data.repository.homescreen.AddressRepository
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.util.Resource
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLException

class BookingRepository @Inject constructor(
    private val apiService: OrderApiInterface,
    private val userPreferencesManager: UserPreferencesManager
)
{
    companion object {
        const val TAG = "BookingRepository"
    }
    private val gson = Gson()

    fun getOrderHistory(): Flow<Resource<OrderListResponse>> = flow {
        emit(Resource.Loading())

        val token = userPreferencesManager.getJwtToken()
        val userId = userPreferencesManager.getUserId()

        if (token == null || userId == null) {
            emit(Resource.Error("User not authenticated. Please log in again."))
            return@flow
        }

        try {
            val response = apiService.getOrderHistory(token = "Bearer $token", userId = userId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "An unknown error occurred."))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred."))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach the server. Check your internet connection."))
        }
    }fun getOrderDetails(orderId: Int): Flow<Resource<OrderDetailResponse>> = flow {
    emit(Resource.Loading())

    val token = userPreferencesManager.getJwtToken()
    val userId = userPreferencesManager.getUserId()

    if (token == null || userId == null) {
        emit(Resource.Error("User not authenticated. Please log in again."))
        return@flow
    }

    try {
        val response = apiService.getOrderDetails(
            token = "Bearer $token",
            userId = userId,
            orderId = orderId
        )

        if (response.isSuccessful && response.body() != null) {
            emit(Resource.Success(response.body()!!))
        } else {
            // Handle API-specific errors, e.g., "Order not found"
            emit(Resource.Error(response.message() ?: "Could not fetch order details."))
        }
    } catch (e: HttpException) {
        emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred."))
    } catch (e: IOException) {
        emit(Resource.Error("Couldn't reach the server. Check your internet connection."))
    }
}
    suspend fun startBooking(request: BookingStartRequest): Resource<BookingStartResponse> {
        val token = userPreferencesManager.getJwtToken()
            ?: return Resource.Error("Please login again — Token missing")

        Log.d("BookingRepository", "📤 Final Payload = ${Gson().toJson(request)}")
        Log.d("BookingRepository", "🚀 Calling startBooking API: POST v1/user/auth/booking/start")

        return try {
            val response = apiService.startBooking("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Log.d("BookingRepository", "✅ startBooking API SUCCESS - Backend should now send FCM to vendors")
                Log.d("BookingRepository", "📬 Response: ${response.body()}")
                Resource.Success(response.body()!!)
            } else {
                Log.e("BookingRepository", "❌ startBooking API FAILED - Status: ${response.code()}")
                Resource.Error("Server Error: ${response.code()} → ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    } // End of startBooking function

    suspend fun cancelBooking(bookingId: Int): Response<kotlin.Any> {
        return apiService.cancelBooking(com.example.carzorrouserside.data.model.booking.CancelBookingRequest(booking_id = bookingId))
    }

    private fun parseValidationError(errorBody: String?): String? {
        return try {
            errorBody?.let {
                val json = JSONObject(it)
                json.optString("message").takeIf { message -> message.isNotBlank() }
            }
        } catch (e: Exception) {
            Log.w(AddressRepository.Companion.TAG, "Failed to parse validation error: ${e.message}")
            null
        }
    }}




