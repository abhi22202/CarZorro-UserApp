package com.example.carzorrouserside.ui.theme.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.components.AuthenticationRequiredDialog

/**
 * Authentication Guard composable that checks if user is authenticated.
 * If not authenticated, shows a modal dialog prompting user to login/sign up.
 * If authenticated, shows the protected content.
 */
@Composable
fun AuthenticationGuard(
    navController: NavController,
    userPreferencesManager: UserPreferencesManager,
    content: @Composable () -> Unit
) {
    val TAG = "AuthenticationGuard"
    val isAuthenticated by userPreferencesManager.isAuthenticated.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            Log.d(TAG, "🔒 User not authenticated - showing authentication dialog")
            val currentRoute = navController.currentDestination?.route
            Log.d(TAG, "📍 Current route: $currentRoute")
            showAuthDialog = true
        } else {
            Log.d(TAG, "✅ User is authenticated - showing protected content")
            showAuthDialog = false
        }
    }

    // Show authentication dialog if not authenticated
    if (showAuthDialog && !isAuthenticated) {
        AuthenticationRequiredDialog(
            onDismiss = {
                showAuthDialog = false
                // Navigate back when dialog is dismissed
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                } else {
                    // If no back stack, navigate to home
                    navController.navigate(Routes.HOME_SCREEN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            onLoginClick = {
                showAuthDialog = false
                Log.d(TAG, "🔐 User clicked Login - navigating to login screen")
                navController.navigate(Routes.LOGIN_SCREEN) {
                    // Pop back to home or previous screen, but keep login in stack
                    popUpTo(Routes.HOME_SCREEN) { inclusive = false }
                    launchSingleTop = true
                }
            },
            onSignUpClick = {
                showAuthDialog = false
                Log.d(TAG, "📝 User clicked Sign Up - navigating to sign up screen")
                navController.navigate(Routes.SIGN_UP_SCREEN) {
                    // Pop back to home or previous screen, but keep sign up in stack
                    popUpTo(Routes.HOME_SCREEN) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
    }

    // Only show content if authenticated
    if (isAuthenticated) {
        content()
    }
}

