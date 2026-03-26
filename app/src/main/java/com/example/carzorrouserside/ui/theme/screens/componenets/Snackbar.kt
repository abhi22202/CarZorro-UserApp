package com.example.carzorrouserside.ui.theme.screens.componenets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import com.example.carzorrouserside.data.model.SnackbarType
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.viewmodel.booking.PostBookingViewModel
import kotlinx.coroutines.delay
@Composable
fun CancelRequestConfirmation(
    onKeepSearching: () -> Unit,
    onCancelRequest: () -> Unit
) {
    val postBookingViewModel: PostBookingViewModel = hiltViewModel()
    val isLoading by postBookingViewModel.isLoading.observeAsState(false)
    val cancelMessage by postBookingViewModel.cancelMessage.observeAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Show error message if cancel failed
    LaunchedEffect(cancelMessage) {
        cancelMessage?.let { message ->
            if (message.contains("Failed") || message.contains("Error")) {
                android.widget.Toast.makeText(
                    context,
                    message,
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    Dialog(
        onDismissRequest = {
            if (!isLoading) {
                onKeepSearching()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
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

                    .offset(y=-80.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),

                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
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
                    // Title
                    Text(
                        text = "Are you sure want to cancel your request?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Keep Searching button
                    Button(
                        onClick = onKeepSearching,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appPrimary
                        ),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Keep Searching",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cancel Request button
                    val bookingId = postBookingViewModel.bookingId.value
                    Button(
                        onClick = {
                            if (!isLoading) {
                                val currentBookingId = bookingId
                                if (currentBookingId != null && currentBookingId > 0) {
                                    // Use existing booking ID
                                    postBookingViewModel.cancelBooking(currentBookingId) { success ->
                                        if (success) {
                                            onCancelRequest()
                                        }
                                        // Error is shown via Toast in LaunchedEffect
                                    }
                                } else {
                                    // Fetch from active booking API if bookingId is null
                                    postBookingViewModel.fetchActiveBookingId { fetchedBookingId ->
                                        if (fetchedBookingId != null && fetchedBookingId > 0) {
                                            // Use fetched booking ID
                                            postBookingViewModel.cancelBooking(fetchedBookingId) { success ->
                                                if (success) {
                                                    onCancelRequest()
                                                }
                                                // Error is shown via Toast in LaunchedEffect
                                            }
                                        } else {
                                            // No active booking found, just close the dialog
                                            android.widget.Toast.makeText(
                                                context,
                                                "No active booking found",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            onCancelRequest()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFEB3B) // Yellow color
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
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
}

// Reusable animated snackbar component
@Composable
fun AnimatedSnackbar(
    visible: Boolean,
    message: String,
    type: SnackbarType = SnackbarType.SUCCESS,
    duration: Long = 2000, // <<< Ensure this is 2 seconds
    onDismiss: () -> Unit
) {
    // Auto-dismiss after duration
    LaunchedEffect(visible) {
        if (visible) {
            delay(duration)
            onDismiss()
        }
    }

    // Get color and icon based on type
    val backgroundColor = when (type) {
        SnackbarType.SUCCESS -> Color(0xFF4CAF50) // Green
        SnackbarType.ERROR -> Color(0xFFE53935) // Red
        SnackbarType.INFO -> Color(0xFF2196F3) // Blue
        SnackbarType.WARNING -> Color(0xFFE53935) // Red
    }

    val icon = when (type) {
        SnackbarType.SUCCESS -> Icons.Filled.CheckCircle
        SnackbarType.ERROR -> Icons.Filled.Close
        SnackbarType.INFO -> Icons.Filled.Info
        SnackbarType.WARNING -> Icons.Filled.Close
    }

    // Animation for slide in/out
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 300),
        label = "snackbarOffset"
    )

    // Animation for fade in/out
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "snackbarAlpha"
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
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                }
            }
        }
    }
}