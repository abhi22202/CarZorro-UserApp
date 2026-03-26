package com.example.carzorrouserside.ui.theme.screens.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carzorrouserside.ui.theme.CarZorroColors
import com.example.carzorrouserside.ui.theme.viewmodel.login.OtpVerificationViewModel
import com.example.carzorrouserside.ui.theme.viewmodel.login.UserLoginViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    initialServerOtp: String = "", // Add this parameter
    onVerificationSuccess: (String) -> Unit = {},
    onBackPressed: () -> Unit = {},
    onResendOtp: (String) -> Unit = {},
    viewModel: OtpVerificationViewModel = hiltViewModel()
) {
    val TAG = "OtpVerificationScreen"

    // Log the received phone number and OTP
    LaunchedEffect(phoneNumber, initialServerOtp) {
        Log.d(TAG, "Phone number received from navigation: '$phoneNumber'")
        Log.d(TAG, "Phone number length: ${phoneNumber.length}")
        Log.d(TAG, "Initial Server OTP received: '$initialServerOtp'")
    }

    // State for OTP digits
    var otpDigits by remember { mutableStateOf(List(4) { "" }) }

    // State for timer
    var remainingSeconds by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var timerKey by remember { mutableIntStateOf(0) }

    // State for showing/hiding server OTP
    var showServerOtp by remember { mutableStateOf(true) }

    // Focus requesters for each OTP field
    val focusRequesters = remember { List(4) { FocusRequester() } }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Clipboard manager for copying OTP
    val clipboardManager = LocalClipboardManager.current

    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Use the passed initialServerOtp, or fall back to new OTP from resend
    val currentServerOtp = uiState.newOtp?.takeIf { it.isNotEmpty() } ?: initialServerOtp

    // Log the OTP states for debugging
    LaunchedEffect(initialServerOtp, uiState.newOtp, currentServerOtp) {
        Log.d(TAG, "🔢 OTP STATE DEBUG:")
        Log.d(TAG, "   ├─ Initial Server OTP (passed): '$initialServerOtp'")
        Log.d(TAG, "   ├─ New Server OTP (from resend): '${uiState.newOtp}'")
        Log.d(TAG, "   └─ Current Server OTP (effective): '$currentServerOtp'")
    }

    // Handle verification success
    LaunchedEffect(uiState.isVerificationSuccessful) {
        if (uiState.isVerificationSuccessful) {
            uiState.token?.let { token ->
                Log.d(TAG, "✅ OTP verification successful with token: $token")
                onVerificationSuccess(token)
            }
        }
    }


    // Handle resend OTP success
    LaunchedEffect(uiState.resendSuccess) {
        if (uiState.resendSuccess) {
            Log.d(TAG, "🔄 OTP resent successfully")
            Log.d(TAG, "🔢 New OTP received: ${uiState.newOtp}")
            snackbarHostState.showSnackbar("OTP resent successfully!")

            // Reset timer
            remainingSeconds = 60
            isTimerRunning = true
            timerKey++

            viewModel.clearResendSuccess()
        }
    }

    // Timer effect
    LaunchedEffect(key1 = timerKey) {
        while (isTimerRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        if (remainingSeconds == 0) {
            isTimerRunning = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarZorroColors.background)
    ) {
        // Back Button
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .padding(top = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = CarZorroColors.primary
            )
        }

        // Primary header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(CarZorroColors.primary)
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 24.dp, top = 50.dp)
            ) {
                Text(
                    text = "Verify your Phone",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = "Number",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Format phone number for display
                val displayPhoneNumber = if (phoneNumber.startsWith("+91")) {
                    "+91 ${phoneNumber.substring(3)}"
                } else if (phoneNumber.length == 10 && phoneNumber.all { it.isDigit() }) {
                    "+91 $phoneNumber"
                } else {
                    phoneNumber
                }

                Text(
                    text = "An OTP has been sent to your phone number $displayPhoneNumber",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    lineHeight = 20.sp
                )
            }
        }

        // OTP Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 220.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter the OTP",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = CarZorroColors.textPrimary
            )

            // Server OTP Test Section (show if any OTP exists)
            if (currentServerOtp.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD) // Light yellow background
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🧪 TEST MODE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF856404)
                            )

                            IconButton(
                                onClick = { showServerOtp = !showServerOtp },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (showServerOtp) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showServerOtp) "Hide OTP" else "Show OTP",
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (showServerOtp) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Server OTP:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF856404)
                                    )
                                    Text(
                                        text = currentServerOtp,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF856404),
                                        letterSpacing = 4.sp
                                    )

                                    // Show if this is a new OTP from resend
                                    if (uiState.newOtp?.isNotEmpty() == true) {
                                        Text(
                                            text = "🔄 RESENT OTP",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF28A745),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                // Copy and Auto-fill buttons
                                Row {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(currentServerOtp))
                                            Log.d(TAG, "📋 Copied OTP to clipboard: $currentServerOtp")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy OTP",
                                            tint = Color(0xFF856404),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            // Auto-fill the OTP
                                            val otpChars = currentServerOtp.take(4).toCharArray()
                                            otpDigits = otpChars.mapIndexed { index, char ->
                                                if (index < otpChars.size) char.toString() else ""
                                            }
                                            Log.d(TAG, "🤖 Auto-filled OTP: $currentServerOtp")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF856404)
                                        ),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = "Auto-fill",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Log when no OTP is available
                LaunchedEffect(Unit) {
                    Log.w(TAG, "⚠️ No server OTP available for display")
                }
            }

            // Rest of your existing UI code remains the same...
            Spacer(modifier = Modifier.height(24.dp))

            // OTP Input Boxes
            Row(
                horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                otpDigits.forEachIndexed { index, digit ->
                    OtpDigitInput(
                        value = digit,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                val updatedDigits = otpDigits.toMutableList().apply {
                                    this[index] = newValue
                                }
                                otpDigits = updatedDigits

                                // Auto-focus next field
                                if (newValue.isNotEmpty() && index < 3) {
                                    focusRequesters[index + 1].requestFocus()
                                }

                                // Auto-focus previous field when deleting
                                if (newValue.isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            }
                        },
                        focusRequester = focusRequesters[index],
                        isFilled = digit.isNotEmpty(),
                        enabled = !uiState.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resend OTP text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isTimerRunning) {
                    Text(
                        text = "Resend OTP in ",
                        fontSize = 14.sp,
                        color = CarZorroColors.textSecondary
                    )

                    Text(
                        text = formatTime(remainingSeconds),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CarZorroColors.primary
                    )
                } else {
                    Text(
                        text = "Didn't receive OTP? ",
                        fontSize = 14.sp,
                        color = CarZorroColors.textSecondary
                    )

                    Text(
                        text = "Resend",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CarZorroColors.primary,
                        modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                            Log.d(TAG, "🔄 Resend OTP clicked for phone: $phoneNumber")
                            remainingSeconds = 60
                            isTimerRunning = true
                            timerKey++
                            viewModel.resendOtp(phoneNumber)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Verify Button
            Button(
                onClick = {
                    val otp = otpDigits.joinToString("")

                    Log.d(TAG, "=== OTP VERIFICATION ATTEMPT ===")
                    Log.d(TAG, "Phone Number: $phoneNumber")
                    Log.d(TAG, "OTP Entered: $otp")
                    Log.d(TAG, "OTP Length: ${otp.length}")
                    Log.d(TAG, "Current Server OTP: $currentServerOtp")
                    Log.d(TAG, "================================")

                    val cleanedPhone = if (phoneNumber.startsWith("+91") && phoneNumber.length == 13) {
                        phoneNumber.substring(3)
                    } else phoneNumber

                    if (cleanedPhone.length == 10 && cleanedPhone.all { it.isDigit() }) {
                        viewModel.verifyOtp(cleanedPhone, otp)
                    } else {
                        Log.e(TAG, "Invalid phone number format: $phoneNumber")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CarZorroColors.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = CarZorroColors.gray,
                    disabledContentColor = CarZorroColors.textSecondary
                ),
                enabled = otpDigits.all { it.isNotEmpty() } && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Verify",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Request focus on the first OTP field when screen loads
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }
}

@Composable
fun OtpDigitInput(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    isFilled: Boolean,
    enabled: Boolean = true
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .background(
                color = CarZorroColors.cardBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isFilled) CarZorroColors.primary else CarZorroColors.border,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .padding(8.dp),
            textStyle = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (enabled) CarZorroColors.textPrimary else CarZorroColors.textSecondary
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            cursorBrush = SolidColor(CarZorroColors.primary),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    innerTextField()
                }
            }
        )
    }
}

// Format seconds to MM:SS format
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%01d:%02d Sec", minutes, remainingSeconds)
}