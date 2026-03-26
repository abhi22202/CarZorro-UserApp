package com.example.carzorrouserside.ui.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.car.CarListingItem
import com.example.carzorrouserside.data.model.car.EditCarRequest
import com.example.carzorrouserside.data.repository.car.CarRepository
//import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.HomepageServiceViewModel.Companion.TAG
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

data class CarListingUiState(
    val isLoading: Boolean = false,
    val cars: List<CarListingItem> = emptyList(),
    val error: String? = null
)

sealed class CarEditEvent {
    data class Success(val message: String) : CarEditEvent()
    data class Error(val message: String) : CarEditEvent()
    object Loading : CarEditEvent()
}


@HiltViewModel
class CarListingViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {
    private val _carsState = MutableStateFlow(CarListingUiState())
    val carsState = _carsState.asStateFlow()

    private val _editEventChannel = Channel<CarEditEvent>()
    val editEvent = _editEventChannel.receiveAsFlow()

    init{
        fetchCars()
    }


    fun fetchCars() {
        carRepository.getCarListing().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _carsState.value = CarListingUiState(isLoading = true)
                }
                is Resource.Success -> {
                    _carsState.value = CarListingUiState(cars = result.data ?: emptyList())
                    Log.d("CAR_DEBUG", "Fetched cars: ${result.data}")
                }
                is Resource.Error -> {
                    _carsState.value = CarListingUiState(error = result.message ?: "An unexpected error occurred")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun editCar(editCarRequest: EditCarRequest) {
        carRepository.editCar(editCarRequest).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _editEventChannel.send(CarEditEvent.Loading)
                }
                is Resource.Success -> {
                    _editEventChannel.send(CarEditEvent.Success(result.data?.message ?: "Car updated successfully"))
                    fetchCars()
                }
                is Resource.Error -> {
                    _editEventChannel.send(CarEditEvent.Error(result.message ?: "An unexpected error occurred"))
                }
            }
        }.launchIn(viewModelScope)
    }
    fun retryLoadCars() {
        Log.d(TAG, "Retrying to load cars")
        fetchCars()
    }
}