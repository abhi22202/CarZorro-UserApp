package com.example.carzorrouserside.ui.theme.screens.booking.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import com.example.carzorrouserside.ui.theme.navigation.Routes

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.viewmodel.booking.PostBookingViewModel
import com.example.carzorrouserside.util.Resource

@Composable
fun UtilityConnectionDialog(
//    serviceType: String,
//    featureServiceId: String?,
//    dateTime: String?,
//    amount: Double?,
//    addressId: String?,
//    vehicleId: String,
    onDismiss: () -> Unit
) {
    val viewModel: PostBookingViewModel = hiltViewModel()

    // Collect values directly from ViewModel
    val serviceType by viewModel.serviceType.collectAsState()
    val featureServiceId by viewModel.featureServiceId.collectAsState()
    val dateTime by viewModel.dateTime.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val addressId by viewModel.addressId.collectAsState()
    LaunchedEffect(addressId) {
        Log.d("BookingDebug", "✅ UI observes updated addressId = $addressId")
    }
    LaunchedEffect(amount) {
        Log.d("BookingDebug", "✅ UI observes updated amount = $amount")
    }
    val vehicleId by viewModel.vehicleId.collectAsState()
    val waterAvailable by viewModel.waterAvailable.collectAsState()
    val electricityAvailable by viewModel.electricityAvailable.collectAsState()
    
    var waterSelected by remember { mutableStateOf(waterAvailable) }
    var electricitySelected by remember { mutableStateOf(electricityAvailable) }
    
    // Sync local state with ViewModel state
    LaunchedEffect(waterAvailable) {
        waterSelected = waterAvailable
    }
    LaunchedEffect(electricityAvailable) {
        electricitySelected = electricityAvailable
    }

    val bookingState by viewModel.bookingStartState.collectAsState()
    val context = LocalContext.current


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Would you be able to provide a water and electricity connection up to 50 meters?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ WATER
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            waterSelected = !waterSelected
                            viewModel.setWaterAvailable(waterSelected)
                        },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (waterSelected) appPrimary else MaterialTheme.colorScheme.outline
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (waterSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Water",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (waterSelected) appPrimary else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (waterSelected) appPrimary else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (waterSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ ELECTRICITY
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            electricitySelected = !electricitySelected
                            viewModel.setElectricityAvailable(electricitySelected)
                        },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (electricitySelected) appPrimary else MaterialTheme.colorScheme.outline
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (electricitySelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Electricity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (electricitySelected) appPrimary else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (electricitySelected) appPrimary else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (electricitySelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ SUBMIT BUTTON
                Button(
                    onClick = {
                        Log.d("BookingDebug", "serviceType = $serviceType")
                        Log.d("BookingDebug", "featureServiceId = $featureServiceId")
                        Log.d("BookingDebug", "dateTime = $dateTime")
                        Log.d("BookingDebug", "amount = $amount")
                        Log.d("BookingDebug", "addressId = $addressId")
                        Log.d("BookingDebug", "vehicleId = $vehicleId")
                        Log.d("BookingDebug", "waterAvailable = ${viewModel.waterAvailable.value}")
                        Log.d("BookingDebug", "electricityAvailable = ${viewModel.electricityAvailable.value}")
                        
                        // ✅ Verify addressId from ViewModel directly
                        val viewModelAddressId = viewModel.addressId.value
                        Log.d("BookingDebug", "📍 ViewModel addressId.value = $viewModelAddressId")
                        Log.d("BookingDebug", "📍 Collected addressId state = $addressId")


                        // ✅ Step 2: Check if all booking data is filled
                        if (featureServiceId.isNullOrEmpty() ||
                            dateTime.isNullOrEmpty() ||
                            amount.isNullOrEmpty() ||
                            addressId.isNullOrEmpty() ||
                            vehicleId.isNullOrEmpty()
                        ) {
                            val missingFields = mutableListOf<String>()
                            if (featureServiceId.isNullOrEmpty()) missingFields.add("Service")
                            if (dateTime.isNullOrEmpty()) missingFields.add("Date/Time")
                            if (amount.isNullOrEmpty()) missingFields.add("Amount")
                            if (addressId.isNullOrEmpty()) missingFields.add("Address")
                            if (vehicleId.isNullOrEmpty()) missingFields.add("Vehicle")
                            
                            Log.e("BookingDebug", "❌ Missing fields: ${missingFields.joinToString(", ")}")
                            Toast.makeText(context, "Please fill all fields: ${missingFields.joinToString(", ")}", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // ✅ Step 3: Proceed with booking
                        Log.d("Booking", "Address ID = ${viewModel.addressId.value}")
                        Log.d("Booking", "Address ID (Int conversion) = ${viewModel.addressId.value?.toIntOrNull()}")
                        Log.d("Booking", "serviceType = $serviceType")
                        val argsJson = """{"isRequestSending":true}""" // you can add more fields if needed
                        viewModel.savePendingBookingToPrefs(
                            pendingBookingId = 0,                     // use real booking id if available later
                            route = Routes.BOOKING_SCREEN,            // route to restore
                            routeArgsJson = argsJson,
                            sheetType = "SET_PRICE"                   // keep same sheet type you want restored
                        )
                        viewModel.startBooking()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(appPrimary),
                    enabled = bookingState !is Resource.Loading
                ) {
                    if (bookingState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Please wait…")
                    } else {
                        Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // ✅ Handle API Result
                when (val state = bookingState) {
                    is Resource.Success -> {
                        LaunchedEffect(state) {
                            Toast.makeText(
                                context,
                                state.data?.message ?: "Booking started!",
                                Toast.LENGTH_SHORT
                            ).show()
                           // postBookingViewModel.clearBookingStartState()
                            onDismiss()
                        }
                    }
                    is Resource.Error -> {
                        LaunchedEffect(state.message) {
                            Toast.makeText(
                                context,
                                state.message ?: "Booking failed",
                                Toast.LENGTH_LONG
                            ).show()
                           // postBookingViewModel.clearBookingStartState()
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}
