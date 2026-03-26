package com.example.carzorrouserside.ui.theme.screens.cardetails

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.R
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.ui.theme.viewmodel.CarDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    navController: NavController,
    viewModel: CarDetailsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        if (uiState.errorMessage != null) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearErrorMessage()
        }
        if (uiState.successMessage != null) {
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            try {
                // Get the HOME_SCREEN entry directly and set the flag
                val homeEntry = navController.getBackStackEntry(Routes.HOME_SCREEN)
                homeEntry.savedStateHandle.set("showCarSelectionSheet", true)
                
                // Pop back to HOME_SCREEN
                navController.popBackStack(Routes.HOME_SCREEN, false)
            } catch (e: Exception) {
                // Fallback: use previousBackStackEntry if HOME_SCREEN entry not found
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("showCarSelectionSheet", true)
                navController.popBackStack(Routes.HOME_SCREEN, false)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.screenTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = uiState.imageUrl,
                contentDescription = "${uiState.brandName} ${uiState.modelName}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${uiState.brandName} ${uiState.modelName}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = viewModel.vehicleNumber,
                onValueChange = { viewModel.onVehicleNumberChange(it) },
                label = { Text("Enter Your Vehicle Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appPrimary,
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Car Fuel Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FuelTypeCard(
                        text = "Petrol",
                        iconRes = R.drawable.petrol,
                        isSelected = viewModel.selectedFuelType == "Petrol",
                        onClick = { viewModel.onFuelTypeSelected("Petrol") },
                        modifier = Modifier.weight(1f)
                    )
                    FuelTypeCard(
                        text = "Diesel",
                        iconRes = R.drawable.petrol,
                        isSelected = viewModel.selectedFuelType == "Diesel",
                        onClick = { viewModel.onFuelTypeSelected("Diesel") },
                        modifier = Modifier.weight(1f)
                    )
                    FuelTypeCard(
                        text = "CNG",
                        iconRes = R.drawable.cng,
                        isSelected = viewModel.selectedFuelType == "CNG",
                        onClick = { viewModel.onFuelTypeSelected("CNG") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FuelTypeCard(
                        text = "Electric",
                        iconRes = R.drawable.logo,
                        isSelected = viewModel.selectedFuelType == "Electric",
                        onClick = { viewModel.onFuelTypeSelected("Electric") },
                        modifier = Modifier.weight(1f)
                    )
                    FuelTypeCard(
                        text = "Hybrid",
                        iconRes = R.drawable.logo,
                        isSelected = viewModel.selectedFuelType == "Hybrid",
                        onClick = { viewModel.onFuelTypeSelected("Hybrid") },
                        modifier = Modifier.weight(1f)
                    )
                    FuelTypeCard(
                        text = "LPG",
                        iconRes = R.drawable.logo,
                        isSelected = viewModel.selectedFuelType == "LPG",
                        onClick = { viewModel.onFuelTypeSelected("LPG") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FuelTypeCard(
                        text = "LNG",
                        iconRes = R.drawable.logo,
                        isSelected = viewModel.selectedFuelType == "LNG",
                        onClick = { viewModel.onFuelTypeSelected("LNG") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }


            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveCar() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = "Done",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FuelTypeCard(
    text: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) appPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) appPrimary else MaterialTheme.colorScheme.outline

    Card(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = if (isSelected) appPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}