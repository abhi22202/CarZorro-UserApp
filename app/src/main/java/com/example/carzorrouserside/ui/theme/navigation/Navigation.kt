package com.example.carzorrouserside.ui.theme.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.carzorrouserside.data.model.VendorResponse
import com.example.carzorrouserside.data.model.register.RegisterRequest
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.screen.booking.OrderDetailScreen
import com.example.carzorrouserside.ui.theme.screens.Helpsupport.HelpSupportScreen
import com.example.carzorrouserside.ui.theme.screens.PackagesScreen
import com.example.carzorrouserside.ui.theme.screens.ProfileScreen
import com.example.carzorrouserside.ui.theme.screens.UpdateProfileScreen
import com.example.carzorrouserside.ui.theme.screens.CoinsScreen
import com.example.carzorrouserside.ui.theme.screens.auth.LoginScreen
import com.example.carzorrouserside.ui.theme.screens.auth.OtpVerificationScreen
import com.example.carzorrouserside.ui.theme.screens.auth.WelcomeScreen
import com.example.carzorrouserside.ui.theme.screens.auth.register.SignUpScreen
import com.example.carzorrouserside.ui.theme.screens.auth.register.UserRegistrationOtpVerificationScreen
import com.example.carzorrouserside.ui.theme.screens.booking.BookingHistoryScreen
import com.example.carzorrouserside.ui.theme.screens.booking.BookingScreen
import com.example.carzorrouserside.ui.theme.screens.booking.BookingConfirmationScreen
import com.example.carzorrouserside.ui.theme.screens.booking.components.BookingSummaryScreen
import com.example.carzorrouserside.ui.theme.screens.booking.components.ServiceTrackingScreen
import com.example.carzorrouserside.ui.theme.screens.cardetails.CarDetailsScreen
import com.example.carzorrouserside.ui.theme.screens.favourites.FavouriteVendorScreen
import com.example.carzorrouserside.ui.theme.screens.homescreen.AllProductsScreen
import com.example.carzorrouserside.ui.theme.screens.homescreen.CarWashAppScreen
import com.example.carzorrouserside.ui.theme.screens.homescreen.ProductDetailScreenWithOrder
import com.example.carzorrouserside.ui.theme.screens.homescreen.address.AddressListingScreen
import com.example.carzorrouserside.ui.theme.screens.homescreen.car.BrandSelectionScreen
import com.example.carzorrouserside.ui.theme.screens.homescreen.car.ModelSelectionScreen
import com.example.carzorrouserside.ui.theme.screens.legal.PrivacyPolicyScreen
import com.example.carzorrouserside.ui.theme.screens.legal.TermsAndConditionsScreen
import com.example.carzorrouserside.ui.theme.screens.notification.NotificationScreen
import com.example.carzorrouserside.ui.theme.screens.packagedetail.PackageDetailScreen
import com.example.carzorrouserside.ui.theme.screens.splashscreen.SplashScreen
import com.example.carzorrouserside.ui.theme.screens.splashscreen.SplashScreen1
import com.example.carzorrouserside.ui.theme.screens.vendor.VendorDetailsScreen
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.AddressViewModel
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.HomepageServiceViewModel
import com.example.carzorrouserside.utils.token.UserAutomatedTokenExpirationHandler
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@SuppressLint("UnrememberedGetBackStackEntry")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(userPreferencesManager: UserPreferencesManager,navController: NavHostController) {
    //val navController = rememberNavController()
    val TAG = "Navigation"

    val tokenHandler = remember { UserAutomatedTokenExpirationHandler.getInstance() }
    val isAuthenticated by userPreferencesManager.isAuthenticated.collectAsState()
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) return@LaunchedEffect

        val currentRoute = navController.currentDestination?.route
        if (currentRoute == Routes.HOME_SCREEN) {
            Log.d("Navigation", "Already on HOME, skipping forced navigation")
            return@LaunchedEffect
        }

        Log.d("Navigation", "🧹 Clearing restored back stack and forcing HOME")

        navController.navigate(Routes.HOME_SCREEN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
            restoreState = false
        }
    }



    Log.d(TAG, "🏗️ Navigation component initialized")
    Log.d(TAG, "🔐 Authentication state: ${if (isAuthenticated) "✅ Authenticated" else "❌ Not Authenticated"}")

    LaunchedEffect(navController) {
        Log.d(TAG, "📱 Registering NavController with token handler for auto-logout")
        tokenHandler?.registerNavController(navController)
    }

    LaunchedEffect(Unit) {
        tokenHandler?.globalSnackbarEvents?.collect { snackbarEvent ->
            Log.d(TAG, "🚨 Token expiration event received: ${snackbarEvent.message}")
        }
    }

    val startDestination = if (isAuthenticated) {
        Log.d(TAG, "🏠 User is authenticated - starting at HOME_SCREEN")
        Routes.HOME_SCREEN
    } else {
        Log.d(TAG, "💦 User is not authenticated - starting at SPLASH_SCREEN")
        Routes.SPLASH_SCREEN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        route = "root"
    ) {
        composable(Routes.SPLASH_SCREEN) {
            SplashScreen(onNavigateToNextScreen = {
                navController.navigate(Routes.SPLASH_SCREEN_1) { popUpTo(Routes.SPLASH_SCREEN) { inclusive = true } }
            })
        }
        composable(Routes.SPLASH_SCREEN_1) {
            SplashScreen1(onNavigateToNextScreen = {
                navController.navigate(Routes.WELCOME_SCREEN) { popUpTo(Routes.SPLASH_SCREEN_1) { inclusive = true } }
            })
        }
        composable(Routes.WELCOME_SCREEN) {
            WelcomeScreen(navController = navController)
        }
        composable(
            route = "${Routes.VENDOR_DETAIL_SCREEN}/{vendorId}",
            arguments = listOf(navArgument("vendorId") { type = NavType.IntType })
        ) { backStackEntry ->
            val vendorId = backStackEntry.arguments?.getInt("vendorId") ?: 0
            Log.d("Navigation", "Navigating to VendorDetailsScreen for vendorId: $vendorId")
            VendorDetailsScreen(navController = navController)
        }
        composable(Routes.LOGIN_SCREEN) {
            LoginScreen(
                onSignUpClick = { navController.navigate(Routes.SIGN_UP_SCREEN) },
                onTermsClick = { navController.navigate(Routes.TERMS_AND_CONDITIONS_SCREEN) },
                onSendOtpClick = { phoneAndOtp: String ->
                    val parts = phoneAndOtp.split("/")
                    val phoneNumber = parts[0]
                    val serverOtp = if (parts.size > 1) parts[1] else ""
                    val route = "${Routes.VERIFY_LOGIN_OTP_SCREEN}/$phoneNumber/$serverOtp"
                    navController.navigate(route)
                },
                onContinueAsGuestClick = {
                    navController.navigate(Routes.HOME_SCREEN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "${Routes.VERIFY_LOGIN_OTP_SCREEN}/{phoneNumber}/{serverOtp}",
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("serverOtp") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val serverOtp = backStackEntry.arguments?.getString("serverOtp") ?: ""
            OtpVerificationScreen(
                phoneNumber = phoneNumber,
                initialServerOtp = serverOtp,
                onVerificationSuccess = {
                    tokenHandler?.forceTokenCheck()
                    navController.navigate(Routes.HOME_SCREEN) { popUpTo(0) { inclusive = true } }
                },
                onBackPressed = { navController.popBackStack() },
                onResendOtp = { /* ... */ }
            )
        }
        composable(Routes.SIGN_UP_SCREEN) {
            SignUpScreen(
                onSignInClick = {
                    navController.navigate(Routes.LOGIN_SCREEN) { popUpTo(Routes.SIGN_UP_SCREEN) { inclusive = true } }
                },
                onTermsClick = { navController.navigate(Routes.TERMS_AND_CONDITIONS_SCREEN) },
                onRegistrationSuccess = { registerRequest, registerResponse ->
                    val userId = registerResponse.data?.userId ?: 0
                    val phoneNumber = registerRequest.phone
                    val initialOtp = registerResponse.data?.otp?.toString() ?: ""
                    val route = Routes.signupOtpScreen(
                        phoneNumber = phoneNumber,
                        userId = userId,
                        initialOtp = initialOtp,
                        fullName = registerRequest.fullName,
                        email = registerRequest.email,
                        dob = registerRequest.dob,
                        gender = registerRequest.gender
                    )
                    navController.navigate(route)
                }
            )
        }
        composable(Routes.HOME_SCREEN) {
            CarWashAppScreen(navController = navController)
        }
        composable(Routes.TERMS_AND_CONDITIONS_SCREEN) {
            TermsAndConditionsScreen(navController = navController)
        }
        composable(Routes.PRIVACY_SCREEN) {
            PrivacyPolicyScreen(navController = navController)
        }
        composable(Routes.HELP_SUPPORT_SCREEN) {
            HelpSupportScreen(navController = navController)
        }
        composable(Routes.FAVOURITES_SCREEN) {
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                FavouriteVendorScreen(navController = navController)
            }
        }
        composable(Routes.PROFILE_SCREEN) {
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                ProfileScreen(navController = navController)
            }
        }
        composable(Routes.UPDATE_PROFILE_SCREEN) {
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                UpdateProfileScreen(navController = navController, userPreferencesManager = userPreferencesManager)
            }
        }
        composable(Routes.COINS_SCREEN) {
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                CoinsScreen(navController = navController)
            }
        }
        composable(Routes.BOOKING_HISTORY_SCREEN) {
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                BookingHistoryScreen(navController = navController, onBookingClick = { bookingId ->
                    navController.navigate(Routes.orderDetailScreen(bookingId))
                })
            }
        }
        composable(Routes.NOTIFICATION_SCREEN) {
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                NotificationScreen(navController = navController)
            }
        }
        composable(Routes.ALL_PRODUCTS_SCREEN) {
            AllProductsScreen(navController = navController)
        }
        composable(
            route = "${Routes.PRODUCT_DETAIL_SCREEN}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            ProductDetailScreenWithOrder(productId = productId, navController = navController)
        }
        composable(Routes.PACKAGE_SCREEN) {
            PackagesScreen(navController = navController)
        }
        composable(
            route = "${Routes.PACKAGE_DETAIL_SCREEN}/{packageId}",
            arguments = listOf(navArgument("packageId") { type = NavType.IntType })
        ) { backStackEntry ->
            val packageId = backStackEntry.arguments?.getInt("packageId")
            PackageDetailScreen(navController = navController)
        }

        composable(
            route = "${Routes.BRAND_SELECTION_SCREEN}?carId={carId}",
            arguments = listOf(navArgument("carId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            BrandSelectionScreen(
                navController = navController,
                carId = backStackEntry.arguments?.getInt("carId")
            )
        }

        composable(
            route = "${Routes.MODEL_SELECTION_SCREEN}/{brandId}/{brandName}?carId={carId}",
            arguments = listOf(
                navArgument("brandId") { type = NavType.IntType },
                navArgument("brandName") { type = NavType.StringType },
                navArgument("carId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val brandId = backStackEntry.arguments?.getInt("brandId") ?: 0
            val brandName = URLDecoder.decode(backStackEntry.arguments?.getString("brandName") ?: "", StandardCharsets.UTF_8.name())
            val carId = backStackEntry.arguments?.getInt("carId")

            ModelSelectionScreen(
                navController = navController,
                brandId = brandId,
                brandName = brandName,
                carId = carId
            )
        }

        composable(
            route = "${Routes.CAR_DETAILS_SCREEN}/{brandId}/{modelId}/{brandName}/{modelName}/{imageUrl}/{carId}",
            arguments = listOf(
                navArgument("brandId") { type = NavType.IntType },
                navArgument("modelId") { type = NavType.IntType },
                navArgument("brandName") { type = NavType.StringType },
                navArgument("modelName") { type = NavType.StringType },
                navArgument("imageUrl") { type = NavType.StringType },
                navArgument("carId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            CarDetailsScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        // Navigation.kt — booking composable (fixed)
        composable(
            route = "${Routes.BOOKING_SCREEN}?sheet={sheet}&isSending={isSending}",
            arguments = listOf(
                navArgument("sheet") {
                    type = NavType.StringType
                    defaultValue = "BOOKING"
                    nullable = true
                },
                // IMPORTANT: BoolType cannot be nullable — give a concrete default and DO NOT set nullable = true
                navArgument("isSending") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                val visibleResponses = remember { mutableStateListOf<VendorResponse>() }
                val homepageServiceViewModel: HomepageServiceViewModel = hiltViewModel()
                val context = androidx.compose.ui.platform.LocalContext.current

                BookingScreen(
                    navController = navController,
                    visibleResponses = visibleResponses,
                    onAccept = { vendorResponse ->
                        // Find the bid from activeBids
                        val bid = homepageServiceViewModel.uiState.value.activeBids
                            .firstOrNull { it.vendorId == vendorResponse.id }
                        
                        bid?.let {
                            homepageServiceViewModel.acceptBookingOffer(
                                bid = it,
                                onSuccess = { bookingId, vendorId ->
                                    // Navigate to booking summary with booking ID
                                    navController.navigate(Routes.bookingSummaryScreenWithBookingId(bookingId)) {
                                        popUpTo(Routes.BOOKING_SCREEN) { inclusive = true }
                                    }
                                },
                                onError = { errorMessage ->
                                    android.widget.Toast.makeText(
                                        context,
                                        errorMessage,
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    Log.e("Navigation", "Failed to accept booking: $errorMessage")
                                }
                            )
                        }
                    },
                    onDecline = { vendorResponse ->
                        // Find the bid from activeBids
                        val bid = homepageServiceViewModel.uiState.value.activeBids
                            .firstOrNull { it.vendorId == vendorResponse.id }
                        
                        bid?.let {
                            homepageServiceViewModel.declineBookingOffer(
                                bid = it,
                                onSuccess = {
                                    // Bid removed from UI automatically by ViewModel
                                    android.widget.Toast.makeText(
                                        context,
                                        "Booking declined.",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d("Navigation", "Booking offer declined successfully")
                                },
                                onError = { errorMessage ->
                                    android.widget.Toast.makeText(
                                        context,
                                        errorMessage,
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    Log.e("Navigation", "Failed to decline booking: $errorMessage")
                                }
                            )
                        }
                    }
                )
            }
        }



//        composable(Routes.BOOKING_SCREEN) {
//            val visibleResponses = remember { mutableStateListOf<VendorResponse>() }
//            BookingScreen(navController, visibleResponses, {}, {})
//        }
        composable(
            route = "${Routes.BOOKING_SUMMARY_SCREEN}/{vendorId}",
            arguments = listOf(navArgument("vendorId") { type = NavType.IntType })
        ) { backStackEntry ->
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                val vendorId = backStackEntry.arguments?.getInt("vendorId") ?: 0
                BookingSummaryScreen(
                    vendorId = vendorId,
                    bookingId = null,
                    onBackPressed = { navController.popBackStack() },
                    onProceedToPay = {},
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN_SCREEN) },
                    onCancelSuccess = {
                        navController.navigate(Routes.HOME_SCREEN) {
                            popUpTo(Routes.HOME_SCREEN) { inclusive = false }
                        }
                    },
                    navController = navController
                )
            }
        }
        composable(
            route = "${Routes.BOOKING_SUMMARY_SCREEN}/booking/{bookingId}",
            arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        ) { backStackEntry ->
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
                BookingSummaryScreen(
                    vendorId = 0,
                    bookingId = bookingId,
                    onBackPressed = { navController.popBackStack() },
                    onProceedToPay = {},
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN_SCREEN) },
                    onCancelSuccess = {
                        navController.navigate(Routes.HOME_SCREEN) {
                            popUpTo(Routes.HOME_SCREEN) { inclusive = false }
                        }
                    },
                    navController = navController
                )
            }
        }
        composable(
            route = "service_tracking_screen/{bookingId}/{status}",
            arguments = listOf(
                navArgument("bookingId") { type = NavType.IntType },
                navArgument("status") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.HOME_SCREEN)
            }

            val homepageServiceViewModel: HomepageServiceViewModel =
                hiltViewModel(parentEntry)

            val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: return@composable
            val status = backStackEntry.arguments?.getString("status") ?: "confirmed"

            ServiceTrackingScreen(
                navController = navController,
                homepageServiceViewModel = homepageServiceViewModel, // ✅ SAME INSTANCE
                bookingId = bookingId,
                initialStatus = status
            )
        }


        composable(
            route = "${Routes.ORDER_DETAIL_SCREEN}/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) {
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                OrderDetailScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
        composable(
            route = "${Routes.BOOKING_CONFIRMATION_SCREEN}?amount={amount}&method={method}",
            arguments = listOf(
                navArgument("amount") {
                    type = NavType.FloatType
                },
                navArgument("method") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble()
                ?: throw IllegalArgumentException("Payment amount is required")
            val paymentMethod = backStackEntry.arguments?.getString("method")
                ?: throw IllegalArgumentException("Payment method is required")
            BookingConfirmationScreen(
                navController = navController,
                paymentAmount = amount,
                paymentMethod = paymentMethod
            )
        }
        composable(Routes.ADDRESS_LISTING_SCREEN) {
            AuthenticationGuard(navController = navController, userPreferencesManager = userPreferencesManager) {
                val homeBackStackEntry = remember(navController) { navController.getBackStackEntry(Routes.HOME_SCREEN) }
                val addressViewModel: AddressViewModel = hiltViewModel(homeBackStackEntry)
                AddressListingScreen(navController = navController, addressViewModel = addressViewModel)
            }
        }
        composable(
            route = "${Routes.VERIFY_SIGNUP_OTP_SCREEN}/{phoneNumber}/{userId}/{initialOtp}/{fullName}/{email}/{dob}/{gender}",
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("userId") { type = NavType.IntType },
                navArgument("initialOtp") { type = NavType.StringType },
                navArgument("fullName") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("dob") { type = NavType.StringType },
                navArgument("gender") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val initialOtp = backStackEntry.arguments?.getString("initialOtp") ?: ""
            val fullName = URLDecoder.decode(backStackEntry.arguments?.getString("fullName") ?: "", StandardCharsets.UTF_8.name())
            val email = URLDecoder.decode(backStackEntry.arguments?.getString("email") ?: "", StandardCharsets.UTF_8.name())
            val dob = URLDecoder.decode(backStackEntry.arguments?.getString("dob") ?: "", StandardCharsets.UTF_8.name())
            val gender = URLDecoder.decode(backStackEntry.arguments?.getString("gender") ?: "", StandardCharsets.UTF_8.name())
            val originalRegisterRequest = RegisterRequest(fullName, email, dob, phoneNumber, gender)
            UserRegistrationOtpVerificationScreen(
                phoneNumber = "+91$phoneNumber",
                userId = userId,
                initialServerOtp = initialOtp,
                originalRegisterRequest = originalRegisterRequest,
                onVerificationSuccess = {
                    tokenHandler?.forceTokenCheck()
                    navController.navigate(Routes.HOME_SCREEN) { popUpTo(0) { inclusive = true } }
                },
                onBackPressed = { navController.popBackStack() }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "🧹 Navigation disposed - unregistering NavController from token handler")
            tokenHandler?.unregisterNavController()
        }
    }
}