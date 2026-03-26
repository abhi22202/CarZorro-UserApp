package com.example.carzorrouserside.ui.theme.screens.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.carzorrouserside.R
import com.example.carzorrouserside.ui.theme.appPrimary

@Composable
fun BookingConfirmationScreen(
    navController: NavController,
    paymentAmount: Double,
    paymentMethod: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Confirmation Animation/Image
            Box(
                modifier = Modifier
                    .size(203.dp)
                    .padding(bottom = 76.dp)
            ) {
                // Using a placeholder image - replace with Lottie animation if available
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Confirmation",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // WOOHOO! Text with emoji
            Row(
                modifier = Modifier.padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "WOOHOO!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF7D64FF),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Emoji icon - using a small image or emoji
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Celebration",
                    modifier = Modifier.size(31.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Service booked message
            Text(
                text = "Your service has been booked.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF212121),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 31.dp)
            )

            // Payment message - different based on payment method
            Row(
                modifier = Modifier.padding(bottom = 304.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Payment of ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center
                )
                // Rupee symbol
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Rupee",
                    modifier = Modifier.size(14.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = " ${paymentAmount.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8770FF),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (paymentMethod == "pay_after_service" || paymentMethod == "Pay on service") {
                        " will be collected after service."
                    } else {
                        " has been received."
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center
                )
            }

            // Go to dashboard button
            Button(
                onClick = {
                    navController.navigate(com.example.carzorrouserside.ui.theme.navigation.Routes.HOME_SCREEN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9D8CFF)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "Go to dashboard",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }
        }
    }
}

