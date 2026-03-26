package com.example.carzorrouserside.ui.theme.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.loginscreen.LogoutUiState
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.ui.theme.viewmodel.login.LogoutViewModel
import com.example.carzorrouserside.ui.theme.viewmodel.profile.ProfileViewModel
import com.example.carzorrouserside.data.model.profile.UserBasicDetails


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    logoutViewModel: LogoutViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    val logoutState by logoutViewModel.logoutState.collectAsStateWithLifecycle()
    val isUserLoggedIn by logoutViewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

    // Refresh profile data when screen becomes visible
    // This will refresh when navigating back from UpdateProfileScreen
    LaunchedEffect(navController.currentBackStackEntry?.id) {
        profileViewModel.refreshUserBasicDetails()
    }

    LaunchedEffect(logoutState) {
        when (logoutState) {
            is LogoutUiState.Success -> {
                navController.navigate(Routes.LOGIN_SCREEN) {
                    popUpTo(0) { inclusive = true }
                }
                logoutViewModel.resetLogoutState()
            }
            is LogoutUiState.Error -> {
                Log.e("ProfileScreen", "Logout error: ${(logoutState as LogoutUiState.Error).message}")
                logoutViewModel.resetLogoutState()
            }
            else -> { }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        fontWeight = FontWeight.Medium
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
                    containerColor = appPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (profileState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = appPrimary)
                }
            } else if (profileState.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = profileState.error ?: "Failed to load profile",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { profileViewModel.refreshUserBasicDetails() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                ProfileHeader(
                    navController = navController,
                    userBasicDetails = profileState.userBasicDetails,
                    onClick = {
                        navController.navigate(Routes.UPDATE_PROFILE_SCREEN)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSection(
                    title = "GENERAL",
                    items = listOf(
                        ProfileMenuItem(
                            icon = Icons.Default.List,
                            title = "My Orders",
                            onClick = {
                                navController.navigate(Routes.BOOKING_HISTORY_SCREEN)
                            }
                        ),
                        ProfileMenuItem(
                            icon = Icons.Default.Language,
                            title = "App Language",
                            onClick = { }
                        ),
                        ProfileMenuItem(
                            icon = Icons.Default.Star,
                            title = "Rate Us",
                            onClick = { }
                        )
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileSection(
                    title = "ABOUT APP",
                    items = listOf(
                        ProfileMenuItem(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy Policy",
                            onClick = { navController.navigate(Routes.PRIVACY_SCREEN)}
                        ),
                        ProfileMenuItem(
                            icon = Icons.Default.Description,
                            title = "Terms & Conditions",
                            onClick = { navController.navigate(Routes.TERMS_AND_CONDITIONS_SCREEN)}
                        ),
                        ProfileMenuItem(
                            icon = Icons.Default.Support,
                            title = "Help Support",
                            onClick = {navController.navigate(Routes.HELP_SUPPORT_SCREEN) }
                        ),
                        ProfileMenuItem(
                            icon = Icons.Default.Logout,
                            title = "Log Out",
                            showArrow = false,
                            onClick = {
                                if (isUserLoggedIn) {
                                    showLogoutDialog = true
                                } else {
                                    navController.navigate(Routes.LOGIN_SCREEN)
                                }
                            }
                        )
                    )
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }


    LogoutBottomSheet(
        isVisible = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        onConfirm = {
            showLogoutDialog = false
            logoutViewModel.logout()
        },
        isLoading = logoutState is LogoutUiState.Loading,
        enabled = logoutState !is LogoutUiState.Loading
    )
}

/**
 * Logout confirmation bottom sheet matching Figma design
 * Design: https://www.figma.com/design/LMVSAOmKLRySyuL4b9EOfV/Carzorro?node-id=12-4513
 * Matches the select car modal style (bottom sheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title: "Logout" - 20sp, Medium, #212121
                Text(
                    text = "Logout",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(23.dp))

                // Divider: light purple line (#eff5ff)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFEFF5FF))
                )

                Spacer(modifier = Modifier.height(31.dp))

                // Message: "Are you sure you want to logout?" - 14sp, Regular, #868d95
                Text(
                    text = "Are you sure you want to logout?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF868D95),
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(66.dp))

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cancel button: White background, #7761ff text, rounded 36px, 162dp wide, 46dp high
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .border(1.dp, Color(0xFFEFF5FF), RoundedCornerShape(36.dp))
                            .background(Color.White)
                            .clickable(enabled = enabled) { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7761FF),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Yes, Logout button: #9d8cff background, white text, rounded 36px, 161dp wide, 46dp high
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .border(1.dp, Color(0xFFEFF5FF), RoundedCornerShape(36.dp))
                            .background(Color(0xFF9D8CFF))
                            .clickable(enabled = enabled) { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Yes, Logout",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(
    navController: NavController,
    userBasicDetails: UserBasicDetails?,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(appPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userBasicDetails?.profilePic.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userBasicDetails?.profilePic)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.logo),
                            error = painterResource(id = R.drawable.logo)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Profile Picture",
                            tint = appPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Edit profile photo icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .clickable {
                            navController.navigate(Routes.UPDATE_PROFILE_SCREEN)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Photo",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }

                // Badge overlay (positioned above edit icon)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2B3990))
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userBasicDetails?.fullName ?: "User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Calculate profile completion percentage
                val completionPercentage = calculateProfileCompletion(userBasicDetails)
                Text(
                    text = "$completionPercentage% Complete",
                    fontSize = 12.sp,
                    color = appPrimary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                Color(0xFFFFF3E0),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable {
                                navController.navigate(Routes.COINS_SCREEN)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFA500)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(userBasicDetails?.walletBalance ?: 0.0).toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8F00)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFA500),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "4.5",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    items: List<ProfileMenuItem>
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = appPrimary.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileMenuItemRow(
                        item = item,
                        showDivider = index < items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItemRow(
    item: ProfileMenuItem,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { item.onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (item.showArrow) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (showDivider) {
            Divider(modifier = Modifier.padding(start = 56.dp))
        }
    }
}

data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val showArrow: Boolean = true,
    val onClick: () -> Unit
)

/**
 * Calculate profile completion percentage based on filled fields
 */
@Composable
fun calculateProfileCompletion(userBasicDetails: UserBasicDetails?): Int {
    if (userBasicDetails == null) return 0
    
    var filledFields = 0
    val totalFields = 8 // Total number of fields to check
    
    if (!userBasicDetails.fullName.isNullOrBlank()) filledFields++
    if (!userBasicDetails.phone.isNullOrBlank()) filledFields++
    if (!userBasicDetails.email.isNullOrBlank()) filledFields++
    if (!userBasicDetails.dob.isNullOrBlank()) filledFields++
    if (!userBasicDetails.profilePic.isNullOrBlank()) filledFields++
    if (!userBasicDetails.gender.isNullOrBlank()) filledFields++
    if (userBasicDetails.isPhoneVerified) filledFields++
    if (userBasicDetails.isEmailVerified) filledFields++
    
    return (filledFields * 100 / totalFields)
}