package com.example.carzorrouserside.ui.theme.screens.auth

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carzorrouserside.R
import com.example.carzorrouserside.ui.theme.CarZorroColors
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.viewmodel.login.UserLoginViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onSendOtpClick: (String) -> Unit = {},
    onContinueAsGuestClick: () -> Unit = {},
    viewModel: UserLoginViewModel = hiltViewModel()
) {
    val TAG = "LoginScreen"

    var phoneNumber by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    var storedPhoneNumber by remember { mutableStateOf("") }

    // ... (All your LaunchedEffects remain the same) ...
    LaunchedEffect(uiState) {
        Log.d(TAG, "🔄 UI State changed: $uiState")
    }
    LaunchedEffect(uiState.otpSent) {
        if (uiState.otpSent && storedPhoneNumber.isNotEmpty()) {
            uiState.message?.let { snackbarHostState.showSnackbar(it) }
            viewModel.clearOtpSentState()
            val serverOtp = uiState.serverOtp?.toString() ?: ""
            onSendOtpClick("$storedPhoneNumber/$serverOtp")
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.clearStates()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // MODIFIED
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    color = appPrimary, // Kept as per request
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 60.dp)
            ) {
                Text(
                    text = "Welcome Back",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Sign in to continue",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Sign Up",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.clickable { onSignUpClick() }
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 180.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // MODIFIED
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Phone Number",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, // MODIFIED
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()
                )
                Text(
                    text = "Enter your mobile number to get started",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // MODIFIED
                    modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // MODIFIED
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Card(
                            modifier = Modifier.padding(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // MODIFIED
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "India Flag",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+91",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface // MODIFIED
                                )
                            }
                        }
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                    phoneNumber = it
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "Enter phone number",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, // MODIFIED
                                    fontSize = 12.sp
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            colors = OutlinedTextFieldDefaults.colors( // MODIFIED
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = {
                        if (phoneNumber.length == 10 && !uiState.isLoading) {
                            storedPhoneNumber = phoneNumber
                            viewModel.sendOtp(phoneNumber)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors( // MODIFIED for disabled state
                        containerColor = appPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    enabled = phoneNumber.length == 10 && !uiState.isLoading,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Send OTP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Continue without Login",
                    color = appPrimary, // Kept as per request
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onContinueAsGuestClick() }
                        .padding(8.dp)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // MODIFIED
            shape = RoundedCornerShape(12.dp),

            ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // MODIFIED
                        fontSize = 13.sp
                    )) {
                        append("By continuing, you agree to our ")
                    }
                    withStyle(style = SpanStyle(
                        color = appPrimary, // Kept as per request
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )) {
                        append("Terms of Service")
                    }
                    withStyle(style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // MODIFIED
                        fontSize = 13.sp
                    )) {
                        append(" and ")
                    }
                    withStyle(style = SpanStyle(
                        color = appPrimary, // Kept as per request
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )) {
                        append("Privacy Policy")
                    }
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onTermsClick() }
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
