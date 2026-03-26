package com.example.carzorrouserside.ui.theme.screens.homescreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.carzorrouserside.MainActivity
import com.example.carzorrouserside.R
import com.example.carzorrouserside.data.model.homescreen.OrderUiState
import com.example.carzorrouserside.data.model.homescreen.ProductDetail
import com.example.carzorrouserside.data.model.homescreen.ProductDetailImage
import com.example.carzorrouserside.data.model.homescreen.ProductHighlight
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.OrderViewModel
import com.example.carzorrouserside.ui.theme.viewmodel.homescreen.ProductDetailViewModel
import com.example.carzorrouserside.util.SnackbarManager
import com.example.carzorrouserside.util.SnackbarPosition
import com.example.carzorrouserside.util.rememberSnackbarState
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreenWithOrder(
    productId: Int,
    navController: NavController,
    productDetailViewModel: ProductDetailViewModel = hiltViewModel(),
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by productDetailViewModel.uiState.collectAsState()
    val orderUiState by orderViewModel.uiState.collectAsState()
    val paymentFlowState by orderViewModel.paymentFlowState.collectAsState()
    val snackbarState = rememberSnackbarState()
    val context = LocalContext.current

    // Get MainActivity instance for Razorpay integration
    val activity = context as? MainActivity

    // Load product detail when screen is first displayed
    LaunchedEffect(productId) {
        productDetailViewModel.loadProductDetail(productId)
    }

    // Handle product detail errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarState.showError(errorMessage)
        }
    }

    // Handle order creation errors
    LaunchedEffect(orderUiState.createOrderError) {
        orderUiState.createOrderError?.let { errorMessage ->
            snackbarState.showError(errorMessage)
        }
    }

    // Handle order verification errors
    LaunchedEffect(orderUiState.verifyOrderError) {
        orderUiState.verifyOrderError?.let { errorMessage ->
            snackbarState.showError(errorMessage)
        }
    }

    // Handle successful order verification
    LaunchedEffect(orderUiState.isOrderVerified) {
        if (orderUiState.isOrderVerified) {
            orderUiState.verificationMessage?.let { message ->
                snackbarState.showSuccess(message)
            }
            // Clear the verification state after showing message
            orderViewModel.clearVerificationMessage()
        }
    }

    // Handle Razorpay payment flow
    LaunchedEffect(paymentFlowState) {
        paymentFlowState?.let { flowState ->
            activity?.let { mainActivity ->
                Log.d("ProductDetail", "Starting Razorpay payment")
                Log.d("ProductDetail", "Order ID: ${flowState.orderId}")
                Log.d("ProductDetail", "Amount: ${flowState.amount}")

                // Set payment callbacks
                mainActivity.setPaymentCallbacks(
                    onSuccess = { payId ->
                        Log.d("ProductDetail", "Payment success with ID: $payId")
                        payId?.let {
                            orderViewModel.verifyPayment(it)
                        }
                    },
                    onError = { code, response ->
                        Log.e("ProductDetail", "Payment failed with code: $code, response: $response")
                        orderViewModel.handlePaymentFailure("Code: $code, Message: $response")
                    }
                )

                // Start Razorpay payment
                mainActivity.startRazorpayPayment(
                    orderId = flowState.orderId,
                    razorpayKey = flowState.razorpayKey,
                    amount = flowState.amount.toDouble()
                )
            }
        }
    }

    // Cleanup payment callbacks when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            activity?.clearPaymentCallbacks()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.productDetail?.name ?: "Product Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                            onClick = { /* TODO: Add to favorites */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Add to Favorites",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = { /* TODO: Share product */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = appPrimary
                    )
                )
            },
            bottomBar = {
                // Bottom action bar with order integration
                uiState.productDetail?.let { product ->
                    ProductDetailBottomBarWithOrder(
                        product = product,
                        orderUiState = orderUiState,
                        onAddToCart = {
                            snackbarState.showInfo("Add to cart feature coming soon!")
                        },
                        onBuyNow = { quantity ->
                            // Create order and initiate payment flow
                            orderViewModel.initiateOrderCreation(
                                productId = product.id,
                                quantity = quantity
                            )
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                when {
                    uiState.isLoading -> {
                        ProductDetailLoadingScreen()
                    }

                    uiState.error != null && uiState.productDetail == null -> {
                        ProductDetailErrorScreen(
                            error = uiState.error!!,
                            onRetry = { productDetailViewModel.retryLoadProductDetail(productId) }
                        )
                    }

                    uiState.productDetail != null -> {
                        ProductDetailContent(
                            product = uiState.productDetail!!,
                            snackbarState = snackbarState
                        )
                    }
                }
            }
        }

        // Loading overlay for order operations
        if (orderUiState.isCreatingOrder || orderUiState.isVerifyingOrder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = appPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when {
                                orderUiState.isCreatingOrder -> "Creating order..."
                                orderUiState.isVerifyingOrder -> "Verifying payment..."
                                else -> "Processing..."
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
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
                productDetailViewModel.retryLoadProductDetail(productId)
            }
        )
    }
}

@Composable
fun ProductDetailBottomBarWithOrder(
    product: ProductDetail,
    orderUiState: OrderUiState,
    onAddToCart: () -> Unit,
    onBuyNow: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column {
            // Quantity selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quantity:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Decrease quantity button
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        enabled = quantity > 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease quantity",
                            tint = if (quantity > 1) appPrimary else Color.Gray
                        )
                    }

                    // Quantity display
                    Text(
                        text = quantity.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .background(
                                Color.Gray.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Increase quantity button
                    IconButton(
                        onClick = {
                            if (quantity < product.stockQuantity) quantity++
                        },
                        enabled = quantity < product.stockQuantity
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            tint = if (quantity < product.stockQuantity) appPrimary else Color.Gray
                        )
                    }
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add to Cart Button
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, appPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = product.stockQuantity > 0 && !orderUiState.isCreatingOrder
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Add to Cart",
                        tint = appPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Cart",
                        color = appPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Buy Now Button
                Button(
                    onClick = { onBuyNow(quantity) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appPrimary,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = product.stockQuantity > 0 && !orderUiState.isCreatingOrder
                ) {
                    if (orderUiState.isCreatingOrder) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (product.stockQuantity > 0) "Buy Now" else "Out of Stock",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Total price display
            if (product.stockQuantity > 0) {
                val totalPrice = try {
                    product.discountPrice.toDouble() * quantity
                } catch (e: Exception) {
                    0.0
                }

                Text(
                    text = "Total: ₹${String.format("%.0f", totalPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
@Composable
fun ProductDetailContent(
    product: ProductDetail,
    snackbarState: Any
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom bar
    ) {
        item {
            // Product Images Gallery
            ProductImageGallery(images = product.images)
        }

        item {
            // Product Info Section
            ProductInfoSection(product = product)
        }

        item {
            // Product Highlights/Specifications
            ProductHighlightsSection(highlights = product.highlights)
        }

        item {
            // Description Section
            ProductDescriptionSection(description = product.description)
        }

        item {
            // Reviews Section (Mock data for now)
            ProductReviewsSection()
        }
    }
}

@Composable
fun ProductImageGallery(images: List<ProductDetailImage>) {
    var selectedImageIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Main Image Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (images.isNotEmpty()) {
                    AsyncImage(
                        model = images[selectedImageIndex].productImages,
                        contentDescription = "Product Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = R.drawable.outline_photo_camera_24),
                        error = painterResource(id = R.drawable.outline_error_24)
                    )
                } else {
                    // Placeholder when no images
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Gray.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No Image",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            // Image Thumbnails (if multiple images)
            if (images.size > 1) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images.size) { index ->
                        val isSelected = index == selectedImageIndex
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) appPrimary else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedImageIndex = index }
                        ) {
                            AsyncImage(
                                model = images[index].productImages,
                                contentDescription = "Thumbnail ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.outline_photo_camera_24),
                                error = painterResource(id = R.drawable.outline_error_24)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductInfoSection(product: ProductDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Product Name
            Text(
                text = product.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rating Row (Mock data)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rating = (4.0f + Random.nextFloat() * 1.0f) // Mock rating 4.0-5.0
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (index < rating.toInt()) Color(0xFFFF9800) else Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("%.1f", rating),
                    fontSize = 14.sp,
                    color = appPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${Random.nextInt(100, 1000)} reviews)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = product.discountPrice,
                    fontSize = 28.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "₹${product.price}",
                    fontSize = 18.sp,
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
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFCC0C39), // Amazon red
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${discount.first}% off",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You save ₹${discount.second}",
                        fontSize = 14.sp,
                        color = Color(0xFF007600), // Amazon green
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stock Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (product.stockQuantity > 0) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (product.stockQuantity > 0) Color(0xFF007600) else Color(0xFFCC0C39),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (product.stockQuantity > 10) {
                        "In stock"
                    } else if (product.stockQuantity > 0) {
                        "Only ${product.stockQuantity} left in stock"
                    } else {
                        "Currently unavailable"
                    },
                    fontSize = 14.sp,
                    color = if (product.stockQuantity > 0) Color(0xFF007600) else Color(0xFFCC0C39),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Info
            Text(
                text = "FREE delivery by tomorrow",
                fontSize = 14.sp,
                color = Color(0xFF007600),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProductHighlightsSection(highlights: List<ProductHighlight>) {
    if (highlights.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Product Specifications",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                highlights.forEach { highlight ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = highlight.key.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = highlight.value,
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                    if (highlight != highlights.last()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = Color.Gray.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDescriptionSection(description: String) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxLines = if (isExpanded) Int.MAX_VALUE else 4

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Product Description",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )

            if (description.length > 200) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    colors = ButtonDefaults.textButtonColors(contentColor = appPrimary)
                ) {
                    Text(
                        text = if (isExpanded) "Show Less" else "Show More",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ProductReviewsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer Reviews",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                TextButton(
                    onClick = { /* TODO: Navigate to all reviews */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = appPrimary)
                ) {
                    Text("View All")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mock review summary
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "4.5",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < 4) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (index < 4) Color(0xFFFF9800) else Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${Random.nextInt(100, 500)} reviews)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mock recent review
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(appPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Anonymous User",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < 5) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (index < 5) Color(0xFFFF9800) else Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Great product! Works exactly as described. Fast delivery and excellent packaging.",
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ProductDetailBottomBar(
    product: ProductDetail,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add to Cart Button
            OutlinedButton(
                onClick = onAddToCart,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, appPrimary),
                shape = RoundedCornerShape(12.dp),
                enabled = product.stockQuantity > 0
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Add to Cart",
                    tint = appPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to Cart",
                    color = appPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Buy Now Button
            Button(
                onClick = onBuyNow,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appPrimary,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = product.stockQuantity > 0
            ) {
                Text(
                    text = if (product.stockQuantity > 0) "Buy Now" else "Out of Stock",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ProductDetailLoadingScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image placeholder
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = appPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // Content placeholders
        items(3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (index == 2) 0.7f else 0.9f)
                                .height(16.dp)
                                .background(
                                    Color.Gray.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        if (index < 2) Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
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
            text = "Failed to load product details",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
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