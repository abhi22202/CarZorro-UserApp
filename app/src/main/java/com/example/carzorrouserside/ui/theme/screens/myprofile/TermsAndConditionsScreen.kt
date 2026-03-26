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
fun TermsAndConditionsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Terms and Conditions",
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
                TermSection(
                    title = "1. Service Overview",
                    content = "Carzorro provides users with a platform to book car wash and detailing services. We work with third-party service providers to fulfill these requests."
                )

                Spacer(modifier = Modifier.height(24.dp))

                TermSection(
                    title = "2. User Eligibility",
                    content = "Users must be at least 18 years old to use the app and agree to provide accurate information upon registration."
                )

                Spacer(modifier = Modifier.height(24.dp))

                TermSection(
                    title = "3. Booking and Cancellation",
                    content = "App users can schedule appointments via the app. All bookings are subject to availability. Users may cancel a booking up to 2hr before the scheduled service without penalty. Late cancellations may incur a fee."
                )

                Spacer(modifier = Modifier.height(24.dp))

                TermSection(
                    title = "4. Payment Terms",
                    content = "Payment for services is processed via the app. Accepted methods include [credit/debit cards, mobile payments, etc.]."
                )

                Spacer(modifier = Modifier.height(24.dp))

                TermSection(
                    title = "5. Refund Policy",
                    content = "Refunds may be issued if a service is canceled by the user within the allowed cancellation window or due to a service failure."
                )
            }
        }
    }
}

/**
 * A reusable composable to display a section of the terms.
 * @param title The bolded heading of the section.
 * @param content The descriptive text for the section.
 */
@Composable
private fun TermSection(title: String, content: String) {
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
fun TermsAndConditionsScreenPreview() {
    // Wrap with your app's theme for an accurate preview
    // For example: CarzorroUserSideTheme { ... }
    TermsAndConditionsScreen(navController = rememberNavController())
}