package com.example.carzorrouserside.ui.theme.screens.homescreen.address

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.homescreen.AddressDetails
import com.example.carzorrouserside.data.model.homescreen.AddressResponse
import com.example.carzorrouserside.data.model.homescreen.EditAddressResponse
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.AddressViewModel
import com.example.carzorrouserside.util.Resource

// Validation Helper Functions
object AddressValidation {
    fun containsEmojis(text: String): Boolean {
        val emojiRegex = Regex("[\\p{So}\\p{Sk}\\p{Cn}\\p{Cf}\\p{Cs}\\p{Co}]")
        return emojiRegex.containsMatchIn(text)
    }
    fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = Regex("^[6-9]\\d{9}$")
        return phone.matches(phoneRegex)
    }
    fun isValidPincode(pincode: String): Boolean {
        val pincodeRegex = Regex("^[1-9][0-9]{5}$")
        return pincode.matches(pincodeRegex)
    }
    fun isValidName(name: String): Boolean {
        val nameRegex = Regex("^[a-zA-Z\\s]+$")
        return name.matches(nameRegex) && !containsEmojis(name)
    }
    fun isValidText(text: String): Boolean {
        return !containsEmojis(text) && text.trim().isNotEmpty()
    }
    fun isValidHouseNo(houseNo: String): Boolean {
        val houseRegex = Regex("^[a-zA-Z0-9\\s\\-\\/\\,\\.]+$")
        return houseNo.matches(houseRegex) && !containsEmojis(houseNo)
    }
}

data class ValidationError(
    val field: String,
    val message: String
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressBottomSheet(
    viewModel: AddressViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onAddressAdded: () -> Unit
)

{
    val showBottomSheet by viewModel.showBottomSheet.observeAsState(false)
    val isEditMode by viewModel.isEditMode.observeAsState(false)
    val addressToEdit by viewModel.addressToEdit.observeAsState()
    val addAddressState by viewModel.addAddressState.observeAsState()
    val editAddressState by viewModel.editAddressState.observeAsState()
    val currentCoordinates by viewModel.currentCoordinates.observeAsState("")

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(addAddressState) {
        if (addAddressState is Resource.Success) {
            val successMessage = (addAddressState as Resource.Success<AddressResponse>).data?.message ?: "Address added successfully"
            viewModel.showSuccessSnackbar(successMessage)
            onAddressAdded()
            viewModel.hideBottomSheet()
            viewModel.clearAddAddressState()
        }
    }

    LaunchedEffect(editAddressState) {
        if (editAddressState is Resource.Success) {
            val successMessage = (editAddressState as Resource.Success<EditAddressResponse>).data?.message
                ?: "Address updated successfully"
            viewModel.showSuccessSnackbar(successMessage)
            onAddressAdded()
            viewModel.hideBottomSheet()
            viewModel.clearEditAddressState()
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.hideBottomSheet()
                onDismiss()
            },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            AddressEntryContent(
                onDismiss = {
                    viewModel.hideBottomSheet()
                    onDismiss()
                },
                onSaveAddress = { addressDetails ->
                    if (isEditMode) {
                        viewModel.editAddress(addressDetails)
                    } else {
                        viewModel.addAddress(addressDetails)
                    }
                },
                isLoading = (addAddressState is Resource.Loading) || (editAddressState is Resource.Loading),
                errorMessage = when {
                    addAddressState is Resource.Error -> (addAddressState as Resource.Error<AddressResponse>).message
                    editAddressState is Resource.Error -> (editAddressState as Resource.Error<EditAddressResponse>).message
                    else -> null
                },
                isEditMode = isEditMode,
                addressToEdit = if (isEditMode) {
                    addressToEdit?.let { viewModel.convertAddressItemToDetails(it) }
                } else null,
                currentCoordinates = currentCoordinates
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddressEntryContent(
    onDismiss: () -> Unit,
    onSaveAddress: (AddressDetails) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isEditMode: Boolean = false,
    addressToEdit: AddressDetails? = null,
    currentCoordinates: String = ""
) {
    var fullName by remember { mutableStateOf(addressToEdit?.fullName ?: "") }
    var phoneNumber by remember { mutableStateOf(addressToEdit?.phoneNumber ?: "") }
    var altPhoneNumber by remember { mutableStateOf(addressToEdit?.altPhoneNumber ?: "") }
    var houseNo by remember { mutableStateOf(addressToEdit?.houseNo ?: "") }
    var locality by remember { mutableStateOf(addressToEdit?.locality ?: "") }
    var city by remember { mutableStateOf(addressToEdit?.city ?: "") }
    var district by remember { mutableStateOf(addressToEdit?.district ?: "") }
    var state by remember { mutableStateOf(addressToEdit?.state ?: "") }
    var pincode by remember { mutableStateOf(addressToEdit?.pincode ?: "") }
    var addressType by remember { mutableStateOf(addressToEdit?.addressType ?: "") }
    var landmark by remember { mutableStateOf(addressToEdit?.landmark ?: "") }

    var validationErrors by remember { mutableStateOf(listOf<ValidationError>()) }
    var showValidationErrors by remember { mutableStateOf(false) }

    LaunchedEffect(addressToEdit) {
        addressToEdit?.let { address ->
            fullName = address.fullName
            phoneNumber = address.phoneNumber
            altPhoneNumber = address.altPhoneNumber
            houseNo = address.houseNo
            locality = address.locality
            city = address.city
            district = address.district
            state = address.state
            pincode = address.pincode
            addressType = address.addressType
            landmark = address.landmark
        }
    }

    fun validateForm(): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        if (addressType.isBlank()) errors.add(ValidationError("addressType", "Please select an address type"))
        if (fullName.isBlank()) errors.add(ValidationError("fullName", "Full name is required"))
        else if (!AddressValidation.isValidName(fullName)) errors.add(ValidationError("fullName", "Name should contain only letters and spaces, no emojis"))
        else if (fullName.length < 2) errors.add(ValidationError("fullName", "Name should be at least 2 characters long"))
        if (phoneNumber.isBlank()) errors.add(ValidationError("phoneNumber", "Phone number is required"))
        else if (!AddressValidation.isValidPhoneNumber(phoneNumber)) errors.add(ValidationError("phoneNumber", "Enter a valid 10-digit Indian mobile number"))
        if (altPhoneNumber.isNotBlank() && !AddressValidation.isValidPhoneNumber(altPhoneNumber)) errors.add(ValidationError("altPhoneNumber", "Enter a valid 10-digit mobile number or leave blank"))
        if (houseNo.isBlank()) errors.add(ValidationError("houseNo", "House/Flat number is required"))
        else if (!AddressValidation.isValidHouseNo(houseNo)) errors.add(ValidationError("houseNo", "House number contains invalid characters"))
        if (locality.isBlank()) errors.add(ValidationError("locality", "Locality/Area is required"))
        else if (!AddressValidation.isValidText(locality)) errors.add(ValidationError("locality", "Locality should not contain emojis or special characters"))
        if (city.isBlank()) errors.add(ValidationError("city", "City is required"))
        else if (!AddressValidation.isValidName(city)) errors.add(ValidationError("city", "City name should contain only letters and spaces"))
        if (district.isBlank()) errors.add(ValidationError("district", "District is required"))
        else if (!AddressValidation.isValidName(district)) errors.add(ValidationError("district", "District name should contain only letters and spaces"))
        if (state.isBlank()) errors.add(ValidationError("state", "State is required"))
        else if (!AddressValidation.isValidName(state)) errors.add(ValidationError("state", "State name should contain only letters and spaces"))
        if (pincode.isBlank()) errors.add(ValidationError("pincode", "Pincode is required"))
        else if (!AddressValidation.isValidPincode(pincode)) errors.add(ValidationError("pincode", "Enter a valid 6-digit pincode"))
        if (landmark.isNotBlank() && !AddressValidation.isValidText(landmark)) errors.add(ValidationError("landmark", "Landmark should not contain emojis or special characters"))
        return errors
    }

    fun getFieldError(fieldName: String): String? = if (showValidationErrors) validationErrors.find { it.field == fieldName }?.message else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = if (isEditMode) "Edit Address Details" else "Enter Address Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(24.dp))
        }

        if (currentCoordinates.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = appPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Using current location coordinates",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }

        Text(
            text = "Address Type *",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (getFieldError("addressType") != null) 4.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AddressTypeChip(text = "Home", isSelected = addressType == "home", onClick = { addressType = "home" }, modifier = Modifier.weight(1f))
            AddressTypeChip(text = "Work", isSelected = addressType == "work", onClick = { addressType = "work" }, modifier = Modifier.weight(1f))
            AddressTypeChip(text = "Other", isSelected = addressType == "other", onClick = { addressType = "other" }, modifier = Modifier.weight(1f))
        }

        getFieldError("addressType")?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        ValidatedAddressField(value = fullName, onValueChange = { fullName = it }, label = "Full Name *", painter = painterResource(id = R.drawable.ic_person), iconColor = appPrimary, errorMessage = getFieldError("fullName"), keyboardType = KeyboardType.Text)
        Spacer(modifier = Modifier.height(16.dp))
        ValidatedAddressField(value = phoneNumber, onValueChange = { if (it.length <= 10) phoneNumber = it }, label = "Phone Number *", painter = painterResource(id = R.drawable.ic_phone), iconColor = appPrimary, errorMessage = getFieldError("phoneNumber"), keyboardType = KeyboardType.Phone)
        Spacer(modifier = Modifier.height(16.dp))
        ValidatedAddressField(value = altPhoneNumber, onValueChange = { if (it.length <= 10) altPhoneNumber = it }, label = "Alternative Phone Number (Optional)", painter = painterResource(id = R.drawable.ic_phone), iconColor = appPrimary, errorMessage = getFieldError("altPhoneNumber"), keyboardType = KeyboardType.Phone)
        Spacer(modifier = Modifier.height(16.dp))
        ValidatedAddressField(value = houseNo, onValueChange = { houseNo = it }, label = "House no / Flat no *", painter = painterResource(id = R.drawable.solar_home_broken), iconColor = appPrimary, errorMessage = getFieldError("houseNo"))
        Spacer(modifier = Modifier.height(16.dp))
        ValidatedAddressField(value = locality, onValueChange = { locality = it }, label = "Locality / Area *", painter = painterResource(R.drawable.solar_home_broken), iconColor = appPrimary, errorMessage = getFieldError("locality"))
        Spacer(modifier = Modifier.height(16.dp))
        ValidatedAddressField(value = landmark, onValueChange = { landmark = it }, label = "Additional Landmark (Optional)", painter = painterResource(R.drawable.solar_home_broken), iconColor = appPrimary, errorMessage = getFieldError("landmark"))
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ValidatedAddressField(value = city, onValueChange = { city = it }, label = "City *", painter = painterResource(R.drawable.iconoir_city), iconColor = appPrimary, modifier = Modifier.weight(1f), errorMessage = getFieldError("city"))
            ValidatedAddressField(value = district, onValueChange = { district = it }, label = "District *", painter = painterResource(R.drawable.iconoir_city), iconColor = appPrimary, modifier = Modifier.weight(1f), errorMessage = getFieldError("district"))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ValidatedAddressField(value = state, onValueChange = { state = it }, label = "State *", painter = painterResource(R.drawable.iconoir_city), iconColor = appPrimary, modifier = Modifier.weight(1f), errorMessage = getFieldError("state"))
            ValidatedAddressField(value = pincode, onValueChange = { if (it.length <= 6) pincode = it }, label = "Pincode *", painter = painterResource(R.drawable.iconoir_city), iconColor = appPrimary, modifier = Modifier.weight(1f), errorMessage = getFieldError("pincode"), keyboardType = KeyboardType.Number)
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                validationErrors = validateForm()
                showValidationErrors = true
                if (validationErrors.isEmpty()) {
                    val addressDetails = AddressDetails(addressId = addressToEdit?.addressId, fullName = fullName.trim(), phoneNumber = phoneNumber.trim(), altPhoneNumber = altPhoneNumber.trim(), houseNo = houseNo.trim(), locality = locality.trim(), city = city.trim(), district = district.trim(), state = state.trim(), pincode = pincode.trim(), addressType = addressType, landmark = landmark.trim())
                    onSaveAddress(addressDetails)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = appPrimary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text(text = if (isEditMode) "Update Address" else "Save Address", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AddressTypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) appPrimary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ValidatedAddressField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    painter: Painter,
    iconColor: Color,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(appPrimary.copy(alpha = 0.1f))
            ) {
                Image(
                    painter = painter,
                    contentDescription = label,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(iconColor)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = if (errorMessage != null) MaterialTheme.colorScheme.error else appPrimary,
                    unfocusedBorderColor = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                isError = errorMessage != null
            )
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 48.dp, top = 4.dp)
            )
        }
    }
}