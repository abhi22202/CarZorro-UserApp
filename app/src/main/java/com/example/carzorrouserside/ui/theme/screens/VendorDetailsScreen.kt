package com.example.carzorrouserside.ui.theme.screens.vendor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.vendor.AllReview
import com.example.carzorrouserside.data.model.vendor.UserReviewDetail
import com.example.carzorrouserside.data.model.vendor.VendorData
import com.example.carzorrouserside.data.model.vendor.VendorPackage
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.components.ShowAuthenticationModal
import com.example.carzorrouserside.ui.viewmodel.VendorDetailsViewModel
import com.example.carzorrouserside.ui.theme.navigation.Routes
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDetailsScreen(
    navController: NavController,
    viewModel: VendorDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Shows a snackbar message when the favorite status is updated
    LaunchedEffect(uiState.favoriteUpdateMessage) {
        uiState.favoriteUpdateMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearFavoriteMessage() // Reset the message after showing it
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            uiState.vendorData?.vendor?.name ?: "Loading...",
                            fontWeight = FontWeight.Bold
                        )
                        Text("Vendor", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // --- FULLY UPDATED FAVORITE BUTTON LOGIC ---
                    val vendorData = uiState.vendorData
                    if (vendorData != null) {
                        if (uiState.isFavoriting) {
                            // Show a progress indicator while the request is in flight
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 16.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            val isFavorite = vendorData.isFavourite ?: false
                            val icon = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder
                            val tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant

                            IconButton(
                                onClick = { viewModel.toggleFavoriteStatus() },
                                enabled = !uiState.isFavoriting
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Favorite",
                                    tint = tint
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (uiState.vendorData != null) {
                VendorDetailsBottomBar()
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(
                        text = "Failed to load details: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                uiState.vendorData != null -> {
                    VendorDetailsContent(
                        data = uiState.vendorData!!,
                        packages = uiState.packages,
                        navController = navController
                    )
                }
            }
        }
    }
}
@Composable
fun VendorDetailsContent(
    data: VendorData,
    packages: List<VendorPackage>,
    navController: NavController
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { ImagePagerSection() }
        item { VendorHeaderInfo(data) }
        item { AboutVendorSection() }
        item {
            Text(
                text = "Our Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        // --- UPDATED: Displaying packages from the API ---
        val hasReviews = data.allReviews != null || !data.vendor.reviews.isNullOrEmpty()
        if (packages.isEmpty() && hasReviews) { // check reviews to ensure we are not in a loading state
            item {
                Text(
                    text = "No services available.",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            items(packages) { packageItem ->
                PackageItemCard(
                    service = packageItem,
                    navController = navController,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }


        item {
            // allReviews is now a List<AllReview>
            val reviewsList = data.allReviews ?: emptyList()
            ApiCustomerReviewSection(
                reviews = reviewsList,
                navController = navController
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// --- NEW COMPOSABLE to display a VendorPackage item from the API ---
@Composable
fun PackageItemCard(
    service: VendorPackage,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPreferencesManager = remember {
        UserPreferencesManager(context.applicationContext)
    }
    var showAuthModal by remember { mutableStateOf(false) }
    
    // Show authentication modal
    ShowAuthenticationModal(
        navController = navController,
        userPreferencesManager = userPreferencesManager,
        showModal = showAuthModal,
        onDismiss = { showAuthModal = false }
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(service.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Text(" ${"%.1f".format(service.rating.toFloat())}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        // Duration is not available in this API, so it's not shown
                    }
                    Text(service.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Price
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("₹${service.discountPrice.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "₹${service.price.toInt()}",
                            color = Color.Gray,
                            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough),
                            fontSize = 14.sp
                        )
                        if (service.price > 0 && service.price > service.discountPrice) {
                            val discount = ((service.price - service.discountPrice) / service.price * 100).toInt()
                            Text(
                                text = "$discount% OFF",
                                color = Color(0xFF4CAF50),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Right Side: Image and Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // This API doesn't provide an image per package, so we use a placeholder
                    Image(
                        painter = painterResource(id = R.drawable.car_wash),
                        contentDescription = service.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val isLoggedIn = runBlocking { 
                                userPreferencesManager.getJwtToken() != null 
                            }
                            if (!isLoggedIn) {
                                showAuthModal = true
                            } else {
                                // Handle select - navigate to booking or add to cart
                                // TODO: Implement package selection logic
                            }
                        },
                        modifier = Modifier
                            .height(36.dp)
                            .width(80.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = appPrimary.copy(alpha = 0.1f)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Select", color = appPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// --- ALL COMPOSABLES BELOW ARE UNCHANGED TO AVOID BREAKING PACKAGE_DETAIL_SCREEN ---

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ImagePagerSection() {
    val images = listOf(R.drawable.logo, R.drawable.spray_wash, R.drawable.underbody)
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = "Vendor Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Row(
            Modifier
                .height(20.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) appPrimary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun VendorHeaderInfo(data: VendorData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = data.business.businessName ?: data.business.businessNameAlt ?: "Business",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFC107))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(data.rating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text("${data.reviewCount} reviews", fontSize = 12.sp, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoChip(icon = Icons.Default.AccessTime, text = "35mins")
            InfoChip(icon = painterResource(id = R.drawable.ic_star), text = "6.5KM")
            InfoChip(icon = Icons.Default.LocationOn, text = data.address.city)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        InfoRow(icon = Icons.Default.LocationOn, text = data.address.fullAddress)
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(icon = Icons.Default.AccessTime, text = "Availability: 10:00 AM - 07:00 PM (Everyday)")
    }
}

@Composable
fun AboutVendorSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "A highly skilled professional with extensive experience in car wash services. Known for attention to detail and commitment to delivering top-notch results, they ensure every vehicle gets the best treatment possible.",
            fontSize = 14.sp,
            color = Color.Gray,
        )
    }
}

@Composable
fun CustomerReviewSection(
    ratingDistribution: RatingDistribution,
    reviews: List<CustomerReview>,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        ReviewSectionHeader(navController = navController)
        Spacer(modifier = Modifier.height(16.dp))
        RatingBreakdown(distribution = ratingDistribution)
        reviews.forEach { review ->
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            StaticReviewItem(review = review)
        }
    }
}

@Composable
private fun ApiCustomerReviewSection(
    reviews: List<AllReview>,
    navController: NavController
) {
    if (reviews.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        ReviewSectionHeader(navController = navController)
        reviews.forEach { review ->
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            ApiReviewItem(review = review)
        }
    }
}

@Composable
private fun ReviewSectionHeader(navController: NavController) {
    val context = LocalContext.current
    val userPreferencesManager = remember {
        UserPreferencesManager(context.applicationContext)
    }
    var showAuthModal by remember { mutableStateOf(false) }
    
    // Show authentication modal
    ShowAuthenticationModal(
        navController = navController,
        userPreferencesManager = userPreferencesManager,
        showModal = showAuthModal,
        onDismiss = { showAuthModal = false }
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Customer Review",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedButton(
            onClick = {
                val isLoggedIn = runBlocking { 
                    userPreferencesManager.getJwtToken() != null 
                }
                if (!isLoggedIn) {
                    showAuthModal = true
                } else {
                    // TODO: Navigate to rating screen
                }
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp),
        ) {
            Text("Rate the Vendor")
        }
    }
}

@Composable
fun RatingBreakdown(distribution: RatingDistribution) {
    val totalReviews = (distribution.fiveStars + distribution.fourStars + distribution.threeStars + distribution.twoStars + distribution.oneStar).toFloat().coerceAtLeast(1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RatingBarRow(star = 5, count = distribution.fiveStars, progress = distribution.fiveStars / totalReviews)
        RatingBarRow(star = 4, count = distribution.fourStars, progress = distribution.fourStars / totalReviews)
        RatingBarRow(star = 3, count = distribution.threeStars, progress = distribution.threeStars / totalReviews)
        RatingBarRow(star = 2, count = distribution.twoStars, progress = distribution.twoStars / totalReviews)
        RatingBarRow(star = 1, count = distribution.oneStar, progress = distribution.oneStar / totalReviews)
    }
}

@Composable
fun RatingBarRow(star: Int, count: Int, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("$star", fontSize = 12.sp)
        Icon(Icons.Filled.Star, contentDescription = "star", tint = Color.Gray, modifier = Modifier.size(16.dp).padding(start = 2.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).padding(horizontal = 8.dp),
            color = Color(0xFFFFC107),
            trackColor = Color.LightGray.copy(alpha = 0.5f)
        )
        Text(text = "$count", fontSize = 12.sp, modifier = Modifier.width(30.dp), color = Color.Gray)
    }
}

@Composable
private fun ApiReviewItem(review: AllReview) {
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else dateString.substringBefore("T")
        } catch (e: Exception) {
            dateString.substringBefore("T")
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = review.user.profilePic, contentDescription = review.user.fullName, placeholder = painterResource(id = R.drawable.baseline_person_24), error = painterResource(id = R.drawable.baseline_person_24), contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(review.user.fullName, fontWeight = FontWeight.Bold)
                    Text(formatDate(review.createdAt), fontSize = 12.sp, color = Color.Gray)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF8E1)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("${review.rating}", fontWeight = FontWeight.Bold, color = Color(0xFFF9A825))
                Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFF9A825), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = review.review, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
    }
}

@Composable
private fun StaticReviewItem(review: CustomerReview) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(review.name, fontWeight = FontWeight.Bold)
                Text(review.date, fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF8E1)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("${review.rating}", fontWeight = FontWeight.Bold, color = Color(0xFFF9A825))
                Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFF9A825), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = review.reviewText, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
    }
}

@Composable
fun VendorDetailsBottomBar() {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.surface, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = appPrimary)) {
            Text("Book Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(onClick = { /*TODO*/ }, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(appPrimary.copy(alpha = 0.1f))) {
            Icon(Icons.Default.Message, contentDescription = "Chat", tint = appPrimary)
        }
    }
}

@Composable
fun InfoChip(icon: Any, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.LightGray.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        when (icon) {
            is ImageVector -> Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            is Painter -> Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(showBackground = true, name = "Vendor Details Loaded")
@Composable
fun PreviewVendorDetailsScreen() {
    MaterialTheme {
        VendorDetailsScreen(navController = rememberNavController())
    }
}

data class CustomerReview(
    val id: Int,
    val name: String,
    val date: String,
    val rating: Float,
    val reviewText: String
)
data class RatingDistribution(
    val fiveStars: Int,
    val fourStars: Int,
    val threeStars: Int,
    val twoStars: Int,
    val oneStar: Int
)