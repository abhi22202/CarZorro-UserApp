package com.example.carzorrouserside.ui.theme.screens.vendordetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.R // Ensure this points to your project's R file
import com.example.carzorrouserside.ui.theme.appPrimary // Reusing appPrimary from your theme

@Composable
fun VendorDetailScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BookingBottomBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()) // Prevents content from hiding behind the bottom bar
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            VendorImageHeader(navController)
            VendorInfoSection()
            OurServicesSection()
            CustomerReviewsSection()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun VendorImageHeader(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        AsyncImage(
            model = R.drawable.logo, // Replace with your actual image resource or URL
            contentDescription = "Vendor Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
                        startY = 0f,
                        endY = 200f
                    )
                )
        )
        // Top navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Prateek Sharma",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* Handle favorite */ }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Color.White)
            }
        }
    }
}

@Composable
fun VendorInfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title and Rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sparkle Shine Car Wash",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("4.7", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = appPrimary)
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = appPrimary, modifier = Modifier.size(18.dp))
                }
                Text("13 reviews", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Tags
        Text("✨ 35mins | 📍 6.5km | 🗺️ New Delhi", color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Location and Availability
        InfoRowWithIcon(icon = Icons.Default.LocationOn, text = "Sector 29, Gurugram, Hariyana")
        InfoRowWithIcon(icon = Icons.Default.Schedule, text = "10:00 AM - 07:00 PM (Everyday)")

        // About section
        Column {
            Text("About", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                buildAnnotatedString {
                    append("Prateek Sharma is a highly skilled professional with extensive experience in car wash services. Known for his attention to detail and commitment to delivering top-notch results. ")
                    withStyle(style = SpanStyle(color = appPrimary, fontWeight = FontWeight.Bold)) {
                        append("Read more..")
                    }
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun InfoRowWithIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = appPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun OurServicesSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "Our Services",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Replicating 3 service cards from the image
            VendorServiceCard()
            VendorServiceCard()
            VendorServiceCard()
        }
    }
}

@Composable
fun VendorServiceCard() {
    // This is an adaptation of the ProductCard from your HomeScreen code
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "⭐ POPULAR",
                    color = Color(0xFFE6A400),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Full Car Was Service",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color.Yellow, modifier = Modifier.size(14.dp))
                    Text(" 4.7", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("  •  🕒 35mins", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "A combination of dry wash, Vacuum Cleaning, AC Cleaning More..",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹3456", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = appPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "₹5000",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "50% OFF",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = R.drawable.logo, // Replace with your image
                    contentDescription = "Service Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* Handle Select */ },
                    modifier = Modifier
                        .height(36.dp)
                        .width(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appPrimary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Select", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun CustomerReviewsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Customer Review", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            OutlinedButton(
                onClick = { /* Rate vendor */ },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, appPrimary)
            ) {
                Text("Rate the Vendor", color = appPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Rating Summary Progress Bars
        RatingSummary()
        Spacer(modifier = Modifier.height(20.dp))

        // Individual Reviews
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ReviewItem()
            ReviewItem()
        }
    }
}

@Composable
fun RatingSummary() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RatingProgressRow(star = "5", progress = 0.9f, count = "322")
        RatingProgressRow(star = "4", progress = 0.2f, count = "40")
        RatingProgressRow(star = "3", progress = 0.0f, count = "0")
        RatingProgressRow(star = "2", progress = 0.0f, count = "0")
        RatingProgressRow(star = "1", progress = 0.0f, count = "0")
    }
}

@Composable
fun RatingProgressRow(star: String, progress: Float, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(star, fontSize = 12.sp)
        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
            color = appPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(count, fontSize = 12.sp, modifier = Modifier.width(30.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
fun ReviewItem() {
    // This is an adaptation of the TestimonialCard from your HomeScreen code
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = R.drawable.logo, // Your reviewer avatar
                contentDescription = "Reviewer Avatar",
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Rohit Kapoor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("08/09/2024", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFFBEA))
                    .border(1.dp, Color(0xFFFFD600), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("4.7", fontWeight = FontWeight.Bold, color = Color(0xFFFFD600))
                Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "The ceramic coat gave my car a slight shine, but it wasn't as durable as I hoped. The interior vacuuming was decent and the mint fragrance was nice but not enough to wow me.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun BookingBottomBar() {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* Handle Book Now */ },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
            ) {
                Text("Book Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { /* Handle Chat */ },
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, appPrimary),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = appPrimary)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun VendorDetailScreenPreview() {
    MaterialTheme {
        VendorDetailScreen(navController = rememberNavController())
    }
}