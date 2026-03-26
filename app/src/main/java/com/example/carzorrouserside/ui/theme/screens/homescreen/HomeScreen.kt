package com.example.carzorrouserside.ui.theme.screens.homescreen

import android.os.Build
import dagger.hilt.components.SingletonComponent

import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.car.CarListingItem
import com.example.carzorrouserside.data.model.homescreen.*
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.components.ShowAuthenticationModal
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.ui.theme.screens.bottomnav.BottomNavBar
import com.example.carzorrouserside.ui.theme.screens.homescreen.address.AddressBottomSheet
import com.example.carzorrouserside.ui.theme.screens.homescreen.car.CarSelectionBottomSheet
import com.example.carzorrouserside.ui.theme.viewmodel.PackageViewModel
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.*
import com.example.carzorrouserside.util.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import kotlin.math.roundToInt


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CarWashAppScreen(
    navController: NavController,
    bannerViewModel: BannerViewModel = hiltViewModel(),
    addressViewModel: AddressViewModel = hiltViewModel(),
    homepageServiceViewModel: HomepageServiceViewModel = hiltViewModel(),
    testimonialViewModel: TestimonialViewModel = hiltViewModel(),
    popularAreaViewModel: PopularAreaViewModel = hiltViewModel(),
) {
    var currentRoute by remember { mutableStateOf(BottomNavItem.Home.route) }
    var showCarSelectionSheet by remember { mutableStateOf(false) }
    var selectedCar by remember { mutableStateOf<CarListingItem?>(null) }
    var showAuthModalForService by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val snackbarState = rememberSnackbarState()
    val selectedAddressForHome by addressViewModel.selectedAddressForHome.observeAsState()
    val bannerUiState by bannerViewModel.uiState.collectAsState()
    val showAddressSheet by addressViewModel.showBottomSheet.observeAsState(false)
    val addAddressState by addressViewModel.addAddressState.observeAsState()
    val userPref = EntryPointAccessors.fromApplication(
        context.applicationContext,
        UserPrefEntryPoint::class.java
    ).userPreferencesManager()


    var lastHandledAddressState by remember { mutableStateOf(addAddressState) }
    LaunchedEffect(Unit) {
        // Clear current entry saved state
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.keys()
            ?.forEach { key ->
                navController.currentBackStackEntry?.savedStateHandle?.remove<Any>(key)
            }

        // Clear previous entry saved state
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.keys()
            ?.forEach { key ->
                navController.previousBackStackEntry?.savedStateHandle?.remove<Any>(key)
            }

        Log.d("CarWashAppScreen", "🧹 Cleared savedStateHandle from both entries")
    }


    LaunchedEffect(Unit) {
        val userId = userPref.getUserId()
        val token = userPref.getJwtToken()
        Log.d("HomeScreen", "📡 Preparing to call checkActiveBooking — userId=$userId, token=$token")

        if (userId is Int && token != null && token is String) {
            Log.d("HomeScreen", "🚀 Calling checkActiveBooking() now...")
            homepageServiceViewModel.checkActiveBooking(userId, token)
        }else{
            Log.e("HomeScreen", "❌ Missing userId or token — API not called")
        }
    }
    val uiState by homepageServiceViewModel.uiState.collectAsState()
    var backendLoaded by remember { mutableStateOf(false) }
    suspend fun restorePendingBookingIfNeeded(
        userPref: UserPreferencesManager,
        navController: NavController
    ) {
        val pending = userPref.getPendingBooking() ?: return

        try {
            var destination = pending.route
            val sheetType = pending.sheetType
            val argsJson = pending.argsJson

            val isSending = try {
                argsJson?.let { JSONObject(it).optBoolean("isRequestSending", false) } ?: false
            } catch (e: Exception) { false }

            destination = if (destination.contains("?")) {
                "$destination&sheet=$sheetType"
            } else {
                "$destination?sheet=$sheetType"
            }

            if (isSending) destination += "&isSending=true"

            Log.d("CarWashApp", "🔄 Restoring pending booking → $destination")

            navController.navigate(destination) {
                popUpTo(Routes.HOME_SCREEN) { inclusive = false }
            }

        } catch (e: Exception) {
            Log.e("CarWashApp", "Restore failed: ${e.message}")
        }
    }


    val coroutineScope = rememberCoroutineScope()
    var pendingRestored by remember { mutableStateOf(false) }
    // --- REPLACE the existing LaunchedEffect block with this ---
    // 🔹 Check booking status immediately on app launch
    var apiHandled by remember { mutableStateOf(false) }

    LaunchedEffect(
        uiState.backendResponded
    ) {
        if (!uiState.backendResponded) return@LaunchedEffect

        // ✅ Only cleanup logic — NO navigation
        if (homepageServiceViewModel.isBackendBookingNone()) {
            Log.d("HomeScreen", "Backend says no booking — clearing state")
            userPref.clearPendingBooking()
            userPref.saveBookingStatus("")
        }
    }









    LaunchedEffect(uiState.activeBookingMessage) {
        uiState.activeBookingMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // Listen for FCM broadcast when vendor accepts booking
    val broadcastReceiver = remember {
        object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                val notificationType = intent?.getStringExtra("type")
                val bookingIdString = intent?.getStringExtra("bookingId")
                
                Log.d("HomeScreen", "📨 Received broadcast - Type: $notificationType, BookingId: $bookingIdString")
                
                if (notificationType == com.example.carzorrouserside.util.AppConstants.NOTIFICATION_TYPE_BOOKING_ACCEPTED) {
                    Log.d("HomeScreen", "✅ Vendor accepted booking - refreshing active booking and navigating")
                    
                    // Clear rebids
                    homepageServiceViewModel.clearRebidsForAcceptedBooking()
                    
                    // Refresh active booking to get latest status
                    val userId = userPref.getUserId()
                    val token = userPref.getJwtToken()
                    
                    if (userId is Int && token != null && token is String) {
                        homepageServiceViewModel.checkActiveBooking(userId, token)
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


    LaunchedEffect(Unit) {
        Log.d("CarWashAppScreen", "Loading selected address on screen composition")
        addressViewModel.loadSelectedAddress()

        if (addressViewModel.isAuthenticated()) {
            addressViewModel.loadAddressList()
        }
    }

    LaunchedEffect(addAddressState) {
        if (addAddressState != lastHandledAddressState) {
            when (addAddressState) {
                is Resource.Success -> {
                    snackbarState.showSuccess("Address saved successfully!")
                    addressViewModel.loadSelectedAddress()
                }

                is Resource.Error -> {
                    val errorMessage = (addAddressState as Resource.Error<AddressResponse>).message
                        ?: "Failed to save address"
                    snackbarState.showError(errorMessage)
                }

                else -> {}
            }
            lastHandledAddressState = addAddressState
        }
    }

    LaunchedEffect(selectedAddressForHome) {
        selectedAddressForHome?.let { address ->
            Log.d(
                "CarWashAppScreen",
                "Selected address updated: ${address.fullName} in ${address.city}"
            )
            // Refetch popular providers when address changes
            popularAreaViewModel.fetchPopularProviders()
        } ?: Log.d("CarWashAppScreen", "No address currently selected")
    }

    // Observe savedStateHandle changes to show car selection sheet
    // Check immediately when composable is first composed
    LaunchedEffect(Unit) {
        val showSheet = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("showCarSelectionSheet")

        if (showSheet == true) {
            showCarSelectionSheet = true
            // Clear the flag after reading it
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("showCarSelectionSheet", false)
        }
    }
    
    // Also check when back stack entry changes (after navigation)
    LaunchedEffect(navController.currentBackStackEntry) {
        val showSheet = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("showCarSelectionSheet")

        if (showSheet == true) {
            showCarSelectionSheet = true
            // Clear the flag after reading it
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("showCarSelectionSheet", false)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomNavBar(navController = navController, currentRoute = currentRoute)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    LocationHeader(
                        onCarButtonClick = { showCarSelectionSheet = true },
                        onAddressClick = {
                            Log.d(
                                "CarWashAppScreen",
                                "Address header clicked, navigating to address listing"
                            )
                            navController.navigate(Routes.ADDRESS_LISTING_SCREEN)
                        },
                        selectedCar = selectedCar,
                        selectedAddress = selectedAddressForHome
                    )



                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-50).dp)
                    ) {
                        // CHANGE 1: Passed the HomepageServiceViewModel to the ServiceCategories composable
                        ServiceCategories(
                            navController = navController,
                            viewModel = homepageServiceViewModel,
                            snackbarState = snackbarState,
                            onShowAuthModal = { showAuthModalForService = true }
                        )
                    }

                    DynamicBannerSection(
                        bannerUiState = bannerUiState,
                        onRetry = { bannerViewModel.retryLoadBanners() }
                    )

                    // Only show Popular in your area when location is selected
                    selectedAddressForHome?.let {
                        PopularProvidersSection(
                            navController = navController,
                            viewModel = popularAreaViewModel
                        )
                    }

                    PackageProductTabSection(navController = navController, userPreferencesManager = userPref)

                    // CHANGE 2: The old "HomepageServicesSection" has been removed from here as its
                    // functionality is now inside the "ServiceCategories" card.

                    TestimonialsSection(viewModel = testimonialViewModel)

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            AnimatedVisibility(
                visible = uiState.hasRebid && uiState.activeBids.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -150 }) + fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA000000)) // translucent dark background
                        .zIndex(2f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                            .zIndex(3f)
                    ) {

                        uiState.activeBids.forEach { bid ->
                            UserBidCard(
                                bid = bid,
                                onAccept = { selectedBid ->
                                    homepageServiceViewModel.acceptBookingOffer(
                                        bid = selectedBid,
                                        onSuccess = { bookingId, vendorId ->
                                            // Navigate to booking summary with booking ID
                                            navController.navigate(Routes.bookingSummaryScreenWithBookingId(bookingId)) {
                                                popUpTo(Routes.HOME_SCREEN) { inclusive = false }
                                            }
                                        },
                                        onError = { errorMessage ->
                                            Toast.makeText(
                                                context,
                                                errorMessage,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                },
                                onReject = { selectedBid ->
                                    homepageServiceViewModel.declineBookingOffer(
                                        bid = selectedBid,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Booking declined.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { errorMessage ->
                                            Toast.makeText(
                                                context,
                                                errorMessage,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

        }
        // ✅ Floating Active Booking Card (Bottom Center)
        if (uiState.hasActiveBooking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp), // ⬅️ above bottom nav
                contentAlignment = Alignment.BottomCenter
            ) {
                ActiveBookingCard(
                    uiState = uiState,
                    onClick = {
                        navigateFromActiveBooking(
                            uiState = uiState,
                            navController = navController
                        )
                    }
                )
            }
        }


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
            onDismissWarning = { snackbarState.hideWarning() },
            onRetryError = {
                homepageServiceViewModel.retryLoadServices()
            }
        )

        // Show authentication modal for service clicks
        ShowAuthenticationModal(
            navController = navController,
            userPreferencesManager = userPref,
            showModal = showAuthModalForService,
            onDismiss = {
                showAuthModalForService = false
            }
        )

        CarSelectionBottomSheet(
            isVisible = showCarSelectionSheet,
            onDismiss = { showCarSelectionSheet = false },
            onCarSelected = { car ->
                selectedCar = car
                showCarSelectionSheet = false
            },
            navController = navController
        )

        AddressBottomSheet(
            viewModel = addressViewModel,
            onDismiss = { addressViewModel.hideBottomSheet() },
            onAddressAdded = {
                Log.d("CarWashAppScreen", "Address added callback triggered")
            }
        )
    }
}

@Composable
fun LocationHeader(
    onCarButtonClick: () -> Unit,
    onAddressClick: () -> Unit,
    selectedCar: CarListingItem? = null,
    selectedAddress: AddressItem? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(appPrimary)
            .padding(top = 36.dp, bottom = 60.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onAddressClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color.White
            )

            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = if (selectedAddress != null) {
                        "${selectedAddress.city}, ${selectedAddress.state}"
                    } else {
                        "Select Location"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = selectedAddress?.address ?: "Tap to add your address",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Button(
            onClick = { onCarButtonClick() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.height(40.dp),
            contentPadding = PaddingValues(start = 8.dp, end = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedCar != null) {
                    AsyncImage(
                        model = selectedCar.imageUrl,
                        contentDescription = "Selected car",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.logo),
                        error = painterResource(id = R.drawable.logo)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Select Car Icon",
                        tint = appPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = selectedCar?.let { car ->
                        val brand = car.brand?.takeIf { it.isNotBlank() } ?: ""
                        val model = car.model?.takeIf { it.isNotBlank() } ?: ""
                        when {
                            brand.isNotEmpty() && model.isNotEmpty() -> "$brand $model"
                            brand.isNotEmpty() -> brand
                            model.isNotEmpty() -> model
                            else -> "Select Car"
                        }
                    } ?: "Select Car",
                    color = appPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PopularProvidersSection(
    navController: NavController,
    viewModel: PopularAreaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Text(
            text = "Popular in your area",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when {
            uiState.isLoading -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(4) { PopularProviderPlaceholder() }
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load providers", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.fetchPopularProviders() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            uiState.providers.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No popular providers found in your area.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            uiState.providers.isNotEmpty() -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.providers) { provider ->
                        PopularProviderItem(
                            provider = provider,
                            onClick = {
                                navController.navigate(Routes.vendorDetailScreen(provider.id))
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun UserBidCard(
    bid: Bid,
    onAccept: (Bid) -> Unit,
    onReject: (Bid) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp) // ✅ More consistent margins
            .height(160.dp), // ✅ Taller to match Figma
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔹 Left section — Vendor details
            Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Vendor Image",
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAEAEA)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = bid.vendorName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${bid.vendorRating} (${bid.vendorReviews} reviews)",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 🔹 Right section — Price + Buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(90.dp)
            ) {
                Text(
                    text = "₹${bid.newAmount}",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onReject(bid) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
                        modifier = Modifier
                            .height(40.dp)
                            .width(90.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Decline",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { onAccept(bid) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1C4E9)),
                        modifier = Modifier
                            .height(40.dp)
                            .width(90.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Accept",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PopularProviderItem(provider: PopularProviderItem, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = provider.profilePic,
            contentDescription = provider.name,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.logo),
            error = painterResource(id = R.drawable.logo)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = provider.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_star),
                contentDescription = "Rating",
                tint = Color(0xFFFFA500),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "%.1f".format(provider.rating),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Composable
fun PopularProviderPlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(12.dp)
                .fillMaxWidth(0.8f)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(10.dp)
                .fillMaxWidth(0.5f)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun PackageProductTabSection(
    navController: NavController,
    userPreferencesManager: UserPreferencesManager
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = appPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = appPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "Packages",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "Packages",
                        tint = if (selectedTabIndex == 0) appPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "Products",
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Products",
                        tint = if (selectedTabIndex == 1) appPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 420.dp),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            when (selectedTabIndex) {
                0 -> PackagesContent(navController = navController)
                1 -> ProductsContent(navController = navController, userPreferencesManager = userPreferencesManager)
            }
        }
    }
}


@Composable
fun PackagesContent(navController: NavController, viewModel: PackageViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Our Popular Packages",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .align(Alignment.CenterHorizontally)
        )

        when {
            uiState.isLoading && uiState.packages.isEmpty() -> {
                repeat(3) {
                    PackageCardPlaceholder()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            uiState.error != null && uiState.packages.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Failed to load packages.", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { /* ViewModel action to retry */ }) {
                        Text("Retry")
                    }
                }
            }
            uiState.packages.isNotEmpty() -> {
                uiState.packages.take(3).forEach { packageItem ->
                    HomepagePackageCard(
                        packageItem = packageItem,
                        onClick = {
                            navController.navigate(Routes.packageDetailScreen(packageItem.id))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate(Routes.PACKAGE_SCREEN)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "View All Packages",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun HomepagePackageCard(packageItem: ApiPackage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = packageItem.imageRes),
                contentDescription = packageItem.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = packageItem.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    packageItem.originalPrice?.let { original ->
                        Text(
                            text = "₹${original.roundToInt()}",
                            textDecoration = TextDecoration.LineThrough,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "₹${packageItem.displayPrice.roundToInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%.1f".format(packageItem.rating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${packageItem.reviewCount} Reviews)",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View Details",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PackageCardPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.height(16.dp).fillMaxWidth(0.7f).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.height(14.dp).fillMaxWidth(0.5f).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.height(12.dp).fillMaxWidth(0.6f).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp)))
            }
        }
    }
}


@Composable
fun ProductsContent(
    navController: NavController,
    userPreferencesManager: UserPreferencesManager
) {
    val productViewModel: ProductViewModel = hiltViewModel()
    val homeProductsState by productViewModel.homeProductsState.collectAsState()
    val snackbarState = rememberSnackbarState()
    var lastShownError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        productViewModel.refreshHomeProducts()
    }
    LaunchedEffect(homeProductsState.error) {
        val currentError = homeProductsState.error
        if (currentError != null && currentError != lastShownError) {
            snackbarState.showError(currentError)
            lastShownError = currentError
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            homeProductsState.isLoading -> {
                repeat(4) {
                    ProductCardPlaceholder()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            homeProductsState.error != null && homeProductsState.products.isEmpty() -> {
                Column(
                    modifier = Modifier.padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Failed to load products",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { productViewModel.retryLoadHomeProducts() },
                        colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }

            homeProductsState.products.isNotEmpty() -> {
                homeProductsState.products.forEach { product ->
                    ProductCard(
                        product = product,
                        navController = navController,
                        onClick = { },
                        userPreferencesManager = userPreferencesManager
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        navController.navigate(Routes.ALL_PRODUCTS_SCREEN)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "View All Products",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier.padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "No Products",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No products available",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We're stocking up on amazing products for you!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

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
        onDismissWarning = { snackbarState.hideWarning() },
        onRetryError = {
            productViewModel.retryLoadHomeProducts()
        }
    )
}

@Composable
fun ProductCard(
    product: Product,
    navController: NavController,
    onClick: () -> Unit = {},
    userPreferencesManager: UserPreferencesManager? = null
) {
    val context = LocalContext.current
    val userPref = userPreferencesManager ?: remember {
        UserPreferencesManager(context.applicationContext)
    }
    var showAuthModal by remember { mutableStateOf(false) }
    
    // Show authentication modal
    ShowAuthenticationModal(
        navController = navController,
        userPreferencesManager = userPref,
        showModal = showAuthModal,
        onDismiss = { showAuthModal = false }
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val isLoggedIn = runBlocking { 
                    userPref.getJwtToken() != null 
                }
                if (!isLoggedIn) {
                    showAuthModal = true
                } else {
                    navController.navigate(Routes.productDetailScreen(product.id))
                }
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.image?.productImageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.outline_photo_camera_24),
                error = painterResource(id = R.drawable.outline_error_24)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${product.discountPrice}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = appPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "₹${product.price}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = LocalTextStyle.current.copy(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val discount = try {
                        val original = product.price.toDouble()
                        val discounted = product.discountPrice.toDouble()
                        ((original - discounted) / original * 100).toInt()
                    } catch (e: Exception) {
                        0
                    }

                    if (discount > 0) {
                        Text(
                            text = "${discount}% OFF",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(
                                    Color.Red,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (product.stockQuantity > 0) {
                        "In Stock (${product.stockQuantity})"
                    } else {
                        "Out of Stock"
                    },
                    fontSize = 12.sp,
                    color = if (product.stockQuantity > 0) Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ProductCardPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    color = appPrimary,
                    strokeWidth = 2.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicBannerSection(
    bannerUiState: BannerUiState,
    onRetry: () -> Unit
) {
    when {
        bannerUiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = appPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        bannerUiState.error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Failed to load banners",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
        }

        bannerUiState.banners.isNotEmpty() -> {
            AutoScrollingBannerPager(banners = bannerUiState.banners)
        }

        else -> {
            PromotionalBanner()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoScrollingBannerPager(banners: List<Banner>) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { banners.size }
    )

    val autoScrollDelay = 2000L

    LaunchedEffect(Unit) {
        if (banners.size > 1) {
            while (true) {
                delay(autoScrollDelay)
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.currentPage + 1) % banners.size
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(
                            durationMillis = 600,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        )
                    )
                }
            }
        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing=16.dp,
                beyondViewportPageCount = 1,
                userScrollEnabled = true
            ) { page ->
                DynamicBannerCard(
                    banner = banners[page]
                )
            }
        }

        if (banners.size > 1) {
            BannerIndicators(
                currentIndex = pagerState.currentPage,
                totalCount = banners.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
fun BannerIndicators(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalCount) { index ->
            val isSelected = index == currentIndex

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(
                        width = if (isSelected) 24.dp else 8.dp,
                        height = 8.dp
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) appPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 300)
                    )
            )
        }
    }
}

@Composable
fun DynamicBannerCard(
    banner: Banner
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = banner.bannerImageUrl,
                contentDescription = banner.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f)
                            ),
                            startY = 200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (banner.title.isNotEmpty()) {
                    Text(
                        text = banner.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (banner.status.isNotEmpty()) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = appPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = banner.status,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionalBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2B3990))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = "CLEAN YOUR",
                    color = Color(0xFFFFA500),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "DELUXE CAR",
                    color = Color(0xFFFFA500),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "BOOK NOW",
                        color = Color(0xFF2B3990),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Deluxe Car",
                modifier = Modifier
                    .size(150.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun SearchBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = {
                Text(
                    "Search Pressure car wash...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = appPrimary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLeadingIconColor = appPrimary,
                unfocusedLeadingIconColor = appPrimary
            ),
            singleLine = true
        )
    }
}

// CHANGE 3: `ServiceCategories` is completely rewritten to be state-driven.
@Composable
fun ServiceCategories(
    navController: NavController,
    viewModel: HomepageServiceViewModel,
    snackbarState: SnackbarState,
    onShowAuthModal: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var lastShownError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Effect to show snackbar on error
    LaunchedEffect(uiState.error) {
        val currentError = uiState.error
        if (currentError != null && currentError != lastShownError) {
            snackbarState.showError(currentError)
            lastShownError = currentError
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SearchBar()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )

                Text(
                    text = "What's in your mind?",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    val placeholderRows = (0..5).chunked(3)
                    placeholderRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            rowItems.forEach {
                                ServiceCategoryPlaceholder()
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                uiState.error != null && uiState.services.isEmpty() -> {
                    // Show error view with a retry button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Failed to load services", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.retryLoadServices() }) {
                            Text("Retry")
                        }
                    }
                }

                uiState.services.isNotEmpty() -> {
                    val serviceRows = uiState.services.chunked(3)
                    serviceRows.forEach { rowServices ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowServices.forEach { service ->
                                ApiDrivenServiceCategoryItem(
                                    service = service,
                                    onClick = {
                                        val userPref = EntryPointAccessors.fromApplication(
                                            context.applicationContext,
                                            UserPrefEntryPoint::class.java
                                        ).userPreferencesManager()

                                        val isLoggedIn = runBlocking { userPref.getJwtToken() != null }

                                        if (!isLoggedIn) {
                                            onShowAuthModal()
                                        } else {
                                            snackbarState.showInfo("Selected ${service.name}")
                                            navController.navigate(Routes.BOOKING_SCREEN)
                                        }
                                    }
                                )
                            }
                            repeat(3 - rowServices.size) {
                                Spacer(modifier = Modifier.width(100.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                else -> {
                    // Show empty state if there are no services
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No services available at the moment.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApiDrivenServiceCategoryItem(
    service: HomepageService,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = service.image,
                contentDescription = service.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = service.name,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ServiceCategoryPlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .height(14.dp)
                .fillMaxWidth(0.8f)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        )
    }
}



@Composable
fun TestimonialsSection(viewModel: TestimonialViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = "What Our Clients Say",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        when {
            uiState.isLoading -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(3) {
                        TestimonialCardPlaceholder()
                    }
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Failed to load testimonials",
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }
            uiState.testimonials.isNotEmpty() -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.testimonials) { testimonial ->
                        TestimonialCard(testimonial = testimonial)
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No testimonials available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun TestimonialCard(testimonial: Testimonial) {
    Card(
        modifier = Modifier
            .width(280.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = testimonial.image,
                    contentDescription = "Avatar of ${testimonial.name}",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.logo),
                    error = painterResource(id = R.drawable.logo)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = testimonial.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = testimonial.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(testimonial.rating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "\"${testimonial.review}\"",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 60.dp)
            )
        }
    }
}

@Composable
fun TestimonialCardPlaceholder() {
    Card(
        modifier = Modifier
            .width(280.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .width(100.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(70.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.7f)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            )
        }
    }
}
@EntryPoint
@InstallIn(SingletonComponent::class)
interface UserPrefEntryPoint {
    fun userPreferencesManager(): UserPreferencesManager
}
@Composable
fun ActiveBookingCard(
    uiState: HomepageServiceUiState,
    onClick: () -> Unit
) {
    if (!uiState.hasActiveBooking) return

    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .height(64.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF4CC) // 🟡 Cream like screenshot
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "Booking",
                tint = Color(0xFF6A1B9A) // Purple icon
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ongoing booking",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Tap to continue",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


fun navigateFromActiveBooking(
    uiState: HomepageServiceUiState,
    navController: NavController
) {
    val bookingId = uiState.activeBookingData?.booking?.id ?: return
    val status = uiState.bookingStatus?.lowercase()

    when (status) {
//        "pending" -> {
//            navController.navigate(Routes.BOOKING_STATUS_SCREEN)
//        }
        "created", "bidding" -> {
            navController.navigate(
                Routes.bookingScreenWithSheet(
                    sheet = "SET_PRICE",
                    isSending = true
                )
            )
        }


        "accepted", "vendor_accepted" -> {
            navController.navigate(
                Routes.bookingSummaryScreenWithBookingId(bookingId)
            )
        }

        "started","vendor_arrived","vehicle_picked_up","service_started","service_completed" -> {
            navController.navigate(
                "service_tracking_screen/$bookingId/confirmed"
            )
        }

    }
}




@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewCarWashApp() {
    CarWashAppScreen(rememberNavController())
}