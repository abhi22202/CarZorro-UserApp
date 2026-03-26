package com.example.carzorrouserside.ui.theme.screens.favourites

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.data.model.vendor.FavouriteVendorDto
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteVendorScreen(
    navController: NavController,
    viewModel: FavouriteVendorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favourite Vendors") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is FavouriteUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is FavouriteUiState.Error -> {
                    Text(
                        text = "Failed to load vendors:\n${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is FavouriteUiState.Success -> {
                    if (state.vendors.isEmpty()) {
                        Text(
                            "No favourite vendors yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.vendors, key = { it.id }) { vendor ->
                                FavouriteVendorCard(
                                    vendor = vendor,
                                    onBookNowClick = {
                                        // TODO: Handle book now click
                                    },
                                    onCardClick = {
                                        navController.navigate(Routes.vendorDetailScreen(vendor.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavouriteVendorCard(
    vendor: FavouriteVendorDto,
    onBookNowClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val imageUrls = if (!vendor.profilePic.isNullOrEmpty()) {
        listOf(vendor.profilePic)
    } else {
        listOf(
            "https://images.pexels.com/photos/3764984/pexels-photo-3764984.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
            "https://images.pexels.com/photos/8995927/pexels-photo-8995927.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"
        )
    }

    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                HorizontalPager(state = pagerState) { page ->
                    AsyncImage(
                        model = imageUrls[page],
                        contentDescription = vendor.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = "₹ 3456",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                if (imageUrls.size > 1) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(imageUrls.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) appPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            Box(modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoChip(icon = Icons.Default.AccessTime, text = "35mins")
                    InfoChip(icon = Icons.Default.LocationOn, text = vendor.distance)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(vendor.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    RatingChip(rating = vendor.rating.toFloat())
                }

                Text("Deep Clean • Interior • Exterior", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OfferTag(offerText = "50% OFF on this service")

                    Button(
                        onClick = onBookNowClick,
                        colors = ButtonDefaults.buttonColors(containerColor = appPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text("Book Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RatingChip(rating: Float) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appPrimary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("%.1f".format(rating), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = appPrimary)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.Star, contentDescription = "Rating", tint = appPrimary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun OfferTag(offerText: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.LocalOffer,
            contentDescription = "Offer",
            tint = appPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(offerText, color = appPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun FavouriteVendorScreenPreview() {
    MaterialTheme {
        FavouriteVendorScreen(navController = rememberNavController())
    }
}
