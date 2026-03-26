package com.example.carzorrouserside.data.repository.homescreen

import com.example.carzorrouserside.data.api.homescreen.PackageApiService
import com.example.carzorrouserside.data.model.homescreen.PackageDetailData
import com.example.carzorrouserside.data.model.homescreen.PackagePaginationData
import com.example.carzorrouserside.util.LocationManager
import com.example.carzorrouserside.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PackageRepository @Inject constructor(
    private val apiService: PackageApiService,
    private val locationManager: LocationManager
) {
    suspend fun getPopularPackages(perPage: Int, page: Int): Flow<Resource<PackagePaginationData>> = flow {
        emit(Resource.Loading())
        try {
            val location = locationManager.getBestAvailableLocation()
            val response = apiService.getPopularPackages(
                latitude = location.latitude,
                longitude = location.longitude,
                perPage = perPage,
                page = page
            )

            if (response.isSuccessful && response.body() != null) {
                val packageData = response.body()!!.paginationData
                if (packageData != null) {
                    emit(Resource.Success(packageData))
                } else {
                    emit(Resource.Error("No popular package data found."))
                }
            } else {
                emit(Resource.Error(response.message() ?: "An unknown error occurred"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Couldn't reach the server. Check your internet connection."))
        }
    }

    suspend fun getAllPackages(perPage: Int, page: Int): Flow<Resource<PackagePaginationData>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getAllPackages(
                perPage = perPage,
                page = page
            )

            if (response.isSuccessful && response.body() != null) {
                val packageData = response.body()!!.paginationData
                if (packageData != null) {
                    emit(Resource.Success(packageData))
                } else {
                    emit(Resource.Error("No package data found."))
                }
            } else {
                emit(Resource.Error(response.message() ?: "An unknown error occurred"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Couldn't reach the server. Check your internet connection."))
        }
    }

    suspend fun getPackageDetails(packageId: Int): Flow<Resource<PackageDetailData>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getPackageDetails(packageId)

            if (response.isSuccessful && response.body() != null) {
                val detailData = response.body()!!.data
                if (detailData != null) {
                    emit(Resource.Success(detailData))
                } else {
                    emit(Resource.Error("Package details not found."))
                }
            } else {
                emit(Resource.Error(response.message() ?: "An error occurred fetching details."))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Couldn't reach the server."))
        }
    }

    suspend fun searchPackages(query: String): Flow<Resource<PackagePaginationData>> = flow {
        emit(Resource.Loading())
        try {
            val location = locationManager.getBestAvailableLocation()
            val response = apiService.searchPackages(
                query = query,
                latitude = location.latitude,
                longitude = location.longitude
            )

            if (response.isSuccessful && response.body() != null) {
                val packageData = response.body()!!.paginationData
                if (packageData != null) {
                    emit(Resource.Success(packageData))
                } else {
                    emit(Resource.Success(PackagePaginationData(1, emptyList(), 1, 0)))
                }
            } else {
                emit(Resource.Error(response.message() ?: "Search failed."))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Couldn't reach the server. Check your internet connection."))
        }
    }
}