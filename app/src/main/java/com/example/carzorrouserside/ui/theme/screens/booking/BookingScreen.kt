package com.example.carzorrouserside.ui.theme.screens.booking

import android.app.DatePickerDialog
import android.os.Build
import androidx.compose.animation.slideInVertically

import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.SnackbarType
import com.example.carzorrouserside.data.model.VendorResponse
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.ui.theme.screens.booking.components.PriceBottomSheet
import com.example.carzorrouserside.ui.theme.screens.booking.components.UtilityConnectionDialog
import com.example.carzorrouserside.ui.theme.screens.booking.components.VendorResponseCard
import com.example.carzorrouserside.ui.theme.screens.componenets.AnimatedSnackbar
import com.example.carzorrouserside.ui.theme.screens.componenets.CancelRequestConfirmation
import com.example.carzorrouserside.ui.theme.screens.homescreen.car.CarSelectionBottomSheet
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.HomepageServiceViewModel
import com.example.carzorrouserside.ui.viewmodel.CarListingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.DisposableEffect
import kotlin.math.max
import kotlin.math.min

import android.app.TimePickerDialog // <-- ADDED Import
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.screens.homescreen.UserBidCard
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar // <-- ADDED Import
import com.example.carzorrouserside.ui.theme.viewmodel.booking.PostBookingViewModel
import com.example.carzorrouserside.util.Resource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingScreen(
    navController: NavController,
    visibleResponses: List<VendorResponse>,
    onAccept: (VendorResponse) -> Unit,
    onDecline: (VendorResponse) -> Unit,


) {
    val postBookingViewModel: PostBookingViewModel = hiltViewModel()
    //var currentSheet by remember { mutableStateOf(SheetType.BOOKING) }
    var selectedVehicleId by remember { mutableStateOf<Int?>(null) }
    var sheetExpanded by remember { mutableStateOf(false) }
    var showUtilityDialog by remember { mutableStateOf(false) }
    //var isRequestSending by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("Request cancelled successfully") }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }
    var showCarSelectionSheet by remember { mutableStateOf(false) }
    val selectedVehicle by postBookingViewModel.selectedVehicle.collectAsState()
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val homepageServiceViewModel: HomepageServiceViewModel = hiltViewModel()
    val uiState by homepageServiceViewModel.uiState.collectAsState()

    // Handle booking status changes and navigate when vendor accepts
    LaunchedEffect(uiState.bookingStatus) {
        when (uiState.bookingStatus?.lowercase()) {
            "created", "bidding" -> {
                Log.d("BookingScreen", "📍 Staying on current screen (status=${uiState.bookingStatus})")
            }

            "accepted", "vendor_accepted" -> {
                Log.d("BookingScreen", "🚀 Booking accepted by vendor — navigating to summary screen")
                
                // Get booking ID from active booking data
                val bookingId = uiState.activeBookingData?.booking?.id
                
                if (bookingId != null && bookingId > 0) {
                    Log.d("BookingScreen", "✅ Navigating with booking ID: $bookingId")
                    navController.navigate(Routes.bookingSummaryScreenWithBookingId(bookingId)) {
                        popUpTo(Routes.BOOKING_SCREEN) { inclusive = true }
                    }
                } else {
                    Log.w("BookingScreen", "⚠️ Booking ID not found, using vendor ID fallback")
                    val vendorId = uiState.activeBids.firstOrNull()?.vendorId ?: 0
                    navController.navigate(Routes.bookingSummaryScreen(vendorId)) {
                        popUpTo(Routes.BOOKING_SCREEN) { inclusive = true }
                    }
                }
            }

            else -> {
                Log.d("BookingScreen", "ℹ️ Unknown status — ${uiState.bookingStatus}")
            }
        }
    }

    // Listen for FCM broadcast when vendor accepts booking
    val broadcastReceiver = remember {
        object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                val notificationType = intent?.getStringExtra("type")
                val bookingIdString = intent?.getStringExtra("bookingId")
                
                Log.d("BookingScreen", "📨 Received broadcast - Type: $notificationType, BookingId: $bookingIdString")
                
                if (notificationType == com.example.carzorrouserside.util.AppConstants.NOTIFICATION_TYPE_BOOKING_ACCEPTED) {
                    Log.d("BookingScreen", "✅ Vendor accepted booking - refreshing active booking")
                    
                    // Refresh active booking to get latest status
                    val userId = userPreferencesManager.getUserId()
                    val token = userPreferencesManager.getJwtToken()
                    
                    if (userId != null && token != null) {
                        homepageServiceViewModel.checkActiveBooking(userId, token)
                    }
                    
                    // Navigate after a short delay to allow state update
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500)
                        
                        val bookingId = bookingIdString?.toIntOrNull()
                        if (bookingId != null && bookingId > 0) {
                            Log.d("BookingScreen", "🚀 Navigating to booking summary with ID: $bookingId")
                            navController.navigate(Routes.bookingSummaryScreenWithBookingId(bookingId)) {
                                popUpTo(Routes.BOOKING_SCREEN) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        val filter = android.content.IntentFilter(com.example.carzorrouserside.util.AppConstants.ACTION_BOOKING_UPDATE)
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context)
            .registerReceiver(broadcastReceiver, filter)
        
        onDispose {
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(broadcastReceiver)
        }
    }






    var fare by remember { mutableStateOf("500") }
    val serviceType by postBookingViewModel.serviceType.collectAsState()
    val bookingState by postBookingViewModel.bookingStartState.collectAsState()

    val collapsedHeight = 120.dp
    val expandedHeight = 750.dp

//    val allVendorResponses = remember {
//        mutableStateListOf(
//            VendorResponse(1, "Santosh Sarma", 4.5, 125, "Deluxe Cleaning", 120, 2, 100),
//            VendorResponse(2, "Rahul Verma", 4.2, 98, "Standard Cleaning", 100, 5, 300),
//            VendorResponse(3, "Amit Kumar", 4.7, 210, "Premium Service", 150, 10, 600),
//            VendorResponse(4, "Priya Singh", 4.8, 175, "Deluxe Cleaning", 130, 3, 200),
//            VendorResponse(5, "Vikram Patel", 4.0, 85, "Basic Cleaning", 90, 7, 450)
//        )
//    }

    val visibleResponses = remember { mutableStateListOf<VendorResponse>() }
    var currentResponseIndex by remember { mutableIntStateOf(0) }
    // BookingScreen top: read nav args
    val backEntry = navController.currentBackStackEntry

    val sheetArg = backEntry
        ?.arguments
        ?.getString("sheet")
        ?: "BOOKING"

// read boolean safely; will be false if not present (because NavArgument defaultValue=false)
    val isSendingArg = backEntry
        ?.arguments
        ?.getBoolean("isSending", false) ?: false

    val initialSheet = when (sheetArg.uppercase()) {
        "SET_PRICE", "PRICE" -> SheetType.SET_PRICE
        else -> SheetType.BOOKING
    }

    var currentSheet by remember { mutableStateOf(initialSheet) }
// initialize isRequestSending from nav arg
    var isRequestSending by remember { mutableStateOf(isSendingArg) }

    LaunchedEffect(Unit) {
        val userId = userPreferencesManager.getUserId()
        val token = userPreferencesManager.getJwtToken()
        if (userId != null && token != null) {
            homepageServiceViewModel.checkActiveBooking(userId, token)
            Log.d("BookingScreen", "✅ checkActiveBooking() triggered in BookingScreen")
        } else {
            Log.w("BookingScreen", "⚠️ Missing userId or token — cannot fetch active booking")
        }
    }



//    LaunchedEffect(isRequestSending) {
//        if (isRequestSending) {
//            delay(5000)
//            while (isRequestSending) {
//                if (visibleResponses.size < 2) {
//                    val newResponse = allVendorResponses[currentResponseIndex]
//                    visibleResponses.add(newResponse)
//                    currentResponseIndex = (currentResponseIndex + 1) % allVendorResponses.size
//                } else {
//                    visibleResponses.removeAt(0)
//                    val newResponse = allVendorResponses[currentResponseIndex]
//                    visibleResponses.add(newResponse)
//                    currentResponseIndex = (currentResponseIndex + 1) % allVendorResponses.size
//                }
//                delay(6000)
//            }
//        } else {
//            visibleResponses.clear()
//        }
//    }

    val sheetHeight by animateDpAsState(
        targetValue = if (sheetExpanded) expandedHeight else collapsedHeight,
        label = "sheetHeight"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FullMapSection(
            selectedServiceType = serviceType,
            onServiceTypeSelected ={ newType ->
                postBookingViewModel.setServiceType(
                    when (newType) {
                        "Door Service" -> "doorstep"
                        "Pick up" -> "pickup"
                        "Self Visit" -> "self_visit"
                        else -> "doorstep"
                    }
                )
            },
            onBackClick = { navController.popBackStack() }
        )
        // ✅ Show real active bids (rebid cards) from HomepageServiceViewModel
        if (uiState.hasRebid && uiState.activeBids.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 90.dp)
                    .padding(horizontal = 16.dp)
                    .zIndex(10f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val context = LocalContext.current
                uiState.activeBids.forEach { bid ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { -150 }) + fadeIn(),
                        exit = fadeOut()
                    ) {
                        UserBidCard(
                            bid = bid,
                            onAccept = { selectedBid ->
                                // Call the handler which will trigger ViewModel method
                                onAccept(VendorResponse.fromBid(selectedBid))
                            },
                            onReject = { selectedBid ->
                                // Call the handler which will trigger ViewModel method
                                onDecline(VendorResponse.fromBid(selectedBid))
                            }
                        )
                    }
                }
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            when (currentSheet) {
                SheetType.BOOKING -> {
                    PersistentBottomSheet(
                        navController = navController,
                        selectedVehicleId = selectedVehicleId,
                        sheetHeight = sheetHeight,
                        collapsedHeight = collapsedHeight,
                        onSheetStateChanged = { expanded ->
                            sheetExpanded = expanded
                        },
                        onFindVendorsClick = {
                            // Validate before opening PriceBottomSheet
                            val isVehicleSelected = postBookingViewModel.vehicleId.value != null
                            val isAddressSelected = postBookingViewModel.addressId.value != null
                            val selectedDate = postBookingViewModel.selectedDate.value
                            val selectedTime = postBookingViewModel.selectedTime.value
                            
                            // Check if dateTime is set, or if date and time are selected individually
                            val isDateTimeSelected = postBookingViewModel.dateTime.value != null || 
                                (selectedDate != null && selectedTime != null)
                            
                            // If date and time are selected but dateTime is not set, set it now
                            if (selectedDate != null && selectedTime != null && postBookingViewModel.dateTime.value == null) {
                                postBookingViewModel.setDateTime(selectedDate, selectedTime)
                            }
                            
                            val isServiceSelected = postBookingViewModel.featureServiceId.value != null

                            if (!isVehicleSelected || !isAddressSelected || !isDateTimeSelected || !isServiceSelected) {
                                snackbarMessage = "Please fill all details before proceeding"
                                snackbarType = SnackbarType.ERROR
                                showSnackbar = true
                            } else {
                                currentSheet = SheetType.SET_PRICE
                            }
                        },

                        selectedVehicle = selectedVehicle,
                        onSelectVehicleClick = { showCarSelectionSheet = true },
                        fare = fare,
                        onFareChange = { fare = it },
                        onShowUtilityDialog = { showUtilityDialog = true }
                    )
                }
                SheetType.SET_PRICE -> {
                    PriceBottomSheet(
                        onCloseClick = {
                            currentSheet = SheetType.BOOKING
                            isRequestSending = false
                        },
                        onFindVendorsClick = {
                            when (serviceType) {
                                "doorstep" -> showUtilityDialog = true
                                "pickup", "self_visit" -> {
                                    // Save pending using ViewModel (no LocalContext)
                                    val argsJson = """{"isRequestSending":true}"""
//                                    postBookingViewModel.savePendingBookingToPrefs(
//                                        pendingBookingId = 0,
//                                        route = Routes.BOOKING_SCREEN,
//                                        routeArgsJson = argsJson,
//                                        sheetType = "SET_PRICE"
//                                    )


                                    postBookingViewModel.startBooking()
                                }
                            } },


                        isRequestSending = isRequestSending,
                        onCancelRequest = {
                        },
                        onShowCancelConfirmation = {
                            showCancelConfirmation = true
                        },
                        initialPrice = fare,
                        homepageServiceViewModel = homepageServiceViewModel
                    )
                }

            }
            when (val state = bookingState) {
                is Resource.Loading -> {
                    // optional: show loading spinner if you like
                }

                is Resource.Success -> {
                    LaunchedEffect(state) {

                        Toast.makeText(
                            context,
                            state.data?.message ?: "Booking started!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // ✅ Now we mark that booking has started
                        isRequestSending = true

                        // Optional cleanup if your VM has one
                        // postBookingViewModel.clearBookingStartState()
                    }
                }

                is Resource.Error -> {
                    LaunchedEffect(state.message) {
                        val cleanMessage = try {
                            val raw = state.message ?: "Booking failed"

                            // Extract the JSON part if your message includes "→ {...}"
                            val jsonPart = if (raw.contains("→")) {
                                raw.substringAfter("→").trim()
                            } else {
                                raw
                            }

                            // Try to parse only the message field from the JSON
                            if (jsonPart.startsWith("{")) {
                                val json = org.json.JSONObject(jsonPart)
                                json.optString("message", raw)
                            } else {
                                raw
                            }
                        } catch (e: Exception) {
                            "Booking failed"
                        }

                        Toast.makeText(
                            context,
                            cleanMessage,
                            Toast.LENGTH_LONG
                        ).show()

                        // ✅ Prevent transition on error
                        isRequestSending = false
                    }
                }


                else -> Unit
            }
        }

        if (showCancelConfirmation) {
            CancelRequestConfirmation(
                onKeepSearching = {
                    showCancelConfirmation = false
                },
                onCancelRequest = {
                    showCancelConfirmation = false
                    currentSheet = SheetType.BOOKING
                    isRequestSending = false
                    snackbarMessage = "Request cancelled successfully"
                    snackbarType = SnackbarType.SUCCESS
                    showSnackbar = true
                }
            )
        }

        if (showUtilityDialog) {
            UtilityConnectionDialog(
                onDismiss = { showUtilityDialog = false }
            )
        }

        AnimatedSnackbar(
            visible = showSnackbar,
            message = snackbarMessage,
            type = snackbarType,
            onDismiss = { showSnackbar = false }
        )

        CarSelectionBottomSheet(
            isVisible = showCarSelectionSheet,
            onDismiss = { showCarSelectionSheet = false },
            onCarSelected =  { car ->
                val displayName = car.registrationNumber ?: run {
                    val brand = car.brand?.takeIf { it.isNotBlank() } ?: ""
                    val model = car.model?.takeIf { it.isNotBlank() } ?: ""
                    when {
                        brand.isNotEmpty() && model.isNotEmpty() -> "$brand $model"
                        brand.isNotEmpty() -> brand
                        model.isNotEmpty() -> model
                        else -> "Car"
                    }
                }
                postBookingViewModel.setSelectedVehicle(displayName)
                postBookingViewModel.setVehicleId(car.id?.toString())
                showCarSelectionSheet = false
            },
            navController = navController
        )
    }
}


enum class SheetType {
    BOOKING,
    SET_PRICE
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PersistentBottomSheet(
    selectedVehicleId: Int?,
    navController: NavController,
    sheetHeight: Dp,
    collapsedHeight: Dp,
    onSheetStateChanged: (Boolean) -> Unit,
    onFindVendorsClick: () -> Unit,
    selectedVehicle: String,
    onSelectVehicleClick: () -> Unit,
    fare: String,
    onFareChange: (String) -> Unit,
    onShowUtilityDialog: () -> Unit
) {
    var sheetOffsetY by remember { mutableStateOf(0f) }
    var isExpanded by remember { mutableStateOf(true) }
    val postBookingViewModel: PostBookingViewModel = hiltViewModel()

    LaunchedEffect(isExpanded) {
        onSheetStateChanged(isExpanded)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        isExpanded = sheetOffsetY < -50f
                        sheetOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        sheetOffsetY = max(-200f, min(200f, sheetOffsetY + dragAmount.y))
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            CollapsedSheetContent(
                onExpandClick = { isExpanded = true }
            )

            if (sheetHeight > collapsedHeight) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                ExpandedSheetContent(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    onFindVendorsClick = {
                        Log.d("BookingScreen", "✅ Find Vendors clicked (BottomSheet)")
                        // ✅ Delegate to parent BookingScreen
                        onFindVendorsClick()
                    },

                    selectedVehicle = selectedVehicle,
                    onSelectVehicleClick = onSelectVehicleClick,
                    fare = fare,
                    onFareChange = onFareChange
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedSheetContent(
    navController: NavController,
    viewModel: HomepageServiceViewModel = hiltViewModel(),
    carListViewModel : CarListingViewModel = hiltViewModel(),
    postBookingViewModel: PostBookingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onFindVendorsClick: () -> Unit,
    selectedVehicle: String,
    onSelectVehicleClick: () -> Unit,
    fare: String,
    onFareChange: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val serviceOptions = listOf("Deep Cleaning", "Pick Up", "Self Wash")
    var selectedService by rememberSaveable { mutableStateOf(serviceOptions[0]) }
    var selectedAddress by remember { mutableStateOf("Select your address") }

   // var selectedTime by remember { mutableStateOf<LocalTime?>(null) } // Start with null


// RED: Calculate time limits
    val currentTime = LocalTime.now()


    // --- near top of ExpandedSheetContent, get the postBookingViewModel ---


// 1) read VM flows (strings)
    val selectedDate by postBookingViewModel.selectedDate.collectAsState()
    val selectedTime by postBookingViewModel.selectedTime.collectAsState()



// 3) only keep these local UI booleans
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

// 4) derived display string
    val formattedDateTime = remember(selectedDate, selectedTime) {
        val date = selectedDate ?: LocalDate.now()
        val time = selectedTime ?: LocalTime.now()
        val datePattern = DateTimeFormatter.ofPattern("dd / MM / yyyy")
        val timePattern = DateTimeFormatter.ofPattern("hh : mm a")
        "${date.format(datePattern)}, ${time.format(timePattern)}"
    }

    // 🧠 Observe both address and addressId from AddressListingScreen
    val selectedAddressResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("selectedAddress")?.observeAsState()

    val selectedAddressIdResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("selectedAddressId")?.observeAsState()

    LaunchedEffect(selectedAddressResult?.value, selectedAddressIdResult?.value) {
        // Update UI with the selected address
        selectedAddressResult?.value?.let {
            selectedAddress = it
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("selectedAddress")
        }
        // ✅ Update ViewModel with the selected addressId
        selectedAddressIdResult?.value?.let { id ->
            postBookingViewModel.setAddressId(id)
            Log.d("BookingDebug", "✅ BookingScreen received and set addressId = $id")
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("selectedAddressId")
        }
    }


    Column(
        modifier = modifier
    ) {
        Column(modifier = Modifier.clickable { onSelectVehicleClick() }) {
            BookingField(
                title = "Select Your Vehicle",
                value = selectedVehicle,
                iconResId = R.drawable.vechile_number,
                iconTint = MaterialTheme.colorScheme.primary,
                showChevron = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        when {
            uiState.isLoading -> {
                BookingDropdownField(
                    title = "Select Your Service",
                    selectedValue = "Loading...",
                    options = emptyList(),
                    onValueChange = {},
                    iconResId = R.drawable.deep_clean
                )
            }

            uiState.error != null && uiState.services.isEmpty() -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Failed to load services",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.retryLoadServices() },
                        colors = ButtonDefaults.buttonColors(containerColor = appPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Retry", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            uiState.services.isNotEmpty() -> {
                val serviceOptionsFromApi = uiState.services.map { it.name }
                BookingDropdownField(
                    title = "Select Your Service",
                    selectedValue = selectedService.ifEmpty { serviceOptionsFromApi.first() },
                    options = serviceOptionsFromApi,
                    onValueChange = { serviceName ->
                        selectedService = serviceName
                        val serviceId = uiState.services.firstOrNull { it.name == serviceName }?.id.toString()
                        postBookingViewModel.setFeatureServiceId(serviceId) // ✅ Save to ViewModel
//                        postBookingViewModel.setServiceType(serviceName)
                    },
                    iconResId = R.drawable.deep_clean
                )
            }

            else -> {
                BookingDropdownField(
                    title = "Select Your Service",
                    selectedValue = "No services available",
                    options = emptyList(),
                    onValueChange = {},
                    iconResId = R.drawable.deep_clean
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.clickable { showDatePicker = true }) {
            BookingField(
                title = "Select Date & Time",
                value = formattedDateTime,
                iconResId = R.drawable.calender,
                iconTint = MaterialTheme.colorScheme.primary,
                showChevron = true
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.clickable {
                navController.navigate(Routes.ADDRESS_LISTING_SCREEN)
            }
        ) {
            BookingField(
                title = "Enter Your Address",
                value = selectedAddress,
                iconResId = R.drawable.location,
                iconTint = MaterialTheme.colorScheme.primary,
                showChevron = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        FareSection(fare = fare, onFareChange = onFareChange)
        Spacer(modifier = Modifier.height(16.dp))
        AutoAcceptToggle()
        Spacer(modifier = Modifier.height(24.dp))
        val context = LocalContext.current
        BottomButtons(
            onFindVendorsClick = {
                // Validation check before proceeding
                when {
                    selectedVehicle == "Select Your Vehicle" -> {
                        Toast.makeText(context, "Please select your vehicle", Toast.LENGTH_SHORT).show()
                    }
                    selectedAddress == "Select your address" -> {
                        Toast.makeText(context, "Please select your address", Toast.LENGTH_SHORT).show()
                    }
                    selectedDate == null || selectedTime == null -> {
                        Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
                    }
                    selectedService.isEmpty() -> {
                        Toast.makeText(context, "Please select a service", Toast.LENGTH_SHORT).show()
                    }
                    fare.isBlank() || fare == "0" -> {
                        Toast.makeText(context, "Please enter a valid fare", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Ensure all ViewModel values are set before proceeding
                        // Set fare/amount in ViewModel
                        postBookingViewModel.setAmount(fare)
                        
                        // Ensure dateTime is set if date and time are selected
                        if (selectedDate != null && selectedTime != null && postBookingViewModel.dateTime.value == null) {
                            postBookingViewModel.setDateTime(selectedDate, selectedTime)
                        }
                        
                        // All good, proceed
                        onFindVendorsClick()
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
    val postBookingViewModel: PostBookingViewModel = hiltViewModel()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            // save to ViewModel
                            postBookingViewModel.setSelectedDate(date)
                        }
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    if (showTimePicker) {
        TimePickerDialog(
            selectedDate = selectedDate,
            onDismissRequest = { showTimePicker = false },
            onConfirm = { time ->
                postBookingViewModel.setSelectedTime(time)
                showTimePicker = false

                if (selectedDate != null && time != null) {
                    postBookingViewModel.setDateTime(selectedDate, time)
                }
            }
        )
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDropdownField(
    title: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    iconResId: Int
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomButtons(
    onFindVendorsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onFindVendorsClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appPrimary
            )
        ) {
            Text(
                text = "Find Vendors",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Button(
            onClick = { /* Handle chat action */ },
            modifier = Modifier
                .size(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appPrimary
            )
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Default.Chat,
                contentDescription = "Chat",
                tint = Color.White
            )
        }
    }
}


@Composable
fun CollapsedSheetContent(
    onExpandClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandClick() }
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Image(
                painter = painterResource(id = R.drawable.car_deep_clean_foreground),
                contentDescription = "Deep Clean",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Car Deep Clean",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select options to continue",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FullMapSection(
    selectedServiceType: String,
    onServiceTypeSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        IconButton(
            onClick = { onBackClick() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ServiceTypeTab(
                title = "Door Service",
                iconResId = R.drawable.door_service,
                isSelected = selectedServiceType == "doorstep",
                onClick = { onServiceTypeSelected("Door Service") }
            )
            ServiceTypeTab(
                title = "Pick up",
                iconResId = R.drawable.pick_up,
                isSelected = selectedServiceType == "pickup",
                onClick = { onServiceTypeSelected("Pick up") }
            )
            ServiceTypeTab(
                title = "Self Visit",
                iconResId = R.drawable.self_visit,
                isSelected = selectedServiceType == "self_visit",
                onClick = { onServiceTypeSelected("Self Visit") }
            )
        }

        Text(
            text = "Map Area",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ServiceTypeTab(
    title: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(36.dp)
            .widthIn(min = 100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(
                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BookingField(
    title: String,
    value: String,
    iconResId: Int,
    iconTint: Color,
    showChevron: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(iconTint)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FareSection(fare: String, onFareChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Offer Your Fare",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₹",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )

            BasicTextField(
                value = fare,
                onValueChange = { newValue ->
                    onFareChange(newValue.filter { it.isDigit() })
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )
        }
        Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your offered price is ₹${if (fare.isBlank()) "0" else fare}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AutoAcceptToggle() {
    var autoAccept by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.rocket),
            contentDescription = "Auto-accept",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Automatically accept the nearest vendors for your price",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )

        Switch(
            checked = autoAccept,
            onCheckedChange = { autoAccept = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    selectedDate: LocalDate?,
    onDismissRequest: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val timePickerState = rememberTimePickerState(is24Hour = false)
    val context = LocalContext.current
    val today = LocalDate.now()
    val now = LocalTime.now()
    val minAllowedTime = now.plusHours(2)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                TimePicker(state = timePickerState)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }

                    TextButton(
                        onClick = {
                            val selectedTime =
                                LocalTime.of(timePickerState.hour, timePickerState.minute)

                            // ✅ Case 1: If same-day booking, check time
                            if (selectedDate == today && selectedTime.isBefore(minAllowedTime)) {
                                Toast.makeText(
                                    context,
                                    "Please select a time at least 2 hours from now.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                // ✅ Case 2: Accept time
                                onConfirm(selectedTime)
                                onDismissRequest()
                            }
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun PreviewBookingScreen() {
    BookingScreen(
        navController = rememberNavController(),
        visibleResponses = emptyList(),
        onAccept = {},
        onDecline = {}
    )
}