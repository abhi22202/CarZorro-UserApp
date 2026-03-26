// UserRegistrationOtpVerificationScreen.kt
package com.example.carzorrouserside.ui.theme.screens.auth.register

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
import com.example.carzorrouserside.ui.theme.viewmodel.register.SignUpOtpViewModel
import com.example.carzorrouserside.data.model.register.RegisterRequest
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationOtpVerificationScreen(
    phoneNumber: String,
    userId: Int,
    initialServerOtp: String = "",
    originalRegisterRequest: RegisterRequest, // Pass the original registration request for resending
    onVerificationSuccess: (String) -> Unit = {},
    onBackPressed: () -> Unit = {},
    viewModel: SignUpOtpViewModel = hiltViewModel()
) {
    val TAG = "UserRegistrationOtpScreen"

    // Log initialization
    LaunchedEffect(phoneNumber, userId, initialServerOtp) {
        Log.d(TAG, "🔐 User Registration OTP Screen Initialized")
        Log.d(TAG, "   ├─ Phone number: '$phoneNumber'")
        Log.d(TAG, "   ├─ User ID: $userId")
        Log.d(TAG, "   └─ Initial OTP: '$initialServerOtp'")
    }

    // UI State
    var otpDigits by remember { mutableStateOf(List(4) { "" }) }
    var remainingSeconds by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var timerKey by remember { mutableIntStateOf(0) }
    var showServerOtp by remember { mutableStateOf(true) }

    // Focus management
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // Collect ViewModel state
    val uiState by viewModel.uiState.collectAsState()

    // Determine current OTP to display
    val currentServerOtp = uiState.newOtpFromServer?.takeIf { it.isNotEmpty() } ?: initialServerOtp

    // Log OTP state changes
    LaunchedEffect(initialServerOtp, uiState.newOtpFromServer, currentServerOtp) {
        Log.d(TAG, "🔢 REGISTRATION OTP STATE DEBUG:")
        Log.d(TAG, "   ├─ Initial Server OTP: '$initialServerOtp'")
        Log.d(TAG, "   ├─ New OTP from Resend: '${uiState.newOtpFromServer}'")
        Log.d(TAG, "   └─ Current Effective OTP: '$currentServerOtp'")
    }

    // Handle verification success
    LaunchedEffect(uiState.isVerificationSuccessful) {
        if (uiState.isVerificationSuccessful) {
            uiState.authenticationToken?.let { token ->
                Log.d(TAG, "✅ Registration OTP verification successful with token: $token")
                onVerificationSuccess(token)
            }
        }
    }

    // Handle verification errors
    LaunchedEffect(uiState.verificationErrorMessage) {
        uiState.verificationErrorMessage?.let { errorMessage ->
            Log.e(TAG, "❌ Verification Error: $errorMessage")
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearVerificationError()
        }
    }

    // Handle resend errors
    LaunchedEffect(uiState.resendErrorMessage) {
        uiState.resendErrorMessage?.let { errorMessage ->
            Log.e(TAG, "❌ Resend Error: $errorMessage")
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearResendError()
        }
    }

    // Handle resend success
    LaunchedEffect(uiState.isResendSuccessful) {
        if (uiState.isResendSuccessful) {
            Log.d(TAG, "🔄 OTP resent successfully")
            Log.d(TAG, "🔢 New OTP received: ${uiState.newOtpFromServer}")
            snackbarHostState.showSnackbar("OTP resent successfully!")

            // Reset timer
            remainingSeconds = 60
            isTimerRunning = true
            timerKey++

            viewModel.clearResendSuccess()
        }
    }

    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            if (message.isNotEmpty()) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    // Timer effect
    LaunchedEffect(key1 = timerKey) {
        while (isTimerRunning && remainingSeconds > 0) {
            (1000)
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
                contentDescription = "Back to Registration",
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
                    text = "Complete your",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = "Registration",
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
                    text = "An OTP has been sent to $displayPhoneNumber to complete your account setup",
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
                text = "Enter the verification code",
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
                        containerColor = Color(0xFFE8F5E8) // Light green background
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
                                text = "🎯 REGISTRATION MODE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF155724)
                            )

                            IconButton(
                                onClick = { showServerOtp = !showServerOtp },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (showServerOtp) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showServerOtp) "Hide OTP" else "Show OTP",
                                    tint = Color(0xFF155724),
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
                                        text = "Registration OTP:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF155724)
                                    )
                                    Text(
                                        text = currentServerOtp,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF155724),
                                        letterSpacing = 4.sp
                                    )

                                    if (userId > 0) {
                                        Text(
                                            text = "User ID: #$userId",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF155724),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    // Show if this is a new OTP from resend
                                    if (uiState.newOtpFromServer?.isNotEmpty() == true) {
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
                                            Log.d(TAG, "📋 Copied Registration OTP to clipboard: $currentServerOtp")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Registration OTP",
                                            tint = Color(0xFF155724),
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
                                            Log.d(TAG, "🤖 Auto-filled Registration OTP: $currentServerOtp")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF155724)
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
                    Log.w(TAG, "⚠️ No server OTP available for Registration verification")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OTP Input Boxes
            Row(
                horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                otpDigits.forEachIndexed { index, digit ->
                    RegistrationOtpDigitInput(
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
                        enabled = !uiState.isLoadingVerification
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
                        text = "Resend code in ",
                        fontSize = 14.sp,
                        color = CarZorroColors.textSecondary
                    )

                    Text(
                        text = formatRegistrationOtpTime(remainingSeconds),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CarZorroColors.primary
                    )
                } else {
                    Text(
                        text = "Didn't receive the code? ",
                        fontSize = 14.sp,
                        color = CarZorroColors.textSecondary
                    )

                    if (uiState.isLoadingResend) {
                        CircularProgressIndicator(
                            color = CarZorroColors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "Resend",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CarZorroColors.primary,
                            modifier = Modifier.clickable(enabled = !uiState.isLoadingResend) {
                                Log.d(TAG, "🔄 Resend Registration OTP clicked for phone: $phoneNumber")
                                remainingSeconds = 60
                                isTimerRunning = true
                                timerKey++
                                viewModel.resendRegistrationOtp(originalRegisterRequest)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Complete Registration Button
            Button(
                onClick = {
                    val otp = otpDigits.joinToString("")

                    Log.d(TAG, "=== REGISTRATION OTP VERIFICATION ATTEMPT ===")
                    Log.d(TAG, "User ID: $userId")
                    Log.d(TAG, "Phone Number: $phoneNumber")
                    Log.d(TAG, "OTP Entered: $otp")
                    Log.d(TAG, "OTP Length: ${otp.length}")
                    Log.d(TAG, "Current Server OTP: $currentServerOtp")
                    Log.d(TAG, "=======================================")

                    val cleanedPhone = if (phoneNumber.startsWith("+91") && phoneNumber.length == 13) {
                        phoneNumber.substring(3)
                    } else phoneNumber

                    if (cleanedPhone.length == 10 && cleanedPhone.all { it.isDigit() }) {
                        viewModel.verifyRegistrationOtp(cleanedPhone, otp, userId)
                    } else {
                        Log.e(TAG, "Invalid phone number format for Registration: $phoneNumber")
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
                enabled = otpDigits.all { it.isNotEmpty() } && !uiState.isLoadingVerification
            ) {
                if (uiState.isLoadingVerification) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Complete Registration",
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
fun RegistrationOtpDigitInput(
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

// Format seconds to MM:SS format for Registration
private fun formatRegistrationOtpTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%01d:%02d Sec", minutes, remainingSeconds)
}