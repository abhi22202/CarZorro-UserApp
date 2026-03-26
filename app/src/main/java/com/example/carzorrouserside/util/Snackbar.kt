package com.example.carzorrouserside.util

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.carzorrouserside.ui.theme.CarZorroColors
import kotlinx.coroutines.delay

// Snackbar Type Enum - SINGLE DEFINITION
enum class SnackbarType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING
}

// Snackbar Event Data Class
data class SnackbarEvent(
    val message: String,
    val type: SnackbarType = SnackbarType.INFO,
    val duration: Long = 3000L, // Duration in milliseconds
    val actionLabel: String? = null,
    val onActionClick: (() -> Unit)? = null
)

// Snackbar Position Enum
enum class SnackbarPosition {
    TOP,
    BOTTOM
}

@Stable
class SnackbarState {
    var showError by mutableStateOf(false)
        private set
    var showSuccess by mutableStateOf(false)
        private set
    var showInfo by mutableStateOf(false)
        private set
    var showWarning by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set
    var successMessage by mutableStateOf("")
        private set
    var infoMessage by mutableStateOf("")
        private set
    var warningMessage by mutableStateOf("")
        private set

    fun showError(message: String) {
        hideAll()
        errorMessage = message
        showError = true
    }

    fun showSuccess(message: String) {
        hideAll()
        successMessage = message
        showSuccess = true
    }

    fun showInfo(message: String) {
        hideAll()
        infoMessage = message
        showInfo = true
    }

    fun showWarning(message: String) {
        hideAll()
        warningMessage = message
        showWarning = true
    }

    fun hideError() {
        showError = false
        errorMessage = ""
    }

    fun hideSuccess() {
        showSuccess = false
        successMessage = ""
    }

    fun hideInfo() {
        showInfo = false
        infoMessage = ""
    }

    fun hideWarning() {
        showWarning = false
        warningMessage = ""
    }

    private fun hideAll() {
        showError = false
        showSuccess = false
        showInfo = false
        showWarning = false
    }

    fun handleSnackbarEvent(event: SnackbarEvent) {
        when (event.type) {
            SnackbarType.SUCCESS -> showSuccess(event.message)
            SnackbarType.ERROR -> showError(event.message)
            SnackbarType.INFO -> showInfo(event.message)
            SnackbarType.WARNING -> showWarning(event.message)
        }
    }
}

@Composable
fun rememberSnackbarState(): SnackbarState {
    return remember { SnackbarState() }
}

// Enhanced Snackbar Manager
@Composable
fun SnackbarManager(
    showError: Boolean = false,
    showSuccess: Boolean = false,
    showInfo: Boolean = false,
    showWarning: Boolean = false,
    errorMessage: String = "",
    successMessage: String = "",
    infoMessage: String = "",
    warningMessage: String = "",
    position: SnackbarPosition = SnackbarPosition.TOP,
    onDismissError: () -> Unit = {},
    onDismissSuccess: () -> Unit = {},
    onDismissInfo: () -> Unit = {},
    onDismissWarning: () -> Unit = {},
    onRetryError: (() -> Unit)? = null,
    onActionWarning: (() -> Unit)? = null,
    warningActionLabel: String? = null
) {
    // Show only one snackbar at a time with priority: Error > Warning > Success > Info
    when {
        showError && errorMessage.isNotEmpty() -> {
            when (position) {
                SnackbarPosition.TOP -> TopErrorSnackbar(
                    visible = showError,
                    message = errorMessage,
                    onRetry = onRetryError,
                    onDismiss = onDismissError
                )
                SnackbarPosition.BOTTOM -> BottomErrorSnackbar(
                    visible = showError,
                    message = errorMessage,
                    onRetry = onRetryError,
                    onDismiss = onDismissError
                )
            }
        }
        showWarning && warningMessage.isNotEmpty() -> {
            when (position) {
                SnackbarPosition.TOP -> TopWarningSnackbar(
                    visible = showWarning,
                    message = warningMessage,
                    actionLabel = warningActionLabel,
                    onAction = onActionWarning,
                    onDismiss = onDismissWarning
                )
                SnackbarPosition.BOTTOM -> BottomWarningSnackbar(
                    visible = showWarning,
                    message = warningMessage,
                    actionLabel = warningActionLabel,
                    onAction = onActionWarning,
                    onDismiss = onDismissWarning
                )
            }
        }
        showSuccess && successMessage.isNotEmpty() -> {
            when (position) {
                SnackbarPosition.TOP -> TopSuccessSnackbar(
                    visible = showSuccess,
                    message = successMessage,
                    onDismiss = onDismissSuccess
                )
                SnackbarPosition.BOTTOM -> BottomSuccessSnackbar(
                    visible = showSuccess,
                    message = successMessage,
                    onDismiss = onDismissSuccess
                )
            }
        }
        showInfo && infoMessage.isNotEmpty() -> {
            when (position) {
                SnackbarPosition.TOP -> TopInfoSnackbar(
                    visible = showInfo,
                    message = infoMessage,
                    onDismiss = onDismissInfo
                )
                SnackbarPosition.BOTTOM -> BottomInfoSnackbar(
                    visible = showInfo,
                    message = infoMessage,
                    onDismiss = onDismissInfo
                )
            }
        }
    }
}

// Top-positioned Snackbar Components
@Composable
private fun TopErrorSnackbar(
    visible: Boolean,
    message: String,
    onRetry: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    TopAnimatedSnackbar(
        visible = visible,
        message = message,
        type = SnackbarType.ERROR,
        duration = 2000,
        actionLabel = if (onRetry != null) "RETRY" else null,
        onAction = onRetry,
        onDismiss = onDismiss
    )
}

@Composable
private fun TopSuccessSnackbar(
    visible: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    TopAnimatedSnackbar(
        visible = visible,
        message = message,
        type = SnackbarType.SUCCESS,
        duration = 2000,
        onDismiss = onDismiss
    )
}

@Composable
private fun TopInfoSnackbar(
    visible: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    TopAnimatedSnackbar(
        visible = visible,
        message = message,
        type = SnackbarType.INFO,
        duration = 2000,
        onDismiss = onDismiss
    )
}

@Composable
private fun TopWarningSnackbar(
    visible: Boolean,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    TopAnimatedSnackbar(
        visible = visible,
        message = message,
        type = SnackbarType.WARNING,
        duration = 2000,
        actionLabel = actionLabel,
        onAction = onAction,
        onDismiss = onDismiss
    )
}

// Bottom-positioned Snackbar Components
@Composable
private fun BottomErrorSnackbar(
    visible: Boolean,
    message: String,
    onRetry: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    BottomAnimatedSnackbar(
        visible = visible,
        message = message,
        type = SnackbarType.ERROR,
        duration = 2000,
        actionLabel = if (onRetry != null) "RETRY" else null,
        onAction = onRetry,
        onDismiss = onDismiss
    )
}

@Composable
private fun BottomSuccessSnackbar(
    visible: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    BottomAnimatedSnackbar(
        visible = visible,
        message = message,
        type = SnackbarType.SUCCESS,
        duration = 2000,
        onDismiss = onDismiss
    )
}

@Composable
private fun BottomInfoSnackbar(
    visible: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    BottomAnimatedSnackbar(
        visible = visible,
        message = message,
        type = SnackbarType.INFO,
        duration = 2000,
        onDismiss = onDismiss
    )
}

@Composable
private fun BottomWarningSnackbar(
    visible: Boolean,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    BottomAnimatedSnackbar(
        visible = visible,
        message = message,
        type = SnackbarType.WARNING,
        duration = 2000,
        actionLabel = actionLabel,
        onAction = onAction,
        onDismiss = onDismiss
    )
}

// Top-positioned Animated Snackbar
@Composable
private fun TopAnimatedSnackbar(
    visible: Boolean,
    message: String,
    type: SnackbarType,
    duration: Long,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    LaunchedEffect(visible) {
        if (visible && actionLabel == null) {
            delay(duration)
            onDismiss()
        }
    }

    val config = getSnackbarConfig(type)

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else (-100).dp,
        animationSpec = tween(durationMillis = 300),
        label = "topSnackbarOffset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "topSnackbarAlpha"
    )

    if (visible || alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .zIndex(100f),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY)
                    .padding(top = 40.dp)
                    .alpha(alpha),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = config.backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                SnackbarContent(
                    message = message,
                    icon = config.icon,
                    actionLabel = actionLabel,
                    onAction = onAction,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

// Bottom-positioned Animated Snackbar
@Composable
private fun BottomAnimatedSnackbar(
    visible: Boolean,
    message: String,
    type: SnackbarType,
    duration: Long,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    LaunchedEffect(visible) {
        if (visible && actionLabel == null) {
            delay(duration)
            onDismiss()
        }
    }

    val config = getSnackbarConfig(type)

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 300),
        label = "bottomSnackbarOffset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "bottomSnackbarAlpha"
    )

    if (visible || alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .offset(y = offsetY)
                .zIndex(100f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(8.dp)
                    ),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = config.backgroundColor)
            ) {
                SnackbarContent(
                    message = message,
                    icon = config.icon,
                    actionLabel = actionLabel,
                    onAction = onAction,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

// Shared Snackbar Content
@Composable
private fun SnackbarContent(
    message: String,
    icon: ImageVector,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Action Button (e.g., RETRY)
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = {
                    onAction()
                    onDismiss() // Also dismiss on action
                }
            ) {
                Text(
                    text = actionLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Always show a dismiss icon button
        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Dismiss",
                tint = Color.White
            )
        }
    }
}

// Snackbar Configuration
private data class SnackbarConfig(
    val backgroundColor: Color,
    val icon: ImageVector
)

private fun getSnackbarConfig(type: SnackbarType): SnackbarConfig {
    return when (type) {
        SnackbarType.SUCCESS -> SnackbarConfig(
            backgroundColor = Color(0xFF4CAF50), // Green
            icon = Icons.Filled.CheckCircle
        )
        SnackbarType.ERROR -> SnackbarConfig(
            backgroundColor = Color(0xFFE53935), // Red
            icon = Icons.Filled.Error
        )
        SnackbarType.INFO -> SnackbarConfig(
            backgroundColor = Color(0xFF2196F3), // Blue
            icon = Icons.Filled.Info
        )
        SnackbarType.WARNING -> SnackbarConfig(
            backgroundColor = Color(0xFFFF9800), // Orange
            icon = Icons.Filled.Warning
        )
    }
}

// Legacy components for backward compatibility
@Composable
fun CancelRequestConfirmation(
    onKeepSearching: () -> Unit,
    onCancelRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onKeepSearching,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = -80.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CarZorroColors.cardBackground
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Are you sure want to cancel your request?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = CarZorroColors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onKeepSearching,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CarZorroColors.primary
                        )
                    ) {
                        Text(
                            text = "Keep Searching",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onCancelRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFEB3B)
                        )
                    ) {
                        Text(
                            text = "Cancel Request",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}