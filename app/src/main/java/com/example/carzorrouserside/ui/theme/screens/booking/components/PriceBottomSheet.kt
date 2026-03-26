package com.example.carzorrouserside.ui.theme.screens.booking.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carzorrouserside.R
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.screens.homescreen.UserBidCard
import com.example.carzorrouserside.ui.theme.viewmodel.booking.PostBookingViewModel
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.HomepageServiceViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PriceBottomSheet(
    onCloseClick: () -> Unit,
    onFindVendorsClick: () -> Unit,
    isRequestSending: Boolean = false,
    onCancelRequest: () -> Unit = {},
    onShowCancelConfirmation: () -> Unit = {},
    initialPrice: String,
    homepageServiceViewModel: HomepageServiceViewModel = hiltViewModel()
) {
    var priceValue by remember { mutableStateOf(initialPrice) }
    val initialSliderPos = remember(initialPrice) {
        val price = initialPrice.toFloatOrNull() ?: 500f
        ((price - 300f) / 500f).coerceIn(0f, 1f)
    }
    var sliderPosition by remember { mutableFloatStateOf(initialSliderPos) }
    val scrollState = rememberScrollState()

    var loadingTextIndex by remember { mutableIntStateOf(0) }
    val loadingTexts = listOf(
        "Sending request to vendors...",
        "Waiting for replies..."
    )

    // Animate "sending" text
    LaunchedEffect(isRequestSending) {
        if (isRequestSending) {
            while (true) {
                delay(2000)
                loadingTextIndex = (loadingTextIndex + 1) % loadingTexts.size
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dotsAnimation")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )

    val postBookingViewModel: PostBookingViewModel = hiltViewModel()
    val uiState by homepageServiceViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(750.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Sheet handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            // Close button
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Header
                if (isRequestSending) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedContent(
                            targetState = loadingTextIndex,
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn(animationSpec = tween(500)))
                                    .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                                    .using(SizeTransform(clip = false))
                            },
                            label = "loadingTextAnimation"
                        ) { index ->
                            Text(
                                text = loadingTexts[index],
                                color = appPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            repeat(3) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            appPrimary.copy(
                                                alpha = if (index == loadingTextIndex % 3) dotAlpha else 0.3f
                                            )
                                        )
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Set your price",
                        color = appPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Vendors are more likely to respond quickly if the prices match their rates.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Price display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = appPrimary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = priceValue,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = appPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rocket),
                            contentDescription = "Tip",
                            tint = appPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Higher the price, higher the chance of getting a vendor",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Price slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .align(Alignment.Center)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(sliderPosition)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(appPrimary)
                            .align(Alignment.CenterStart)
                    )

                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
                            val newPrice = (300 + (500 * it)).toInt()
                            priceValue = newPrice.toString()
                            postBookingViewModel.setAmount(priceValue)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.surface,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        ),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .shadow(elevation = 4.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(appPrimary.copy(alpha = 0.2f))
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(appPrimary)
                                    )
                                }
                            }
                        }
                    )
                }

//                Divider(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 16.dp),
//                    color = Color.LightGray.copy(alpha = 0.4f)
//                )

                // ✅ Animated vendor offers section
//                if (isRequestSending && uiState.activeBids.isNotEmpty()) {
//                    AnimatedVisibility(
//                        visible = true,
//                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
//                        exit = fadeOut()
//                    ) {
//                        val context = LocalContext.current
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 12.dp)
//                        ) {
//                            Text(
//                                text = "Offers from vendors",
//                                fontWeight = FontWeight.Bold,
//                                fontSize = 18.sp,
//                                color = appPrimary,
//                                modifier = Modifier.padding(bottom = 8.dp)
//                            )
//
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(max = 280.dp)
//                                    .verticalScroll(rememberScrollState())
//                            ) {
//                                uiState.activeBids.forEach { bid ->
//                                    UserBidCard(
//                                        bid = bid,
//                                        onAccept = { selectedBid ->
//                                            Toast.makeText(
//                                                context,
//                                                "Accepted ${selectedBid.vendorName}",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        },
//                                        onReject = { selectedBid ->
//                                            Toast.makeText(
//                                                context,
//                                                "Rejected ${selectedBid.vendorName}",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }

                Spacer(modifier = Modifier.height(70.dp))
            }

            // Bottom button
            Button(
                onClick = {
                    if (isRequestSending) {
                        postBookingViewModel.clearPendingBooking()
                        onShowCancelConfirmation()
                    } else {
                        onFindVendorsClick()
                        postBookingViewModel.markBookingAsSendingRequest(postBookingViewModel.bookingId.value)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRequestSending) MaterialTheme.colorScheme.tertiaryContainer else appPrimary,
                    contentColor = if (isRequestSending) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = if (isRequestSending) "Cancel Request" else "Find Vendors",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
