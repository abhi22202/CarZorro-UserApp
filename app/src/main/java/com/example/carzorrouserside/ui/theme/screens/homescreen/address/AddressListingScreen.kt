package com.example.carzorrouserside.ui.theme.screens.homescreen.address

import android.R.attr.id
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.carzorrouserside.data.model.homescreen.AddressItem
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.viewmodel.booking.PostBookingViewModel
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.AddressViewModel
import com.example.carzorrouserside.util.Resource
import com.example.carzorrouserside.util.SnackbarManager
import com.example.carzorrouserside.util.SnackbarPosition
import com.example.carzorrouserside.util.rememberSnackbarState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddressListingScreen(
    navController: NavController,
    addressViewModel: AddressViewModel = hiltViewModel()
) {
    val addressListState by addressViewModel.addressListState.observeAsState()
    val currentAddressList by addressViewModel.currentAddressList.observeAsState(emptyList())
    val addAddressState by addressViewModel.addAddressState.observeAsState()
    val editAddressState by addressViewModel.editAddressState.observeAsState()
    val deleteAddressState by addressViewModel.deleteAddressState.observeAsState()
    val snackbarEvent by addressViewModel.snackbarEvent.observeAsState()
    val viewModel: PostBookingViewModel = hiltViewModel()

    val snackbarState = rememberSnackbarState()

    LaunchedEffect(snackbarEvent) {
        snackbarEvent?.let { event ->
            snackbarState.handleSnackbarEvent(event)
            addressViewModel.clearSnackbarEvent()
        }
    }

    LaunchedEffect(Unit) {
        addressViewModel.loadAddressList()
    }

    LaunchedEffect(addAddressState, editAddressState, deleteAddressState) {
        if (addAddressState is Resource.Success || addAddressState is Resource.Error) {
            addressViewModel.clearAddAddressState()
        }
        if (editAddressState is Resource.Success || editAddressState is Resource.Error) {
            addressViewModel.clearEditAddressState()
        }
        if (deleteAddressState is Resource.Success || deleteAddressState is Resource.Error) {
            addressViewModel.clearDeleteAddressState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Saved Addresses",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { addressViewModel.showBottomSheet() },
                    containerColor = appPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Address"
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (val state = addressListState) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = appPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading addresses...",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(
                                    text = "Failed to load addresses",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message ?: "Unknown error occurred",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { addressViewModel.retryLoadAddressList() },
                                    colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
                                ) {
                                    Text("Retry", color = Color.White)
                                }
                            }
                        }
                    }

                    is Resource.Success -> {
                        if (currentAddressList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "No addresses",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No saved addresses",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Add your first address to get started",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { addressViewModel.showBottomSheet() },
                                        colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add Address")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(currentAddressList, key = { it.id }) { address ->
                                    SwipeableAddressListItem(
                                        address = address,
                                        onAddressClick = { selectedAddress ->
                                            // Persist the selected address
                                            addressViewModel.selectAddressForHome(selectedAddress)
                                            // Save both fields in savedStateHandle
                                            navController.previousBackStackEntry?.savedStateHandle?.apply {
                                                set("selectedAddress", selectedAddress.fullAddress)
                                                set("selectedAddressId", selectedAddress.id.toString())
                                            }
                                            navController.popBackStack()
                                        },
                                        onEditClick = { addressToEdit ->
                                            addressViewModel.showEditBottomSheet(addressToEdit)
                                        },
                                        onDeleteClick = { addressToDelete ->
                                            addressViewModel.deleteAddress(addressToDelete)
                                        },
                                        isDeleting = deleteAddressState is Resource.Loading
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                    null -> {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }

        AddressBottomSheet(
            viewModel = addressViewModel,
            onDismiss = { addressViewModel.hideBottomSheet() },
            onAddressAdded = {}
        )

        SnackbarManager(
            showError = snackbarState.showError,
            showSuccess = snackbarState.showSuccess,
            showInfo = snackbarState.showInfo,
            showWarning = snackbarState.showWarning,
            errorMessage = snackbarState.errorMessage,
            successMessage = snackbarState.successMessage,
            infoMessage = snackbarState.infoMessage,
            warningMessage = snackbarState.warningMessage,
            position = SnackbarPosition.TOP,
            onDismissError = { snackbarState.hideError() },
            onDismissSuccess = { snackbarState.hideSuccess() },
            onDismissInfo = { snackbarState.hideInfo() },
            onDismissWarning = { snackbarState.hideWarning() }
        )
    }
}

@Composable
fun SwipeableAddressListItem(
    address: AddressItem,
    onAddressClick: (AddressItem) -> Unit,
    onEditClick: (AddressItem) -> Unit,
    onDeleteClick: (AddressItem) -> Unit,
    isDeleting: Boolean = false
) {
    val density = LocalDensity.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    val actionButtonWidth = 60.dp
    val actionButtonWidthPx = with(density) { actionButtonWidth.toPx() }
    val maxSwipeDistance = actionButtonWidthPx * 2.5f
    val viewModel: PostBookingViewModel = hiltViewModel()

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = actionButtonWidth, height = 50.dp)
                    .padding(horizontal = 4.dp)
                    .background(Color(0xFF2196F3), RoundedCornerShape(8.dp))
                    .clickable(enabled = !isDeleting) {
                        onEditClick(address)
                        offsetX = 0f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(width = actionButtonWidth, height = 50.dp)
                    .padding(horizontal = 4.dp)
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp))
                    .clickable(enabled = !isDeleting) {
                        onDeleteClick(address)
                        offsetX = 0f
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(address.id) {
                    detectDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX < -maxSwipeDistance * 0.5f) {
                                -maxSwipeDistance
                            } else {
                                0f
                            }
                        }
                    ) { _, dragAmount ->
                        if (!isDeleting) {
                            val newOffset = offsetX + dragAmount.x
                            offsetX = newOffset.coerceIn(-maxSwipeDistance, 0f)
                        }
                    }
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AddressListItem(
                address = address,
                onAddressClick = {
                    if (offsetX != 0f) {
                        offsetX = 0f
                    } else if (!isDeleting) {
                        onAddressClick(address)
                       // viewModel.setAddressId(address.id)
                    }
                }
            )
        }
    }
}

@Composable
fun AddressListItem(
    address: AddressItem,
    onAddressClick: (AddressItem) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddressClick(address) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(appPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = appPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = address.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = appPrimary,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = "DEFAULT",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "+91 ${address.phoneNumber}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = address.fullAddress,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                if (address.city.isNotBlank() || address.pincode.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${address.city} - ${address.pincode}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}