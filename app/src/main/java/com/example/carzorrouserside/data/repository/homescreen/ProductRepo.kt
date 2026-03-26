package com.example.carzorrouserside.data.repository.homescreen


import com.example.carzorrouserside.data.api.homescreen.ProductApiService
import com.example.carzorrouserside.data.model.homescreen.ProductDetailResponse
import com.example.carzorrouserside.data.model.homescreen.ProductResponse
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productApiService: ProductApiService,
    private val userPreferencesManager: UserPreferencesManager
) {

    suspend fun getProducts(
        page: Int = 1,
        perPage: Int = 20,
        searchQuery: String? = null
    ): Flow<Resource<ProductResponse>> = flow {
        try {
            emit(Resource.Loading())

            val response = productApiService.getProducts(
                page = page,
                perPage = perPage,
                search = searchQuery?.takeIf { it.isNotBlank() }
            )

            if (response.isSuccessful) {
                response.body()?.let { productResponse ->
                    if (productResponse.success) {
                        emit(Resource.Success(productResponse))
                    } else {
                        emit(Resource.Error(productResponse.message))
                    }
                } ?: emit(Resource.Error("Unknown error occurred"))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Authentication failed. Please login again."))
                    403 -> emit(Resource.Error("Access denied. Check your permissions."))
                    404 -> emit(Resource.Error("Products not found."))
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
    suspend fun getProductDetail(productId: Int): Flow<Resource<ProductDetailResponse>> = flow {
        try {
            emit(Resource.Loading())

            // Get the authorization token
            val authToken = userPreferencesManager.getJwtToken()
            if (authToken.isNullOrBlank()) {
                emit(Resource.Error("Authentication required. Please login again."))
                return@flow
            }

            val response = productApiService.getProductDetail(
                productId = productId,
                authorization = "Bearer $authToken"
            )

            if (response.isSuccessful) {
                response.body()?.let { productDetailResponse ->
                    if (productDetailResponse.success) {
                        emit(Resource.Success(productDetailResponse))
                    } else {
                        emit(Resource.Error(productDetailResponse.message))
                    }
                } ?: emit(Resource.Error("Unknown error occurred"))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Authentication failed. Please login again."))
                    403 -> emit(Resource.Error("Access denied. Check your permissions."))
                    404 -> emit(Resource.Error("Product not found."))
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