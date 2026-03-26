package com.example.carzorrouserside.ui.theme.screens

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.viewmodel.profile.ProfileViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Update Profile Screen matching Figma design
 * Design: https://www.figma.com/design/LMVSAOmKLRySyuL4b9EOfV/Carzorro?node-id=12-4781
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    navController: NavController,
    userPreferencesManager: UserPreferencesManager,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Form state
    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var alternateMobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Refresh profile data when screen is opened
    LaunchedEffect(Unit) {
        profileViewModel.refreshUserBasicDetails()
    }

    // Auto-populate fields when user data is loaded or updated
    // Use a key that changes when screen is opened to ensure fields are populated
    LaunchedEffect(profileState.userBasicDetails, profileState.isLoading) {
        if (!profileState.isLoading && profileState.userBasicDetails != null) {
            profileState.userBasicDetails?.let { user ->
                name = user.fullName ?: ""
                mobileNumber = user.phone ?: ""
                // Ensure alt_phone is properly populated - handle null, empty, and blank strings
                // Log to debug
                android.util.Log.d("UpdateProfileScreen", "Loading user data - altPhone from API: '${user.altPhone}'")
                alternateMobileNumber = user.altPhone ?: ""
                email = user.email ?: ""
                dob = user.dob ?: ""
                gender = user.gender ?: ""
                android.util.Log.d("UpdateProfileScreen", "Set alternateMobileNumber to: '$alternateMobileNumber'")
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Convert URI to File and upload
            val imageFile = uriToFile(context, it)
            imageFile?.let { file ->
                profileViewModel.uploadProfileImage(file)
            }
        }
    }
    
    // Function to launch image picker
    fun launchImagePicker() {
        imagePickerLauncher.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
        )
    }

    // Handle update success - refresh profile data before navigating back
    LaunchedEffect(profileState.updateSuccess) {
        if (profileState.updateSuccess) {
            // Show success toast
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            // Wait a bit longer to ensure the API has updated the data and refresh is complete
            kotlinx.coroutines.delay(800)
            profileViewModel.resetUpdateState()
            navController.popBackStack()
        }
    }

    // Handle image upload success
    LaunchedEffect(profileState.imageUploadSuccess) {
        if (profileState.imageUploadSuccess) {
            profileViewModel.resetImageUploadState()
            selectedImageUri = null // Clear selected image after successful upload
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with purple background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(305.dp)
                    .background(appPrimary)
            ) {
                // Back button and title - positioned properly with status bar padding
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 56.dp, // Account for status bar
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Basic Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Profile picture with edit icon
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 20.dp)
                ) {
                    // Profile picture
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> {
                                // Show selected image
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(selectedImageUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Selected Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.logo),
                                    error = painterResource(id = R.drawable.logo)
                                )
                            }
                            !profileState.userBasicDetails?.profilePic.isNullOrBlank() -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(profileState.userBasicDetails?.profilePic)
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
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                        
                        // Show loading indicator if uploading
                        if (profileState.isUploadingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = appPrimary,
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    // Edit icon overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .clickable(enabled = !profileState.isUploadingImage) {
                                launchImagePicker()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Picture",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Form section with white background
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 25.dp)
                        .padding(top = 24.dp)
                        .padding(bottom = 90.dp) // Add bottom padding to account for button
                ) {
                    // Name field
                    ProfileTextField(
                        label = "Enter your name",
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mobile Number field
                    ProfileTextField(
                        label = "Enter your Mobile Number",
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        keyboardType = KeyboardType.Phone,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Alternate Mobile Number field
                    ProfileTextField(
                        label = "Enter your Alternate mobile number",
                        value = alternateMobileNumber,
                        onValueChange = { alternateMobileNumber = it },
                        keyboardType = KeyboardType.Phone,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Email field
                    ProfileTextField(
                        label = "Enter your Email ID",
                        value = email,
                        onValueChange = { email = it },
                        keyboardType = KeyboardType.Email,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Date of Birth field
                    ProfileTextField(
                        label = "Date of Birth (YYYY-MM-DD)",
                        value = dob,
                        onValueChange = { dob = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Gender field
                    GenderDropdown(
                        label = "Gender",
                        selectedGender = gender,
                        onGenderSelected = { gender = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Show image upload error if any
                    if (profileState.imageUploadError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = profileState.imageUploadError ?: "Failed to upload image",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // State and Pincode row
                    // NOTE: State and Pincode are NOT saved to the backend API
                    // The edit-profile API only supports: full_name, email, dob, phone, alt_phone, gender
                    // These fields are shown in the UI for display purposes only
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        ProfileTextField(
                            label = "Enter your State",
                            value = state,
                            onValueChange = { state = it },
                            modifier = Modifier.weight(1f)
                        )

                        ProfileTextField(
                            label = "Enter your Pincode",
                            value = pincode,
                            onValueChange = { pincode = it },
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Show error message if update failed
                    if (profileState.updateError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = profileState.updateError ?: "Failed to update profile",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Edit button at bottom
        Button(
            onClick = {
                android.util.Log.d("UpdateProfileScreen", "Saving profile - alternateMobileNumber: '$alternateMobileNumber'")
                profileViewModel.editProfile(
                    fullName = name,
                    email = email,
                    dob = dob,
                    phone = mobileNumber,
                    altPhone = alternateMobileNumber.trim().takeIf { it.isNotEmpty() } ?: null,
                    gender = gender
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
                .padding(bottom = 24.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appPrimary
            ),
            enabled = !profileState.isUpdating
        ) {
            if (profileState.isUpdating) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF636363),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF9D8CFF).copy(alpha = 0.05f),
                unfocusedContainerColor = Color(0xFF9D8CFF).copy(alpha = 0.05f),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdown(
    label: String,
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val genders = listOf("male", "female", "other")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF636363),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedGender.ifBlank { "Select Gender" },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF9D8CFF).copy(alpha = 0.05f),
                    unfocusedContainerColor = Color(0xFF9D8CFF).copy(alpha = 0.05f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genders.forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(text = gender.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onGenderSelected(gender)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Helper function to convert URI to File
 */
private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "profile_pic_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        
        file
    } catch (e: Exception) {
        android.util.Log.e("UpdateProfileScreen", "Error converting URI to File", e)
        null
    }
}

