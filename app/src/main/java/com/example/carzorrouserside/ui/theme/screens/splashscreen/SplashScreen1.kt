package com.example.carzorrouserside.ui.theme.screens.splashscreen

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carzorrouserside.R
import com.example.carzorrouserside.ui.theme.appPrimary

import kotlinx.coroutines.delay

@Composable
fun SplashScreen1(onNavigateToNextScreen: () -> Unit) {
    val scale = remember { Animatable(0f) }


    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = {
                    OvershootInterpolator(2f).getInterpolation(it)
                }
            )
        )

        delay(2000L)
        onNavigateToNextScreen()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appPrimary)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 150.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "CarZorro Logo",
                modifier = Modifier
                    .size(200.dp)

            )

        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {

            Image(
                painter = painterResource(id = R.drawable.car_logo),
                contentDescription = "Car Illustration",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp)
                    .scale(scale.value)
            )
        }
    }
}