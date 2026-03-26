package com.example.carzorrouserside.ui.theme.screens.booking.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.carzorrouserside.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.HomepageServiceViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceTrackingScreen(
    navController: NavController,
    homepageServiceViewModel: HomepageServiceViewModel,
    bookingId: Int,
    initialStatus: String = "confirmed",
    estimatedArrivalTime: String = "11:45 AM"
) {
    val uiState by homepageServiceViewModel.uiState.collectAsState()

    val booking = uiState.activeBookingData?.booking
    val bookingStatus = booking?.booking_status

    val startOtp = booking?.startOtp
    val endOtp = booking?.endOtp
    val price = booking?.price
    val vendor = booking?.vendor
    val mode =booking?.paymentMode
    val paymentStatus = booking?.paymentStatus
    val dateTime = booking?.dateTime
    val showPaymentPopup =
        bookingStatus?.lowercase() == "service_completed" &&
                paymentStatus?.lowercase() == "pending"



    val (formattedDate, formattedTime) =
        formatBookingDateTime(dateTime)


    val serviceStatuses = listOf("Confirmed", "Completed")
    var currentStatus by remember { mutableStateOf(0) }

    fun mapApiStatusToIndex(status: String): Int =
        when (status.lowercase()) {
            "completed", "service_completed", "car_delivered" -> 1
            else -> 0
        }

    LaunchedEffect(initialStatus) {
        currentStatus = mapApiStatusToIndex(initialStatus)
    }

    val headingTitle = when (bookingStatus?.lowercase()) {
        "vehicle_picked_up" -> "Vehicle picked up by vendor"
        "service_started" ->"Service in Progress"
        "service_completed" ->"Service Completed"
        "completed",
        "car_delivered" -> "Service completed"
        else -> "Service is confirmed.."
    }
    val otpLabelAndValue: Pair<String, Int?>? = when (bookingStatus?.lowercase()) {

        // 🔹 Show START OTP
        "confirmed",
        "vehicle_picked_up" -> {
            startOtp?.let { "CODE" to it }
        }

        // 🔹 Show END OTP
        "service_started" -> {
            endOtp?.let { "END CODE" to it }
        }

        // 🔹 AFTER completion → show NOTHING
        "service_completed",
        "completed",
        "car_delivered" -> {
            null
        }

        else -> null
    }




    val headingSubtitle =
        if (currentStatus == 0) "Arriving at $estimatedArrivalTime"
        else "Thank you for choosing CarZorro"

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            sheetPeekHeight = 420.dp,
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            sheetContainerColor = Color.White, // ✅ remove purple
            sheetContent = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {

                    /* Drag handle */
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(Color.LightGray, RoundedCornerShape(2.dp))
                    )

                    /* ================= CARD 1 — STATUS ================= */
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column {
                                    Text(
                                        text = headingTitle,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = headingSubtitle,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("500m Away", fontSize = 12.sp, color = Color.Gray)
                                    otpLabelAndValue?.let { (label, otp) ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$label - $otp",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                serviceStatuses.forEachIndexed { index, status ->
                                    val active = index <= currentStatus

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (active) appPrimary else Color.LightGray
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector =
                                                    if (index == 0) Icons.Default.Check
                                                    else Icons.Default.DoneAll,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                        Text(
                                            text = status,
                                            fontSize = 12.sp,
                                            color = if (active) appPrimary else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    /* ================= CARD 2 — PROFILE + DETAILS ================= */
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            /* 🔹 Vendor Profile */
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(vendor?.profilePic) // 🔥 vendor image from API
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Vendor Profile",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.ic_person),
                                    error = painterResource(id = R.drawable.ic_person)
                                )


                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = vendor?.name.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "⭐ ${vendor?.avgRating ?: "--"} (${vendor?.reviewCount ?: 0} Reviews)",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                IconButton(onClick = { /* call */ }) { Text("📞") }
                                IconButton(onClick = { /* chat */ }) { Text("💬") }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            /* 🔹 Booking Details */
                            DetailRow("Amount", "₹$price")
                            DetailRow("Payment", "$mode")
                            DetailRow("Date", "$formattedDate")
                            DetailRow("Time", "$formattedTime")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Map View", color = Color.DarkGray)
            }
        }
        /* ================= DIM BACKGROUND ================= */
        if (showPaymentPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .zIndex(1f)
            )
        }
        /* ================= TOP PAYMENT POPUP ================= */
        AnimatedVisibility(
            visible = showPaymentPopup,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(400)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        ) {
            PaymentPendingPopup(
                vendorName = vendor?.name.orEmpty(),
                vendorImage = vendor?.profilePic,
                rating = vendor?.avgRating,
                reviews = vendor?.reviewCount,
                onProceedPay = {
                    // TODO: navigate to payment
                },
                onCancel = {
                    // TODO: cancel request API
                }
            )
        }
    }
}

@Composable
private fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatBookingDateTime(dateTime: String?): Pair<String, String> {
    if (dateTime.isNullOrEmpty()) {
        return "-" to "-"
    }

    val inputFormatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    )

    val outputDateFormatter = DateTimeFormatter.ofPattern(
        "dd MMM, yyyy",
        Locale.getDefault()
    )

    val outputTimeFormatter = DateTimeFormatter.ofPattern(
        "hh:mm a",
        Locale.getDefault()
    )

    val parsedDateTime = LocalDateTime.parse(dateTime, inputFormatter)

    val date = parsedDateTime.format(outputDateFormatter)
    val time = parsedDateTime.format(outputTimeFormatter)

    return date to time
}

@Composable
fun PaymentPendingPopup(
    vendorName: String,
    vendorImage: String?,
    rating: Double?,
    reviews: Int?,
    onProceedPay: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Your service is about to complete",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                AsyncImage(
                    model = vendorImage,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_person),
                    error = painterResource(R.drawable.ic_person)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(vendorName, fontWeight = FontWeight.Bold)
                    Text(
                        text = "⭐ ${rating ?: "--"} (${reviews ?: 0} Reviews)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { /* call */ }) { Text("📞") }
                IconButton(onClick = { /* chat */ }) { Text("💬") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Get ready to pay the remaining amount to the vendor.",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onProceedPay,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
            ) {
                Text("Proceed to pay")
            }

            Spacer(modifier = Modifier.height(8.dp))

//            OutlinedButton(
//                onClick = onCancel,
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Text("Cancel Request")
//            }
        }
    }
}



