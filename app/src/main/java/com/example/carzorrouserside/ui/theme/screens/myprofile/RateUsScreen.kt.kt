package com.example.carzorrouserside.ui.theme.screens.feedback // Changed package to fit screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.R // Make sure you have a drawable for the illustration
import com.example.carzorrouserside.ui.theme.appPrimary

@Composable
fun ServiceCompleteScreen(navController: NavController) {

    // State for the two feedback sections
    var vendorRating by remember { mutableIntStateOf(0) }
    var vendorFeedback by remember { mutableStateOf("") }
    var packageRating by remember { mutableIntStateOf(0) }
    var packageFeedback by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable {
                            // TODO: Handle Skip navigation
                            navController.popBackStack()
                        }
                        .padding(8.dp)
                )
            }

            // Illustration
            Image(
                // Replace with your actual illustration from drawables
                painter = painterResource(id = R.drawable.buildings),
                contentDescription = "Service Complete Illustration",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1.2f),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header Text
            Text(
                text = "Service Complete! 🎉",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We have finished washing your car.\nThanks for choosing Carzorro",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Feedback Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeedbackSection(
                        title = "Satisfied with the vendor's Service?",
                        profileImageUrl = "https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500", // Dummy URL
                        rating = vendorRating,
                        onRatingChange = { vendorRating = it },
                        feedback = vendorFeedback,
                        onFeedbackChange = { vendorFeedback = it }
                    )

                    Divider(modifier = Modifier.padding(vertical = 24.dp))

                    FeedbackSection(
                        title = "Satisfied with the service package?",
                        profileImageUrl = "https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500", // Dummy URL
                        rating = packageRating,
                        onRatingChange = { packageRating = it },
                        feedback = packageFeedback,
                        onFeedbackChange = { packageFeedback = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    // TODO: Handle submit action
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp), // Using style from your reference code
                colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
            ) {
                Text(
                    text = "Submit",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * A reusable composable for a feedback input section.
 */
@Composable
private fun FeedbackSection(
    title: String,
    profileImageUrl: String,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    feedback: String,
    onFeedbackChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = profileImageUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Leave a rating and let others know about your experience!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Star Rating Bar
        StarRatingBar(rating = rating, onRatingChange = onRatingChange)

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback Text Field
        OutlinedTextField(
            value = feedback,
            onValueChange = onFeedbackChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = { Text("Please share more about your experience.") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
    }
}


/**
 * An interactive 5-star rating bar.
 */
@Composable
private fun StarRatingBar(
    maxStars: Int = 5,
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val iconColor = if (isSelected) Color(0xFFFFC107) else Color.LightGray

            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(40.dp) // Made stars larger for easier tapping
                    .clickable { onRatingChange(i) }
                    .padding(4.dp)
            )
        }
    }
}
@Composable
fun CarzorroUserSideTheme(content: @Composable () -> Unit) {
    TODO("Not yet implemented")
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ServiceCompleteScreenPreview() {
    CarzorroUserSideTheme { // Wrap with your app's theme
        ServiceCompleteScreen(navController = rememberNavController())
    }
}

