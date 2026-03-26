package com.example.carzorrouserside.ui.theme.screens.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.carzorrouserside.R // Ensure this points to your R file

// Custom colors from the image
val InvoiceTextColor = Color(0xFF6B6B6B)
val AmountDueGreen = Color(0xFF00C853) // Green for $0.00 amount due
val TableHeaderColor = Color(0xFFB0B0B0)
val PaymentDetailLabelColor = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingInvoiceScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "", // Title is empty as per image, content is below
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Invoice Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Section (Logo, Supported by, Vendor Details)
                    InvoiceHeaderSection()
                    Spacer(modifier = Modifier.height(24.dp))

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(24.dp))

                    // Bill To and Amount Due Section
                    BillToAmountDueSection()
                    Spacer(modifier = Modifier.height(24.dp))

                    // Item Description Table Headers
                    InvoiceTableHeader()
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)

                    // Invoice Items
                    InvoiceItemRow(description = "Gold package car wash", rate = "$0.00", qty = "1", amount = "$0.00")
                    InvoiceItemRow(description = "Discount", rate = "-$0.00", qty = "", amount = "$0.00")
                    InvoiceItemRow(description = "Taxes", rate = "$0.00", qty = "", amount = "$0.00")
                    InvoiceItemRow(description = "Coupon (ABC012)", rate = "-$0.00", qty = "", amount = "$0.00")

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Total Amount Section - outside the main card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp) // Fixed height as per image
                    .background(MaterialTheme.colorScheme.surface) // Match card background
                    .padding(horizontal = 36.dp), // More padding than card
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$0.00",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Payment Details Section - in a separate card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "PAYMENT DETAILS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PaymentDetailLabelColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    PaymentDetailRow(label = "Payment Status :", value = "Paid")
                    PaymentDetailRow(label = "Payment Method :", value = "Cash on service")
                    PaymentDetailRow(label = "Payment Type :", value = "Partial Payment")
                }
            }

            // Download Invoice Button (if needed) - can be added here
            // If the "Download Invoice" is actually a button from the previous screen, it might not belong here.
            // For now, I'm omitting it as the image shows it outside the main invoice content
            // and implies it might be a persistent action. If it's part of this screen,
            // we'd add a button here.
        }
    }
}

@Composable
fun InvoiceHeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.logo), // Your Carzorro logo
                contentDescription = "CarZorro Logo",
                modifier = Modifier.width(100.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Image(
                painter = painterResource(id = R.drawable.logo), // "Supported by CarZorro" image
                contentDescription = "Supported by CarZorro",
                modifier = Modifier.width(100.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Vendor Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = InvoiceTextColor)
            Text("Vendor Address", fontSize = 12.sp, color = InvoiceTextColor)
            Text("email@example.com", fontSize = 12.sp, color = InvoiceTextColor)
        }
    }
}

@Composable
fun BillToAmountDueSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("BILL TO", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = InvoiceTextColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Manohar Patil", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("Sector 123, New Delhi, India", fontSize = 12.sp, color = InvoiceTextColor)
            Text("manohar@gmail.com", fontSize = 12.sp, color = InvoiceTextColor)
            Text("Doorstep service", fontSize = 12.sp, color = InvoiceTextColor)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("AMOUNT DUE", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = InvoiceTextColor)
            Text("$0.00", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AmountDueGreen)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Issued on: Dec 10, 2024", fontSize = 12.sp, color = InvoiceTextColor)
            Text("11:50AM", fontSize = 12.sp, color = InvoiceTextColor)
        }
    }
}

@Composable
fun InvoiceTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "DESCRIPTION OF ITEM",
            modifier = Modifier.weight(0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TableHeaderColor
        )
        Text(
            text = "RATE",
            modifier = Modifier.weight(0.2f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TableHeaderColor
        )
        Text(
            text = "QTY",
            modifier = Modifier.weight(0.1f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TableHeaderColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = "AMOUNT",
            modifier = Modifier.weight(0.2f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TableHeaderColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun InvoiceItemRow(description: String, rate: String, qty: String, amount: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = description,
                modifier = Modifier.weight(0.5f),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = rate,
                modifier = Modifier.weight(0.2f),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = qty,
                modifier = Modifier.weight(0.1f),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = amount,
                modifier = Modifier.weight(0.2f),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 0.5.dp)
    }
}

@Composable
fun PaymentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = PaymentDetailLabelColor)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun BookingInvoiceScreenPreview() {
    MaterialTheme {
        BookingInvoiceScreen(navController = rememberNavController())
    }
}