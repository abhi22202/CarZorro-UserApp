package com.example.carzorrouserside.ui.theme.screens.homescreen.car

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import kotlinx.coroutines.runBlocking
import com.example.carzorrouserside.data.token.BookingSessionManager

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.car.CarListingItem
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.components.ShowAuthenticationModal
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.ui.viewmodel.CarListingViewModel
import com.google.gson.Gson
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarSelectionBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCarSelected: (CarListingItem) -> Unit,
    navController: NavController,
    viewModel: CarListingViewModel = hiltViewModel()
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(key1 = isVisible) {
        if (isVisible) {
            viewModel.fetchCars()
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            CarSelectionContent(
                onCarSelected = onCarSelected,
                onDismiss = onDismiss,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun CarItem(
    car: CarListingItem,
    onCarSelected: (CarListingItem) -> Unit,
    onEditClick: (CarListingItem) -> Unit,
    onDeleteClick: (CarListingItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCarSelected(car) },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = car.imageUrl,
                contentDescription = buildString {
                    val brand = car.brand?.takeIf { it.isNotBlank() } ?: ""
                    val model = car.model?.takeIf { it.isNotBlank() } ?: ""
                    when {
                        brand.isNotEmpty() && model.isNotEmpty() -> {
                            append(brand)
                            append(" ")
                            append(model)
                        }
                        brand.isNotEmpty() -> append(brand)
                        model.isNotEmpty() -> append(model)
                        else -> append("Car")
                    }
                },
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo),
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = buildString {
                        val brand = car.brand?.takeIf { it.isNotBlank() }
                        val model = car.model?.takeIf { it.isNotBlank() }
                        
                        when {
                            brand != null && model != null -> {
                                append(brand)
                                append(" ")
                                append(model)
                            }
                            brand != null -> append(brand)
                            model != null -> append(model)
                            else -> append("Car")
                        }
                    },
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = car.fuelType,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { onEditClick(car) }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { onDeleteClick(car) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun AddVehicleButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Vehicle",
                tint = appPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Vehicle",
                color = appPrimary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun CarSelectionContent(
    onCarSelected: (CarListingItem) -> Unit,
    onDismiss: () -> Unit,
    navController: NavController,
    viewModel: CarListingViewModel
) {
    val uiState by viewModel.carsState.collectAsState()
    val context = LocalContext.current

    // ✅ Access UserPreferencesManager directly
    val userPreferencesManager = remember {
        UserPreferencesManager(context.applicationContext)
    }

    // ✅ Booking session manager (used later if needed)
    val bookingSessionManager = remember {
        BookingSessionManager(context.applicationContext, Gson())
    }

    // ✅ Check if user is logged in
    val isLoggedIn = remember {
        userPreferencesManager.isLoggedIn() && userPreferencesManager.getJwtToken() != null
    }

    // Show authentication modal if not logged in
    ShowAuthenticationModal(
        navController = navController,
        userPreferencesManager = userPreferencesManager,
        showModal = !isLoggedIn,
        onDismiss = {
            onDismiss()
        }
    )

    // 🚫 Stop showing bottom sheet if not logged in
    if (!isLoggedIn) {
        return
    }

    // ✅ Show normal UI only when logged in
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(min = 250.dp, max = 500.dp)
    ) {
        Text(
            text = "Select Car",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.cars.isEmpty() -> {
                    Text(
                        text = "No cars found. Please add a vehicle.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn {
                        items(uiState.cars) { car ->
                            CarItem(
                                car = car,
                                onCarSelected = {
                                    onCarSelected(it)
                                    onDismiss()
                                },
                                onEditClick = { selectedCar ->
                                    onDismiss()
                                    navController.navigate("${Routes.BRAND_SELECTION_SCREEN}?carId=${selectedCar.id}")
                                },
                                onDeleteClick = {
                                    // Optional: add confirmation before delete
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AddVehicleButton(
            onClick = {
                onDismiss()
                navController.navigate(Routes.BRAND_SELECTION_SCREEN)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}





@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun CarSelectionContentPreview() {
    CarZorroColors {
        Surface(color = MaterialTheme.colorScheme.surface) {

            CarSelectionContent(
                onCarSelected = {},
                onDismiss = {},
                navController = rememberNavController(),
                viewModel = hiltViewModel()
            )
        }
    }
}

@Composable
fun CarZorroColors(content: @Composable () -> Unit) {
    TODO("Not yet implemented")
}