package com.example.carzorrouserside.ui.theme.screens.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.carzorrouserside.ui.theme.appPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Privacy Policy",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(appPrimary) // Sets the purple background for the entire screen area
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // White content area with rounded top corners
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                PolicySection(
                    title = "1. Information We Collect",
                    content = "We collect information you provide directly to us, such as when you create an account, book a service, or contact customer support. This may include your name, email address, phone number, vehicle information, and payment details."
                )

                Spacer(modifier = Modifier.height(24.dp))

                PolicySection(
                    title = "2. How We Use Your Information",
                    content = "We use the information we collect to provide, maintain, and improve our services. This includes processing your bookings, facilitating payments, communicating with you, and personalizing your app experience."
                )

                Spacer(modifier = Modifier.height(24.dp))

                PolicySection(
                    title = "3. Information Sharing",
                    content = "We may share your information with our third-party service providers to fulfill your car wash requests. We do not sell your personal data. We may also share information if required by law."
                )

                Spacer(modifier = Modifier.height(24.dp))

                PolicySection(
                    title = "4. Data Security",
                    content = "We implement reasonable security measures to protect your information from unauthorized access, use, or disclosure. However, no internet-based service is 100% secure."
                )

                Spacer(modifier = Modifier.height(24.dp))

                PolicySection(
                    title = "5. Your Rights & Choices",
                    content = "You may review, update, or delete your account information at any time by accessing your account settings within the app. You can also opt out of receiving promotional communications from us."
                )
            }
        }
    }
}

/**
 * A reusable composable to display a section of the policy.
 * @param title The bolded heading of the section.
 * @param content The descriptive text for the section.
 */
@Composable
private fun PolicySection(title: String, content: String) {
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyPolicyScreenPreview() {
    // Wrap with your app's theme for an accurate preview
    // For example: CarzorroUserSideTheme { ... }
    PrivacyPolicyScreen(navController = rememberNavController())
}