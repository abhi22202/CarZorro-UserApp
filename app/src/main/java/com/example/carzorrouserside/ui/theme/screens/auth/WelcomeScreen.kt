package com.example.carzorrouserside.ui.theme.screens.auth

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.carzorrouserside.R
import com.example.carzorrouserside.ui.theme.appPrimary


@Composable
fun WelcomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header text with Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hello There,",
                    color = appPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )

                // Skip button
                TextButton(
                    onClick = {
                        // Navigate to CarWashAppScreen when Skip is clicked
                        navController.navigate("home_screen") {
                            popUpTo("welcome_screen") {
                                inclusive = true
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Skip",
                        color = appPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            Text(
                text = "We are happy to see you here.",
                color = Color.DarkGray,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.welcome),
                    contentDescription = "Person using car wash app",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Order text
            Text(
                text = "Order a wash",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Animated arrows with staggered timing
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                AnimatedArrow(delay = 0)
                AnimatedArrow(
                    modifier = Modifier.offset(y = (-12).dp),
                    delay = 300
                )
            }

            // "See the Packages" button
            Button(
                onClick = {
                    // Navigate to PackagesScreen when "See the Packages" is clicked
                    navController.navigate("package_screen")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appPrimary
                )
            ) {
                Text(
                    text = "See the Packages",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}

// Update Preview to provide a NavController
@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(navController = rememberNavController())
}
@Composable
fun AnimatedArrow(
    modifier: Modifier = Modifier,
    delay: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arrow_animation")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                delayMillis = delay,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset_animation"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                delayMillis = delay,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    Icon(
        painter = painterResource(id = R.drawable.ic_arrow_down),
        contentDescription = null,
        modifier = modifier
            .size(36.dp)
            .offset(y = offsetY.dp)
            .alpha(alpha),
        tint = Color.Black
    )
}

