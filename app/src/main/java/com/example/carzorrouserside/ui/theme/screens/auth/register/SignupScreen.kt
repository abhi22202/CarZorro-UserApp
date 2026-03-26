package com.example.carzorrouserside.ui.theme.screens.auth.register

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carzorrouserside.data.model.register.RegisterRequest
import com.example.carzorrouserside.data.model.register.RegisterResponse
import com.example.carzorrouserside.ui.theme.viewmodel.register.SignUpViewModel
import com.example.carzorrouserside.util.NetworkResult
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.LightBackground
import com.example.carzorrouserside.ui.theme.LightSurface
import com.example.carzorrouserside.ui.theme.LightOnSurface
import com.example.carzorrouserside.ui.theme.LightGray
import com.example.carzorrouserside.ui.theme.LightBorder
import com.example.carzorrouserside.ui.theme.LightError
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignInClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onRegistrationSuccess: (RegisterRequest, RegisterResponse) -> Unit = { _, _ -> },
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val TAG = "SignUpScreen"

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("male") }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    val registrationState by viewModel.registrationState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Handle registration response correctly
    LaunchedEffect(registrationState) {
        registrationState?.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "✅ Registration successful in SignUpScreen")
                    Log.d(TAG, "   ├─ User ID: ${result.data.data?.userId}")
                    Log.d(TAG, "   ├─ Phone: $phoneNumber")
                    Log.d(TAG, "   └─ OTP: ${result.data.data?.otp}")

                    val finalBirthDate = if (selectedDate.isNotEmpty()) selectedDate else birthDate
                    val formattedDate = convertDateToApiFormat(finalBirthDate)

                    val registerRequest = RegisterRequest(
                        fullName = fullName.trim(),
                        email = email.trim(),
                        dob = formattedDate,
                        phone = phoneNumber.trim(),
                        gender = selectedGender
                    )

                    onRegistrationSuccess(registerRequest, result.data)
                    viewModel.clearRegistrationState()
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "❌ Registration failed: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "🔄 Registration loading...")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        // Professional header with proper spacing from status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    color = appPrimary, // Keep the original purple as requested
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 60.dp) // Added top padding to avoid status bar
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Join us today",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Sign In",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.clickable { onSignInClick() }
                    )
                }
            }
        }

        // Professional registration content card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(top = 160.dp, bottom = 20.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Show error message if registration failed
                registrationState?.let { result ->
                    if (result is NetworkResult.Error) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = LightError.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = result.message,
                                color = LightError,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // Full Name Field
                Text(
                    text = "Full Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightOnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            "Enter your full name",
                            color = LightGray
                        )
                    },
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightOnSurface,
                        unfocusedTextColor = LightOnSurface,
                        focusedBorderColor = appPrimary,
                        unfocusedBorderColor = LightBorder,
                        focusedContainerColor = LightSurface,
                        unfocusedContainerColor = LightSurface
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Email Field
                Text(
                    text = "Email Address",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightOnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            "Enter your email",
                            color = LightGray
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightOnSurface,
                        unfocusedTextColor = LightOnSurface,
                        focusedBorderColor = appPrimary,
                        unfocusedBorderColor = LightBorder,
                        focusedContainerColor = LightSurface,
                        unfocusedContainerColor = LightSurface
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Date of Birth Field
                Text(
                    text = "Date of Birth",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightOnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ProfessionalDateOfBirthField(
                    selectedDate = if (selectedDate.isNotEmpty()) selectedDate else birthDate,
                    onDateClick = { if (!isLoading) showDatePicker = true },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Gender Field
                Text(
                    text = "Gender",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightOnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ProfessionalGenderDropdownField(
                    selectedGender = selectedGender,
                    onGenderSelected = { selectedGender = it },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Phone Number Field
                Text(
                    text = "Phone Number",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightOnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ProfessionalPhoneNumberField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Register Button
                Button(
                    onClick = {
                        val finalBirthDate = if (selectedDate.isNotEmpty()) selectedDate else birthDate
                        Log.d(TAG, "🚀 Register button clicked")
                        Log.d(TAG, "   ├─ Full Name: '$fullName'")
                        Log.d(TAG, "   ├─ Email: '$email'")
                        Log.d(TAG, "   ├─ Birth Date: '$finalBirthDate'")
                        Log.d(TAG, "   ├─ Phone: '$phoneNumber'")
                        Log.d(TAG, "   └─ Gender: '$selectedGender'")

                        viewModel.registerUser(
                            fullName = fullName,
                            email = email,
                            birthDate = finalBirthDate,
                            phoneNumber = phoneNumber,
                            gender = selectedGender
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = LightBorder,
                        disabledContentColor = LightGray
                    ),
                    enabled = !isLoading && viewModel.validateInputs(
                        fullName = fullName,
                        email = email,
                        birthDate = if (selectedDate.isNotEmpty()) selectedDate else birthDate,
                        phoneNumber = phoneNumber
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Terms text
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(
                            color = LightGray,
                            fontSize = 13.sp
                        )) {
                            append("By creating an account, you agree to our ")
                        }

                        withStyle(style = SpanStyle(
                            color = appPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )) {
                            append("Terms of Service")
                        }

                        withStyle(style = SpanStyle(
                            color = LightGray,
                            fontSize = 13.sp
                        )) {
                            append(" and ")
                        }

                        withStyle(style = SpanStyle(
                            color = appPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )) {
                            append("Privacy Policy")
                        }
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTermsClick() }
                )
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            birthDate = selectedDate
                            showDatePicker = false
                        }
                    ) {
                        Text("OK", color = appPrimary)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) {
                        Text("Cancel", color = LightGray)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = appPrimary,
                        todayDateBorderColor = appPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun ProfessionalDateOfBirthField(
    selectedDate: String,
    onDateClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onDateClick() },
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedDate.isNotEmpty()) selectedDate else "DD/MM/YYYY",
                color = if (selectedDate.isNotEmpty()) LightOnSurface else LightGray,
                fontSize = 16.sp
            )

            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select date",
                tint = if (enabled) appPrimary else LightGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalGenderDropdownField(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("male", "female", "other")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && enabled }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedGender.replaceFirstChar { it.uppercase() },
                    color = LightOnSurface,
                    fontSize = 16.sp
                )

                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(LightSurface)
        ) {
            genderOptions.forEach { gender ->
                DropdownMenuItem(
                    text = {
                        Text(
                            gender.replaceFirstChar { it.uppercase() },
                            color = LightOnSurface
                        )
                    },
                    onClick = {
                        onGenderSelected(gender)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProfessionalPhoneNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            // Country code
            Card(
                modifier = Modifier.padding(4.dp),
                colors = CardDefaults.cardColors(containerColor = LightBackground),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                ) {




                    Text(
                        text = "+91",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightOnSurface
                    )
                }
            }

            // Phone number input
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.all { char -> char.isDigit() } && newValue.length <= 10) {
                        onValueChange(newValue)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp),
                placeholder = {
                    Text(
                        "Enter phone number",
                        color = LightGray
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LightOnSurface,
                    unfocusedTextColor = LightOnSurface,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true,
                enabled = enabled
            )
        }
    }
}

// Helper function to convert date format for API
private fun convertDateToApiFormat(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        Log.e("SignUpScreen", "Date conversion failed: ${e.message}")
        dateString
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpScreen(
            onSignInClick = { },
            onTermsClick = { },
            onRegistrationSuccess = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignUpScreenDarkPreview() {
    MaterialTheme {
        SignUpScreen(
            onSignInClick = { },
            onTermsClick = { },
            onRegistrationSuccess = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfessionalDateOfBirthFieldPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Empty Date Field:")
            ProfessionalDateOfBirthField(
                selectedDate = "",
                onDateClick = { },
                enabled = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Selected Date Field:")
            ProfessionalDateOfBirthField(
                selectedDate = "15/06/1990",
                onDateClick = { },
                enabled = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfessionalGenderDropdownFieldPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfessionalGenderDropdownField(
                selectedGender = "male",
                onGenderSelected = { },
                enabled = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfessionalPhoneNumberFieldPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Empty Phone Field:")
            ProfessionalPhoneNumberField(
                value = "",
                onValueChange = { },
                enabled = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Phone with Number:")
            ProfessionalPhoneNumberField(
                value = "9876543210",
                onValueChange = { },
                enabled = true
            )
        }
    }
}