package com.example.carzorrouserside.ui.theme.screens



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackPressed: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF836FFF),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Service Overview
                TermsSection(
                    number = "1",
                    title = "Service Overview",
                    content = "Carzorro provides users with a platform to book car wash and detailing services. We work with third-party service providers to fulfill these requests."
                )

                // 2. User Eligibility
                TermsSection(
                    number = "2",
                    title = "User Eligibility",
                    content = "Users must be at least 18 years old to use the app and agree to provide accurate information upon registration."
                )

                // 3. Booking and Cancellation
                TermsSection(
                    number = "3",
                    title = "Booking and Cancellation",
                    content = "App users can schedule appointments via the app. All bookings are subject to availability. Users may cancel a booking up to 2hr before the scheduled service without penalty. Late cancellations may incur a fee."
                )

                // 4. Payment Terms
                TermsSection(
                    number = "4",
                    title = "Payment Terms",
                    content = "Payment for services is processed via the app. Accepted methods include (credit/debit cards, mobile payments, etc.)."
                )

                // 5. Refund Policy
                TermsSection(
                    number = "5",
                    title = "Refund Policy",
                    content = "Refunds may be issued if a service is canceled by the user within the allowed cancellation window or due to a service failure."
                )
            }
        }
    }
}

@Composable
fun TermsSection(
    number: String,
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Title with number
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$number. $title",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 20.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyPolicyPreview() {
    MaterialTheme {
        PrivacyPolicyScreen()
    }
}