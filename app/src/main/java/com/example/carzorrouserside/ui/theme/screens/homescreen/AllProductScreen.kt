// Amazon-Style All Products Screen
package com.example.carzorrouserside.ui.theme.screens.homescreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.homescreen.Product
import com.example.carzorrouserside.data.token.UserPreferencesManager
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.components.ShowAuthenticationModal
import com.example.carzorrouserside.ui.theme.navigation.Routes
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.ProductViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.runBlocking
import com.example.carzorrouserside.util.SnackbarManager
import com.example.carzorrouserside.util.SnackbarPosition
import com.example.carzorrouserside.util.rememberSnackbarState
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllProductsScreen(
    navController: NavController,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val uiState by productViewModel.uiState.collectAsState()
    val snackbarState = rememberSnackbarState()
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        productViewModel.refreshProducts()
    }

    // Handle error state with snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarState.showError(errorMessage)
        }
    }

    // Infinite scroll - Load next page when reaching the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= uiState.products.size - 2 &&
                    !uiState.isLoading &&
                    uiState.hasNextPage) {
                    productViewModel.loadNextPage()
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                // Amazon-style header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = appPrimary,
                    shadowElevation = 4.dp
                ) {
                    Column {
                        // Top app bar
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Products",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { productViewModel.refreshProducts() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )

                        // Amazon-style search bar integrated in header
                        AmazonStyleSearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { query -> searchQuery = query },
                            onSearch = {
                                keyboardController?.hide()
                                productViewModel.searchProducts(searchQuery)
                            },
                            onClearSearch = {
                                searchQuery = ""
                                productViewModel.clearSearch()
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF3F3F3)) // Amazon-style light gray background
            ) {
                when {
                    uiState.isLoading && uiState.products.isEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(6) {
                                AmazonStyleProductCardPlaceholder()
                            }
                        }
                    }

                    uiState.error != null && uiState.products.isEmpty() -> {
                        // Amazon-style error state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = Color.Gray,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Oops! Something went wrong",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "We're having trouble loading products. Please check your connection and try again.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { productViewModel.retryLoadProducts() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = appPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Try Again",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    uiState.products.isNotEmpty() -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp) // Amazon-style minimal spacing
                        ) {
                            // Results count header (Amazon-style)
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.White
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Results",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "${uiState.products.size}+ results ${if (searchQuery.isNotBlank()) "for \"$searchQuery\"" else ""}",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(uiState.products) { product ->
                                AmazonStyleProductCard(
                                    product = product,
                                    navController= navController,
                                    onClick = {
                                        snackbarState.showInfo("Selected ${product.name}")
                                    }
                                )
                            }

                            // Loading indicator for pagination
                            if (uiState.isLoading && uiState.products.isNotEmpty()) {
                                item {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.White
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = appPrimary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // End of list indicator
                            if (!uiState.hasNextPage && uiState.products.isNotEmpty()) {
                                item {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.White
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Divider(
                                                    modifier = Modifier
                                                        .width(100.dp)
                                                        .padding(bottom = 16.dp),
                                                    color = Color.Gray.copy(alpha = 0.3f)
                                                )
                                                Text(
                                                    text = "You've seen all products",
                                                    color = Color.Gray,
                                                    fontSize = 14.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "No Products",
                                tint = Color.Gray,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No results found" else "No products available",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "Try different keywords or remove search filters"
                                } else {
                                    "Check back later for new products!"
                                },
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedButton(
                                    onClick = {
                                        searchQuery = ""
                                        productViewModel.clearSearch()
                                    },
                                    border = BorderStroke(1.dp, appPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "Clear Search",
                                        color = appPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Snackbar Manager
        SnackbarManager(
            showError = snackbarState.showError,
            showSuccess = snackbarState.showSuccess,
            showInfo = snackbarState.showInfo,
            showWarning = snackbarState.showWarning,
            errorMessage = snackbarState.errorMessage,
            successMessage = snackbarState.successMessage,
            infoMessage = snackbarState.infoMessage,
            warningMessage = snackbarState.warningMessage,
            position = SnackbarPosition.TOP,
            onDismissError = { snackbarState.hideError() },
            onDismissSuccess = { snackbarState.hideSuccess() },
            onDismissInfo = { snackbarState.hideInfo() },
            onDismissWarning = { snackbarState.hideWarning() },
            onRetryError = {
                productViewModel.retryLoadProducts()
            }
        )
    }
}

@Composable
fun AmazonStyleSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search products",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.Gray
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = appPrimary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )
    }
}
@Composable
fun AmazonStyleProductCard(
    product: Product,
    navController: NavController, // Add this parameter
    onClick: () -> Unit = {}
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
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val isLoggedIn = runBlocking { 
                    userPreferencesManager.getJwtToken() != null 
                }
                if (!isLoggedIn) {
                    showAuthModal = true
                } else {
                    // Navigate to product detail screen with order integration
                    navController.navigate(Routes.productDetailScreen(product.id))
                }
            },
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Product Image with Amazon-style border
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = product.image?.productImageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(id = R.drawable.outline_photo_camera_24),
                    error = painterResource(id = R.drawable.outline_error_24)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Product Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Product Name (Amazon-style)
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mock rating (Amazon-style)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val rating = (4.0f + Random.nextFloat() * 1.0f) // Mock rating 4.0-5.0
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (index < rating.toInt()) Color(0xFFFF9800) else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format("%.1f", rating),
                        fontSize = 12.sp,
                        color = appPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${Random.nextInt(50, 500)})",
                        fontSize = 12.sp,
                        color = appPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price section (Amazon-style)
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = product.discountPrice,
                            fontSize = 20.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "₹${product.price}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            style = LocalTextStyle.current.copy(
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                    }

                    // Discount percentage and savings
                    val discount = try {
                        val original = product.price.toDouble()
                        val discounted = product.discountPrice.toDouble()
                        val savings = original - discounted
                        val percentage = ((original - discounted) / original * 100).toInt()
                        Pair(percentage, savings.toInt())
                    } catch (e: Exception) {
                        Pair(0, 0)
                    }

                    if (discount.first > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Surface(
                                color = Color(0xFFCC0C39), // Amazon red
                                shape = RoundedCornerShape(2.dp)
                            ) {
                                Text(
                                    text = "${discount.first}% off",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save ₹${discount.second}",
                                fontSize = 12.sp,
                                color = Color(0xFF007600), // Amazon green
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Delivery info (Amazon-style)
                Text(
                    text = "FREE delivery Tomorrow",
                    fontSize = 12.sp,
                    color = Color(0xFF007600),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stock status (Amazon-style)
                Text(
                    text = if (product.stockQuantity > 10) {
                        "In stock"
                    } else if (product.stockQuantity > 0) {
                        "Only ${product.stockQuantity} left in stock"
                    } else {
                        "Currently unavailable"
                    },
                    fontSize = 12.sp,
                    color = if (product.stockQuantity > 0) Color(0xFF007600) else Color(0xFFCC0C39),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AmazonStyleProductCardPlaceholder() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    color = appPrimary,
                    strokeWidth = 2.dp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content Placeholder
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rating placeholder
                Row {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        )
                        if (it < 4) Spacer(modifier = Modifier.width(2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price placeholder
                Row {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(20.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Additional info placeholders
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(12.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(12.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}