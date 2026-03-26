package com.example.carzorrouserside.data.repository.homescreen



import com.example.carzorrouserside.data.api.homescreen.OrderApiService
import com.example.carzorrouserside.data.model.homescreen.CreateOrderRequest
import com.example.carzorrouserside.data.model.homescreen.CreateOrderResponse
import com.example.carzorrouserside.data.model.homescreen.VerifyOrderRequest
import com.example.carzorrouserside.data.model.homescreen.VerifyOrderResponse
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderApiService: OrderApiService,
    private val userPreferencesManager: UserPreferencesManager
) {

    suspend fun createOrder(
        productId: Int,
        quantity: Int
    ): Flow<Resource<CreateOrderResponse>> = flow {
        try {
            emit(Resource.Loading())

            // Get user credentials
            val authToken = userPreferencesManager.getJwtToken()
            val userId = userPreferencesManager.getUserId()

            if (authToken.isNullOrBlank()) {
                emit(Resource.Error("Authentication required. Please login again."))
                return@flow
            }

            if (userId == null) {
                emit(Resource.Error("User ID not found. Please login again."))
                return@flow
            }

            val request = CreateOrderRequest(
                productId = productId,
                quantity = quantity
            )

            val response = orderApiService.createOrder(
                userId = userId.toString(),
                authorization = "Bearer $authToken",
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.let { createOrderResponse ->
                    if (createOrderResponse.success) {
                        emit(Resource.Success(createOrderResponse))
                    } else {
                        emit(Resource.Error(createOrderResponse.message))
                    }
                } ?: emit(Resource.Error("Unknown error occurred"))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Authentication failed. Please login again."))
                    403 -> emit(Resource.Error("Access denied. Check your permissions."))
                    404 -> emit(Resource.Error("Product not found."))
                    422 -> emit(Resource.Error("Invalid product or quantity."))
                    500 -> emit(Resource.Error("Server error. Please try again later."))
                    else -> emit(Resource.Error("Network error: ${response.code()}"))
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage}"))
        }
    }

    suspend fun verifyOrder(
        orderId: String,
        payId: String,
        productOrderId: String
    ): Flow<Resource<VerifyOrderResponse>> = flow {
        try {
            emit(Resource.Loading())

            // Get user credentials
            val authToken = userPreferencesManager.getJwtToken()
            val userId = userPreferencesManager.getUserId()

            if (authToken.isNullOrBlank()) {
                emit(Resource.Error("Authentication required. Please login again."))
                return@flow
            }

            if (userId == null) {
                emit(Resource.Error("User ID not found. Please login again."))
                return@flow
            }

            val request = VerifyOrderRequest(
                orderId = orderId,
                payId = payId,
                productOrderId = productOrderId
            )

            val response = orderApiService.verifyOrder(
                userId = userId.toString(),
                authorization = "Bearer $authToken",
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.let { verifyOrderResponse ->
                    if (verifyOrderResponse.success) {
                        emit(Resource.Success(verifyOrderResponse))
                    } else {
                        emit(Resource.Error(verifyOrderResponse.message))
                    }
                } ?: emit(Resource.Error("Unknown error occurred"))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Authentication failed. Please login again."))
                    403 -> emit(Resource.Error("Access denied. Check your permissions."))
                    404 -> emit(Resource.Error("Order not found."))
                    422 -> emit(Resource.Error("Invalid payment details."))
                    500 -> emit(Resource.Error("Server error. Please try again later."))
                    else -> emit(Resource.Error("Network error: ${response.code()}"))
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network connection failed. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage}"))
        }
    }
}