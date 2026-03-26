// in package com.example.carzorrouserside.ui.theme.screens.booking

package com.example.carzorrouserside.ui.theme.screens.booking

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star // NEW: Changed to filled star for dummy data
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.booking.BookingItem
import com.example.carzorrouserside.data.model.booking.Order
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.viewmodel.booking.BookingViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// NEW: Dummy data class for hardcoded items
data class DummyOrder(
    val id: Int,
    val name: String,
    val status: String,
    val date: String,
    val time: String,
    val totalAmount: String,
    @DrawableRes val imageResId: Int,
    val rating: Int
)

// NEW: Function to get dummy data for the "Completed" tab
private fun getCompletedDummyData(): List<DummyOrder> {
    return List(5) {
        DummyOrder(
            id = it,
            name = "Full Car Wash Service",
            status = "Service Delivered",
            date = "11th Nov 2024",
            time = "11:09 PM",
            totalAmount = "3456",
            imageResId = R.drawable.pick_up, // Replace with your actual drawable
            rating = 4
        )
    }
}

// NEW: Function to get dummy data for the "Cancelled" tab
private fun getCancelledDummyData(): List<DummyOrder> {
    return List(5) {
        DummyOrder(
            id = it,
            name = "Full Car Wash Service",
            status = "Cancelled",
            date = "11th Nov 2024",
            time = "11:09 PM",
            totalAmount = "3456",
            imageResId = R.drawable.pick_up, // Replace with your actual drawable
            rating = 4
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    navController: NavController,
    viewModel: BookingViewModel = hiltViewModel(),
    onBookingClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val statusTabs = listOf("Upcoming", "Completed", "Cancelled", "Ongoing")
    val categoryTabs = listOf("Services", "Products")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Bookings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = appPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            // Status Tabs (Upcoming, Completed, Cancelled)
            TabRow(
                selectedTabIndex = uiState.selectedStatusTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = Color.Transparent,
                contentColor = appPrimary,
                divider = { }
            ) {
                statusTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedStatusTabIndex == index,
                        onClick = { viewModel.onStatusTabSelected(index) },
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (uiState.selectedStatusTabIndex == index) appPrimary else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (uiState.selectedStatusTabIndex == index) Color.Transparent else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(50.dp)
                            )
                    ) {
                        Text(
                            text = title,
                            color = if (uiState.selectedStatusTabIndex == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Tabs (Services, Products)
            // This is hidden for hardcoded tabs
            if (uiState.selectedStatusTabIndex == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    categoryTabs.forEachIndexed { index, title ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.onCategoryTabSelected(index) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (uiState.selectedCategoryTabIndex == index) appPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .height(3.dp)
                                    .width(80.dp)
                                    .background(
                                        if (uiState.selectedCategoryTabIndex == index) appPrimary else Color.Transparent,
                                        RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                    )
                            )
                        }
                    }
                }
            }


            // Booking list
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // Conditional logic to switch between API data for each tab
                when (uiState.selectedStatusTabIndex) {
                    // Index 0: "Upcoming" -> Use API data
                    0 -> {
                        when {
                            uiState.isLoading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            uiState.error != null -> {
                                Text(
                                    text = uiState.error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                            uiState.upcomingBookings.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (uiState.selectedCategoryTabIndex == 0) {
                                            "Service history is not available."
                                        } else {
                                            "No products found for this status."
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    items(uiState.upcomingBookings, key = { it.bookingId }) { booking ->
                                        BookingItemCard(
                                            booking = booking,
                                            onBookingClick = { bookingId -> onBookingClick(bookingId) },
                                            showRating = false,
                                            isUpcoming = true
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    // Index 1: "Completed" -> Use API data
                    1 -> {
                        when {
                            uiState.isLoading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            uiState.error != null -> {
                                Text(
                                    text = uiState.error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                            uiState.completedBookings.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No bookings found for this status.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    items(uiState.completedBookings, key = { it.bookingId }) { booking ->
                                        BookingItemCard(
                                            booking = booking,
                                            onBookingClick = { bookingId -> onBookingClick(bookingId) },
                                            showRating = true,
                                            isUpcoming = false
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    // Index 2: "Cancelled" -> Use API data
                    2 -> {
                        when {
                            uiState.isLoading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            uiState.error != null -> {
                                Text(
                                    text = uiState.error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                            uiState.cancelledBookings.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No bookings found for this status.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    items(uiState.cancelledBookings, key = { it.bookingId }) { booking ->
                                        BookingItemCard(
                                            booking = booking,
                                            onBookingClick = { bookingId -> onBookingClick(bookingId) },
                                            showRating = false,
                                            isUpcoming = false
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    // Index 3: "Ongoing" -> Use API data
                    3 -> {
                        when {
                            uiState.isLoading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            uiState.error != null -> {
                                Text(
                                    text = uiState.error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                            uiState.ongoingBookings.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No bookings found for this status.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    items(uiState.ongoingBookings, key = { it.bookingId }) { booking ->
                                        BookingItemCard(
                                            booking = booking,
                                            onBookingClick = { bookingId -> onBookingClick(bookingId) },
                                            showRating = false,
                                            isUpcoming = true
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// NEW: Composable for displaying hardcoded booking items
@Composable
fun DummyBookingHistoryItem(
    order: DummyOrder,
    onBuyAgainClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = order.imageResId),
                    contentDescription = order.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Order Placed on ${order.date}, ${order.time}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = order.status, // The status is directly from dummy data
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Always show rating for these hardcoded items
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start // Aligned to start as per screenshots
            ) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (index < order.rating) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "₹${order.totalAmount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.car_wash), // Replace with your clock icon
                            contentDescription = "Duration",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "35mins",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = { onBuyAgainClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = appPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Book Again",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}


// Composable for displaying booking items from API
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingItemCard(
    booking: BookingItem,
    onBookingClick: (Int) -> Unit,
    showRating: Boolean,
    isUpcoming: Boolean
) {
    val (bookingDate, bookingTime) = remember(booking.createdTime) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val dateTime = LocalDateTime.parse(booking.createdTime, inputFormatter)
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            Pair(dateTime.format(dateFormatter), dateTime.format(timeFormatter))
        } catch (e: Exception) {
            Pair(booking.createdTime, "")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Use AsyncImage for loading images from URL
                if (!booking.serviceFeatureImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = booking.serviceFeatureImage,
                        contentDescription = booking.serviceFeatureName,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.pick_up),
                        error = painterResource(id = R.drawable.pick_up)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.pick_up),
                        contentDescription = booking.serviceFeatureName,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.serviceFeatureName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Order Placed on $bookingDate, $bookingTime",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.car_wash),
                            contentDescription = "Duration",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = booking.serviceTime,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (showRating) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < 4) Color(0xFFFFC107) else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "₹${booking.price}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.car_wash),
                            contentDescription = "Duration",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = booking.serviceTime,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = { onBookingClick(booking.bookingId) },
                    colors = ButtonDefaults.buttonColors(containerColor = appPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text(
                        text = if (isUpcoming) "Track Order" else "Book Again",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// UNCHANGED Original Composables
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingHistoryItem(
    order: Order,
    onBuyAgainClick: (Int) -> Unit,
    showRating: Boolean,
    isUpcoming: Boolean
) {
    val (orderDate, orderTime) = remember(order.createdAt) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ENGLISH)
            val dateTime = LocalDateTime.parse(order.createdAt, inputFormatter)
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            Pair(dateTime.format(dateFormatter), dateTime.format(timeFormatter))
        } catch (e: Exception) {
            Pair("N/A", "N/A")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pick_up),
                    contentDescription = order.product.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Order Placed on $orderDate, $orderTime",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: ${order.orderStatus.replaceFirstChar { it.titlecase() }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showRating) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${order.totalAmount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onBuyAgainClick(order.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = appPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (isUpcoming) "Track Order" else "Buy Again",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun BookingHistoryScreenPreview() {

    val navController = rememberNavController()
    BookingHistoryScreen(navController = navController, onBookingClick = {})

}