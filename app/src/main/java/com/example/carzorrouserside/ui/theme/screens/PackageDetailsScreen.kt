package com.example.carzorrouserside.ui.theme.screens.packagedetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.carzorrouserside.data.model.homescreen.Feature
import com.example.carzorrouserside.data.model.homescreen.PackageDetailData
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.components.ShowAuthenticationModal
import com.example.carzorrouserside.ui.theme.screens.vendor.CustomerReview
import com.example.carzorrouserside.ui.theme.screens.vendor.CustomerReviewSection
import com.example.carzorrouserside.ui.theme.screens.vendor.InfoChip
import com.example.carzorrouserside.ui.theme.screens.vendor.RatingDistribution
import com.example.carzorrouserside.ui.theme.viewmodel.PackageDetailViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

data class AddOnService(
    val name: String,
    val price: Int,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageDetailScreen(
    navController: NavController,
    viewModel: PackageDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val packageData = uiState.packageDetail

    // --- Static Data for sections without API data yet ---
    val addOns = remember {
        listOf(
            AddOnService("Seat Cleaning", 3456, R.drawable.car_deep_clean_foreground),
            AddOnService("Floor Mat Lamination", 1200, R.drawable.underbody)
        )
    }
    val reviews = remember {
        listOf(
            CustomerReview(1, "Rohit Kapoor", "08/09/2024", 4.7f, "The ceramic coat gave my car a slight shine, but it wasn't as glossy as I hoped. The interior vacuuming was average, and the mint fragrance was nice but not enough to wow me."),
        )
    }
    val ratingDistribution = RatingDistribution(322, 40, 0, 0, 0)


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(packageData?.packageDetails?.name ?: "Package Details", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            val lowestPrice = packageData?.coatingPriceMapping
                ?.flatMap { it.priceMappings.orEmpty() }
                ?.mapNotNull { it.smallVehiclePrice ?: it.mediumVehiclePrice ?: it.largeVehiclePrice }
                ?.minOrNull()

            if (lowestPrice != null) {
                PackageDetailBottomBar(
                    price = lowestPrice.roundToInt().toString(),
                    navController = navController
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchPackageDetails() }) {
                            Text("Retry")
                        }
                    }
                }
                packageData?.packageDetails != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { ImagePagerSection(imageUrl = packageData.packageDetails.imageUrl) }
                        item { PackageHeaderSection(packageData = packageData) }
                        if (!packageData.features.isNullOrEmpty()) {
                            item { InclusionsSection(inclusions = packageData.features) }
                        }
                        // --- Static Sections Re-added ---
                        item { AddOnsSection(addOns = addOns, navController = navController) }
                        item {
                            CustomerReviewSection(
                                ratingDistribution = ratingDistribution,
                                reviews = reviews,
                                navController = navController
                            )
                        }
                        item { FaqSection() }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ImagePagerSection(imageUrl: String?) {
    val images = listOfNotNull(
        imageUrl,
        "https://picsum.photos/400/200", // Placeholder 1
        "https://picsum.photos/401/200"  // Placeholder 2
    )
    val pagerState = rememberPagerState(pageCount = { images.size })

    if (images.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = "Service Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.car_wash),
                    error = painterResource(id = R.drawable.car_wash)
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
                    Box(modifier = Modifier.padding(4.dp).clip(CircleShape).background(color).size(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PackageHeaderSection(packageData: PackageDetailData) {
    val details = packageData.packageDetails ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = details.name ?: "N/A",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!details.duration.isNullOrBlank()) {
                InfoChip(icon = Icons.Default.AccessTime, text = details.duration)
            }
            // Add static chips back for now
            InfoChip(icon = painterResource(id = R.drawable.ic_star), text = "6.5KM")
            InfoChip(icon = Icons.Default.LocationOn, text = "New Delhi")
        }

        Divider()

        Text(
            text = details.description ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun InclusionsSection(inclusions: List<Feature>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "What is included in this service?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(inclusions) { inclusion ->
                InclusionItem(inclusion = inclusion)
            }
        }
    }
}

@Composable
private fun InclusionItem(inclusion: Feature) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        AsyncImage(
            model = inclusion.imageUrl,
            contentDescription = inclusion.name,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.star_wash),
            error = painterResource(id = R.drawable.star_wash)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = inclusion.name ?: "",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AddOnsSection(
    addOns: List<AddOnService>,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Recommended Add-Ons",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        addOns.forEach { addOn ->
            AddOnItem(addOn = addOn, navController = navController)
        }
    }
}

@Composable
private fun AddOnItem(
    addOn: AddOnService,
    navController: NavController
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
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = addOn.imageRes),
            contentDescription = addOn.name,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(addOn.name, fontWeight = FontWeight.Bold)
            Text("₹ ${addOn.price}", color = Color.Gray, fontSize = 14.sp)
        }
        Button(
            onClick = {
                val isLoggedIn = runBlocking { 
                    userPreferencesManager.getJwtToken() != null 
                }
                if (!isLoggedIn) {
                    showAuthModal = true
                } else {
                    // TODO: Handle add addon logic
                }
            },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add")
        }
    }
}

@Composable
private fun FaqSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "View FAQs")
    }
}

@Composable
private fun PackageDetailBottomBar(
    price: String,
    navController: NavController
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
    
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("Starts at ₹$price", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = {
                val isLoggedIn = runBlocking { 
                    userPreferencesManager.getJwtToken() != null 
                }
                if (!isLoggedIn) {
                    showAuthModal = true
                } else {
                    // TODO: Handle Book Now - navigate to booking screen
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = appPrimary)
        ) {
            Text("Book Now", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}