package com.example.carzorrouserside.ui.theme.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.car.AddCarRequest
import com.example.carzorrouserside.data.model.car.EditCarRequest
import com.example.carzorrouserside.data.repository.car.CarRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

// State for the UI, including dynamic data like the screen title
data class CarDetailsUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val screenTitle: String = "Add Your Car Details",
    val brandName: String = "",
    val modelName: String = "",
    val imageUrl: String? = null
)

@HiltViewModel
class CarDetailsViewModel @Inject constructor(
    private val carRepository: CarRepository,
    // SavedStateHandle is used to access navigation arguments
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(CarDetailsUiState())
        private set

    var vehicleNumber by mutableStateOf("")
        private set
    var selectedFuelType by mutableStateOf("Petrol") // Default to Petrol
        private set

    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // Private properties to hold IDs
    private var carIdToEdit: Int? = null
    private var brandId: Int? = null
    private var modelId: Int? = null

    init {
        // Read arguments from navigation
        val carIdArg = savedStateHandle.get<Int>("carId")
        brandId = savedStateHandle.get<Int>("brandId")
        modelId = savedStateHandle.get<Int>("modelId")

        val brandNameArg = savedStateHandle.get<String>("brandName")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) } ?: ""
        val modelNameArg = savedStateHandle.get<String>("modelName")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) } ?: ""
        val imageUrlArg = savedStateHandle.get<String>("imageUrl")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }

        // Debug logging
        Log.d("CarDetailsViewModel", "Navigation arguments:")
        Log.d("CarDetailsViewModel", "  carId: $carIdArg")
        Log.d("CarDetailsViewModel", "  brandId: $brandId")
        Log.d("CarDetailsViewModel", "  modelId: $modelId")
        Log.d("CarDetailsViewModel", "  brandName: $brandNameArg")
        Log.d("CarDetailsViewModel", "  modelName: $modelNameArg")

        // Determine if we are in "edit" or "add" mode
        if (carIdArg != null && carIdArg != -1) {
            carIdToEdit = carIdArg
            uiState = uiState.copy(screenTitle = "Edit Your Car Details")
        }

        // Update UI state with brand/model info for display
        uiState = uiState.copy(
            brandName = brandNameArg,
            modelName = modelNameArg,
            imageUrl = if (imageUrlArg == "no_image") null else imageUrlArg
        )
    }

    fun onVehicleNumberChange(newNumber: String) {
        vehicleNumber = newNumber
    }

    fun onFuelTypeSelected(fuelType: String) {
        selectedFuelType = fuelType
    }

    // Single function called by the UI to save the car
    fun saveCar() {
        if (carIdToEdit != null) {
            editCar()
        } else {
            addCar()
        }
    }

    private fun addCar() {
        if (!validateInputs()) return

        Log.d("CarDetailsViewModel", "Adding car with brandId: $brandId, modelId: $modelId")
        val request = AddCarRequest(
            brandId = brandId!!,
            brandModelId = modelId!!,
            licensePlate = vehicleNumber,
            fuelType = selectedFuelType
        )

        viewModelScope.launch {
            carRepository.addCar(request).collect { result ->
                handleApiResult(result, "Car added successfully!")
            }
        }
    }

    private fun editCar() {
        if (!validateInputs()) return

        Log.d("CarDetailsViewModel", "Editing car with id: $carIdToEdit, brandId: $brandId, modelId: $modelId")
        val request = EditCarRequest(
            id = carIdToEdit!!,
            brandId = brandId!!,
            brandModelId = modelId!!,
            licensePlate = vehicleNumber,
            fuelType = selectedFuelType
        )

        viewModelScope.launch {
            carRepository.editCar(request).collect { result ->
                handleApiResult(result, "Car updated successfully!")
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (vehicleNumber.isBlank()) {
            uiState = uiState.copy(errorMessage = "Vehicle number cannot be empty.")
            return false
        }
        if (brandId == null) {
            Log.e("CarDetailsViewModel", "Brand ID is null!")
            uiState = uiState.copy(errorMessage = "Brand ID is missing. Please select a brand.")
            return false
        }
        if (modelId == null) {
            Log.e("CarDetailsViewModel", "Model ID is null!")
            uiState = uiState.copy(errorMessage = "Model ID is missing. Please select a model.")
            return false
        }
        Log.d("CarDetailsViewModel", "Validation passed - brandId: $brandId, modelId: $modelId")
        return true
    }

    private fun <T> handleApiResult(result: Resource<T>, successDefaultMessage: String) {
        when (result) {
            is Resource.Loading -> {
                uiState = uiState.copy(isLoading = true, errorMessage = null, successMessage = null)
            }
            is Resource.Success -> {
                uiState = uiState.copy(isLoading = false, successMessage = result.message ?: successDefaultMessage)
                viewModelScope.launch { _navigationEvent.send(Unit) }
            }
            is Resource.Error -> {
                uiState = uiState.copy(isLoading = false, errorMessage = result.message ?: "An unknown error occurred.")
            }
        }
    }

    fun clearErrorMessage() {
        uiState = uiState.copy(errorMessage = null)
    }
}