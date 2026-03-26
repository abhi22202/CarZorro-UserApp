package com.example.carzorrouserside.ui.screen.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carzorrouserside.data.model.booking.OrderDetail
import com.example.carzorrouserside.ui.viewmodel.booking.OrderDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    viewModel: OrderDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                // You'll need an icon for the back arrow
                // navigationIcon = {
                //     IconButton(onClick = onNavigateBack) {
                //         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                //     }
                // }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                uiState.orderDetail != null -> {
                    OrderDetailContent(order = uiState.orderDetail!!)
                }
            }
        }
    }
}

@Composable
fun OrderDetailContent(order: OrderDetail) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order Status Card
        Card(elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Order #${order.id}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                DetailRow("Order Status:", order.orderStatus.replaceFirstChar { it.uppercase() })
                DetailRow("Payment Status:", order.paymentStatus.replaceFirstChar { it.uppercase() })
                DetailRow("Ordered On:", order.orderedAt) // You might want to format this date
            }
        }

        // Product Details Card
        Card(elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Product Details", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                DetailRow("Product:", order.product.name)
                Text(order.product.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                DetailRow("Quantity:", order.quantity.toString())
            }
        }

        // Payment Summary Card
        Card(elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Payment Summary", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                DetailRow("Item Price:", "₹${order.price}")
                DetailRow("Total Amount:", "₹${order.totalAmount}", isTotal = true)
                DetailRow("Payment Method:", if (order.paymentMethod == "3") "Online" else "COD")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Text(
            text = value,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTotal) 18.sp else 16.sp
        )
    }
}
