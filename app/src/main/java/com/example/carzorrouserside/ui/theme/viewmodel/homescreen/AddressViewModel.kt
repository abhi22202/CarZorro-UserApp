package com.example.carzorrouserside.ui.theme.viewmodel.homescreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.*
import com.example.carzorrouserside.data.repository.homescreen.AddressRepository
import com.example.carzorrouserside.di.SharedPreferencesManager
import com.example.carzorrouserside.util.LocationManager
import com.example.carzorrouserside.util.Resource
import com.example.carzorrouserside.util.SnackbarEvent
import com.example.carzorrouserside.util.SnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val repository: AddressRepository,
    private val locationManager: LocationManager,
    private val preferencesManager: SharedPreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "AddressViewModel"
    }


    private val _selectedAddressForHome = MutableLiveData<AddressItem?>()
    val selectedAddressForHome: LiveData<AddressItem?> = _selectedAddressForHome

    private val _addAddressState = MutableLiveData<Resource<AddressResponse>?>()
    val addAddressState: MutableLiveData<Resource<AddressResponse>?> = _addAddressState

    private val _showBottomSheet = MutableLiveData<Boolean>()
    val showBottomSheet: LiveData<Boolean> = _showBottomSheet

    private val _addressListState = MutableLiveData<Resource<AddressListingResponse>?>()
    val addressListState: LiveData<Resource<AddressListingResponse>?> = _addressListState

    private val _currentAddressList = MutableLiveData<List<AddressItem>>()
    val currentAddressList: LiveData<List<AddressItem>> = _currentAddressList

    // =================== NEW STATES FOR EDIT & DELETE ===================

    private val _editAddressState = MutableLiveData<Resource<EditAddressResponse>?>()
    val editAddressState: LiveData<Resource<EditAddressResponse>?> = _editAddressState

    private val _deleteAddressState = MutableLiveData<Resource<DeleteAddressResponse>?>()
    val deleteAddressState: LiveData<Resource<DeleteAddressResponse>?> = _deleteAddressState

    private val _isEditMode = MutableLiveData(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    private val _addressToEdit = MutableLiveData<AddressItem?>()
    val addressToEdit: LiveData<AddressItem?> = _addressToEdit

    private val _snackbarEvent = MutableLiveData<SnackbarEvent?>()
    val snackbarEvent: LiveData<SnackbarEvent?> = _snackbarEvent

    private val _currentCoordinates = MutableLiveData<String>()
    val currentCoordinates: LiveData<String> = _currentCoordinates

    // =================== INITIALIZATION ===================

    init {
        loadSelectedAddress()
        fetchCurrentLocation()
    }

    // =================== ADDRESS LIST OPERATIONS ===================

    fun loadAddressList() {
        Log.d(TAG, "=== Starting loadAddressList in ViewModel ===")

        if (!repository.isAuthenticated()) {
            Log.e(TAG, "User not authenticated for address listing")
            _addressListState.value = Resource.Error("Authentication required. Please login again.")
            return
        }

        viewModelScope.launch {
            try {
                repository.getAddressList().collect { resource ->
                    _addressListState.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            val addressList = resource.data?.data ?: emptyList()
                            _currentAddressList.value = addressList
                            preferencesManager.saveAddressCount(addressList.size)
                            Log.i(TAG, "Loaded ${addressList.size} addresses")

                            if (_selectedAddressForHome.value == null) {
                                addressList.find { it.isDefault }?.let { defaultAddress ->
                                    selectAddressForHome(defaultAddress)
                                }
                            }
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Failed to load addresses: ${resource.message}")
                            showErrorSnackbar(resource.message ?: "Failed to load addresses")
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Loading addresses...")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadAddressList: ${e.message}", e)
                _addressListState.value = Resource.Error("Failed to load addresses: ${e.message}")
            }
        }
    }

    fun retryLoadAddressList() {
        Log.d(TAG, "Retrying address list load")
        loadAddressList()
    }

    // =================== ADD ADDRESS ===================

    fun addAddress(addressDetails: AddressDetails) {
        Log.d(TAG, "=== Starting addAddress in ViewModel ===")

        if (!repository.isAuthenticated()) {
            showErrorSnackbar("Please login to add address")
            return
        }

        viewModelScope.launch {
            try {
                _addAddressState.value = Resource.Loading()

                val locationData = locationManager.getCurrentLocation()
                val latLongString = "${locationData.latitude},${locationData.longitude}"
                _currentCoordinates.value = latLongString

                preferencesManager.saveLastLocation(locationData.latitude, locationData.longitude)

                val updatedAddressDetails = addressDetails.copy(
                    latitudeAndLongitude = latLongString
                )

                val result = repository.addAddress(updatedAddressDetails)
                _addAddressState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.i(TAG, "Address added successfully")
                        showSuccessSnackbar("Address added successfully")
                        loadAddressList()
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Failed to add address: ${result.message}")
                        showErrorSnackbar(result.message ?: "Failed to add address")
                    }
                    is Resource.Loading -> {
                        Log.d(TAG, "Still loading...")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in addAddress: ${e.message}", e)
                _addAddressState.value = Resource.Error("Failed to process address: ${e.message}")
                showErrorSnackbar("Failed to process address: ${e.message}")
            }
        }
    }

    // =================== EDIT ADDRESS ===================

    fun editAddress(addressDetails: AddressDetails) {
        Log.d(TAG, "=== Starting editAddress in ViewModel ===")

        if (!repository.isAuthenticated()) {
            showErrorSnackbar("Please login to edit address")
            return
        }

        viewModelScope.launch {
            try {
                _editAddressState.value = Resource.Loading()

                val result = repository.editAddress(addressDetails)
                _editAddressState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.i(TAG, "Address edited successfully")
                        showSuccessSnackbar("Address updated successfully")
                        hideBottomSheet()
                        resetEditMode()
                        loadAddressList()
                    }
                    is Resource.Error<*> -> {
                        Log.e(TAG, "Failed to edit address: ${result.message}")
                        showErrorSnackbar(result.message ?: "Failed to update address")
                    }
                    is Resource.Loading<*> -> {
                        Log.d(TAG, "Still loading edit...")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in editAddress: ${e.message}", e)
                _editAddressState.value = Resource.Error("Failed to edit address: ${e.message}")
                showErrorSnackbar("Failed to edit address: ${e.message}")
            }
        }
    }

    // =================== DELETE ADDRESS ===================

    fun deleteAddress(addressItem: AddressItem) {
        Log.d(TAG, "=== Starting deleteAddress in ViewModel ===")
        Log.d(TAG, "Deleting address: ${addressItem.fullName} (ID: ${addressItem.id})")

        if (!repository.isAuthenticated()) {
            showErrorSnackbar("Please login to delete address")
            return
        }

        viewModelScope.launch {
            try {
                _deleteAddressState.value = Resource.Loading()

                // Store address information for the delete operation
                preferencesManager.storeSelectedAddressForOperations(addressItem)

                val result = repository.deleteAddressById(addressItem.id)
                _deleteAddressState.value = result

                when (result) {
                    is Resource.Success<*> -> {
                        Log.i(TAG, "Address deleted successfully")
                        showSuccessSnackbar("Address deleted successfully")

                        // If deleted address was the selected one, clear selection
                        if (_selectedAddressForHome.value?.id == addressItem.id) {
                            clearSelectedAddress()
                        }

                        loadAddressList()
                    }
                    is Resource.Error<*> -> {
                        Log.e(TAG, "Failed to delete address: ${result.message}")
                        showErrorSnackbar(result.message ?: "Failed to delete address")
                    }
                    is Resource.Loading<*> -> {
                        Log.d(TAG, "Still loading delete...")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in deleteAddress: ${e.message}", e)
                _deleteAddressState.value = Resource.Error("Failed to delete address: ${e.message}")
                showErrorSnackbar("Failed to delete address: ${e.message}")
            }
        }
    }

    // =================== BOTTOM SHEET MANAGEMENT ===================

    fun showBottomSheet() {
        Log.d(TAG, "Showing address bottom sheet")
        _showBottomSheet.value = true
    }

    fun hideBottomSheet() {
        Log.d(TAG, "Hiding address bottom sheet")
        _showBottomSheet.value = false
        resetEditMode()
    }

    fun showEditBottomSheet(address: AddressItem) {
        Log.d(TAG, "Showing edit bottom sheet for: ${address.fullName}")
        _isEditMode.value = true
        _addressToEdit.value = address

        // Store address ID for editing
        preferencesManager.storeAddressToEdit(address.id)
        preferencesManager.storeSelectedAddressForOperations(address)

        _showBottomSheet.value = true
    }

    private fun resetEditMode() {
        _isEditMode.value = false
        _addressToEdit.value = null
        preferencesManager.clearAddressToEdit()
        preferencesManager.clearSelectedAddressForOperations()
    }

    // =================== ADDRESS SELECTION ===================

    fun selectAddressForHome(address: AddressItem) {
        Log.d(TAG, "Selecting address for home: ${address.fullName} in ${address.city}")
        _selectedAddressForHome.value = address
        preferencesManager.saveSelectedAddress(address)
    }

    fun loadSelectedAddress() {
        Log.d(TAG, "Loading previously selected address from preferences")

        if (!preferencesManager.isSelectedAddressValid(24)) {
            Log.d(TAG, "Stored address is too old or invalid, clearing it")
            preferencesManager.clearSelectedAddress()
            return
        }

        val savedAddress = preferencesManager.getSelectedAddress()
        savedAddress?.let {
            Log.d(TAG, "Found valid saved address: ${it.fullName} in ${it.city}")
            _selectedAddressForHome.value = it
        } ?: run {
            Log.d(TAG, "No saved address found")
        }
    }

    fun clearSelectedAddress() {
        Log.d(TAG, "Clearing selected address")
        _selectedAddressForHome.value = null
        preferencesManager.clearSelectedAddress()
    }

    // =================== LOCATION MANAGEMENT ===================

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== Manual location fetch requested ===")
                val locationData = locationManager.getCurrentLocation()
                val latLongString = "${locationData.latitude},${locationData.longitude}"

                Log.i(TAG, "Current Location Details:")
                Log.i(TAG, "  - Latitude: ${locationData.latitude}")
                Log.i(TAG, "  - Longitude: ${locationData.longitude}")
                Log.i(TAG, "  - Formatted: $latLongString")

                _currentCoordinates.value = latLongString
                preferencesManager.saveLastLocation(locationData.latitude, locationData.longitude)

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching location manually: ${e.message}", e)
            }
        }
    }

    // =================== SNACKBAR MANAGEMENT ===================

    fun showSuccessSnackbar(message: String) {
        _snackbarEvent.value = SnackbarEvent(message, SnackbarType.SUCCESS)
    }

    private fun showErrorSnackbar(message: String) {
        _snackbarEvent.value = SnackbarEvent(message, SnackbarType.ERROR)
    }

    private fun showInfoSnackbar(message: String) {
        _snackbarEvent.value = SnackbarEvent(message, SnackbarType.INFO)
    }

    fun clearSnackbarEvent() {
        _snackbarEvent.value = null
    }

    // =================== STATE CLEARING ===================

    fun clearAddAddressState() {
        _addAddressState.value = null
    }

    fun clearEditAddressState() {
        _editAddressState.value = null
    }

    fun clearDeleteAddressState() {
        _deleteAddressState.value = null
    }

    fun clearAddressListState() {
        _addressListState.value = null
    }

    // =================== UTILITY METHODS ===================

    fun convertAddressItemToDetails(addressItem: AddressItem): AddressDetails {
        return AddressDetails(
            addressId = addressItem.id,
            fullName = addressItem.fullName,
            phoneNumber = addressItem.phoneNumber,
            altPhoneNumber = addressItem.altPhoneNumber ?: "",
            houseNo = addressItem.houseNo ?: "",
            locality = addressItem.locality ?: "",
            city = addressItem.city,
            district = addressItem.district ?: "",
            state = addressItem.state ?: "",
            pincode = addressItem.pincode,
            addressType = addressItem.addressType ?: "home",
            landmark = addressItem.landmark ?: ""
        )
    }

    fun isAuthenticated(): Boolean = repository.isAuthenticated()

    fun getAddressCount(): Int = _currentAddressList.value?.size ?: preferencesManager.getAddressCount()

    fun isAddressListLoading(): Boolean = _addressListState.value is Resource.Loading

    fun hasAddressListError(): Boolean = _addressListState.value is Resource.Error

    fun getAddressListErrorMessage(): String? = (_addressListState.value as? Resource.Error)?.message

    fun refreshData() {
        Log.d(TAG, "=== Refreshing all address data ===")
        fetchCurrentLocation()
        loadAddressList()
    }

    fun debugPreferences() {
        preferencesManager.debugLogStoredData()
    }

    fun debugAuth() {
        repository.debugAuthState()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}
