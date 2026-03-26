package com.example.carzorrouserside.data.model.homescreen

import com.google.gson.annotations.SerializedName


data class AddressRequest(
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("alt_phone_number")
    val altPhoneNumber: String? = null,
    @SerializedName("country")
    val country: String = "India",
    @SerializedName("state")
    val state: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("pincode")
    val pincode: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude_and_longitude")
    val latitudeAndLongitude: String? = null,
    @SerializedName("landmark")
    val landmark: String,
    @SerializedName("address_type")
    val addressType: String = "home",
    @SerializedName("building_no_or_name")
    val buildingNoOrName: String
)

// Existing AddressResponse (unchanged)
data class AddressResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<Any>
)

// NEW: Edit Address Request
data class EditAddressRequest(
    @SerializedName("address_id")
    val address_id: Int,
    @SerializedName("full_name")
    val full_name: String,
    @SerializedName("phone_number")
    val phone_number: String,
    @SerializedName("alt_phone_number")
    val alt_phone_number: String? = null,
    @SerializedName("country")
    val country: String = "India",
    @SerializedName("state")
    val state: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("pincode")
    val pincode: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude_and_longitude")
    val latitude_and_longitude: String,
    @SerializedName("landmark")
    val landmark: String? = null,
    @SerializedName("address_type")
    val address_type: String = "home",
    @SerializedName("building_no_or_name")
    val building_no_or_name: String? = null
)


// NEW: Edit Address Response
data class EditAddressResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<Any> = emptyList()
)

// NEW: Delete Address Request
data class DeleteAddressRequest(
    @SerializedName("address_id")
    val addressId: Int
)

// NEW: Delete Address Response
data class DeleteAddressResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<Any> = emptyList()
)

// UPDATED: AddressDetails to support edit operations
data class AddressDetails(
    val addressId: Int? = null,
    val fullName: String = "",
    val phoneNumber: String = "",
    val altPhoneNumber: String = "",
    val houseNo: String = "",
    val locality: String = "",
    val city: String = "",
    val district: String = "",
    val state: String = "",
    val pincode: String = "",
    val addressType: String = "home",
    val landmark: String = "",
    val latitudeAndLongitude: String = ""
)

// Existing AddressListingResponse (unchanged)
data class AddressListingResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<AddressItem>?
)

// UPDATED: AddressItem with additional helper properties
data class AddressItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("default_status")
    val defaultStatus: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("alt_phone_number")
    val altPhoneNumber: String?,
    @SerializedName("country")
    val country: String,
    @SerializedName("latitude_and_longitude")
    val latitudeAndLongitude: String?,
    @SerializedName("state")
    val state: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("pincode")
    val pincode: String,
    @SerializedName("building_no_or_name")
    val buildingNoOrName: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("landmark")
    val landmark: String?,
    @SerializedName("address_type")
    val addressType: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) {
    // Helper properties for UI
    val isDefault: Boolean get() = defaultStatus == "1"
    val isActive: Boolean get() = status == "1"

    val fullAddress: String get() = buildString {
        append(buildingNoOrName)
        if (address.isNotBlank()) append(", $address")
        if (landmark?.isNotBlank() == true) append(", Near $landmark")
        append(", $city")
        append(", $state - $pincode")
    }

    val shortAddress: String get() = buildString {
        append(buildingNoOrName)
        if (landmark?.isNotBlank() == true) append(", $landmark")
        append(", $city")
    }

    // NEW: Helper properties for edit/delete operations
    val houseNo: String? get() = buildingNoOrName.takeIf { it.isNotBlank() }
    val locality: String? get() = landmark?.takeIf { it.isNotBlank() }
    val district: String? get() = address.split(",").getOrNull(1)?.trim()
}

// Extension functions for data conversion
fun AddressDetails.toAddressRequest(coordinates: String): AddressRequest {
    return AddressRequest(
        fullName = fullName,
        phoneNumber = phoneNumber,
        altPhoneNumber = altPhoneNumber.takeIf { it.isNotBlank() },
        country = "India",
        state = state,
        city = city,
        pincode = pincode,
        address = "$houseNo, $locality, $district".trim().replace(Regex(",\\s*,"), ","),
        latitudeAndLongitude = coordinates,
        landmark = locality,
        addressType = addressType,
        buildingNoOrName = houseNo
    )
}



fun AddressItem.toAddressDetails(): AddressDetails {
    return AddressDetails(
        addressId = id,
        fullName = fullName,
        phoneNumber = phoneNumber,
        altPhoneNumber = altPhoneNumber ?: "",
        houseNo = buildingNoOrName,
        locality = landmark ?: "",
        city = city,
        district = address.split(",").getOrNull(1)?.trim() ?: "",
        state = state,
        pincode = pincode,
        addressType = addressType,
        landmark = landmark ?: "",
        latitudeAndLongitude = latitudeAndLongitude ?: ""
    )
}