package com.example.carzorrouserside.ui.theme.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.navigation.Routes

/**
 * Helper composable to show authentication modal when user is not authenticated.
 * Use this in any composable that needs to check authentication before proceeding.
 * 
 * @param navController NavController for navigation
 * @param userPreferencesManager UserPreferencesManager to check auth state
 * @param showModal Boolean state to control when to show the modal
 * @param onDismiss Optional callback when modal is dismissed (default: closes modal only)
 */
@Composable
fun ShowAuthenticationModal(
    navController: NavController,
    userPreferencesManager: UserPreferencesManager,
    showModal: Boolean,
    onDismiss: (() -> Unit)? = null
) {
    val isAuthenticated by userPreferencesManager.isAuthenticated.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showModal, isAuthenticated) {
        if (showModal && !isAuthenticated) {
            showAuthDialog = true
        } else {
            showAuthDialog = false
        }
    }

    if (showAuthDialog && !isAuthenticated) {
        AuthenticationRequiredDialog(
            onDismiss = {
                showAuthDialog = false
                onDismiss?.invoke()
            },
            onLoginClick = {
                showAuthDialog = false
                navController.navigate(Routes.LOGIN_SCREEN) {
                    popUpTo(Routes.HOME_SCREEN) { inclusive = false }
                    launchSingleTop = true
                }
            },
            onSignUpClick = {
                showAuthDialog = false
                navController.navigate(Routes.SIGN_UP_SCREEN) {
                    popUpTo(Routes.HOME_SCREEN) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
    }
}

