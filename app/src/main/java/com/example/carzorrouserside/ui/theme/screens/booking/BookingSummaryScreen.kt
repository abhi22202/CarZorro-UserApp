package com.example.carzorrouserside.ui.theme.screens.booking

import android.content.res.Configuration
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.carzorrouserside.R // Ensure this R file import is correct
import com.example.carzorrouserside.ui.theme.CarZorroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryScreen(navController: NavController) {
    // State to control the visibility of the payment dialog
    var showPaymentDialog by remember { mutableStateOf(false) }

    // Show the dialog when the state is true
    if (showPaymentDialog) {
        PaymentMethodDialog(
            onDismissRequest = { showPaymentDialog = false },
            onNextClick = { selectedOption ->
                // TODO: Handle the logic for the selected payment option
                println("Selected option: $selectedOption")
                showPaymentDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Booking Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Pass a lambda to the BottomBar to open the dialog
            BottomBar(onProceedClick = { showPaymentDialog = true })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp)
        ) {
            ServiceInfoSection()
            HorizontalDivider()
            FullCarWashServiceCard()
            Spacer(modifier = Modifier.height(16.dp))
            DateAndDurationSection()
            HorizontalDivider()
            AboutVendorSection()
            HorizontalDivider()
            AboutCustomerSection()
            Spacer(modifier = Modifier.height(24.dp))
            SpecialDealsSection()
            Spacer(modifier = Modifier.height(16.dp))
            ExclusiveBenefitBanner()
            Spacer(modifier = Modifier.height(16.dp))
            PayWithSuperCoins()
            Spacer(modifier = Modifier.height(16.dp))
            PriceDetailsSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaymentMethodDialog(
    onDismissRequest: () -> Unit,
    onNextClick: (String) -> Unit
) {
    val paymentOptions = listOf("Pay on service", "Online Payment")
    var selectedOption by rememberSaveable { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                paymentOptions.forEach { option ->
                    PaymentOptionRow(
                        text = option,
                        selected = selectedOption == option,
                        onClick = { selectedOption = option }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onNextClick(selectedOption) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    enabled = selectedOption.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9D8CFF),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        "Next",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (selected) Color(0xFF9D8CFF) else Color(0xFFDBE0E2),
                RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(if (selected) Color(0xFF9D8CFF).copy(alpha = 0.05f) else Color.White)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color(0xFF1A1C1E)
        )
        Checkbox(
            checked = selected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF9D8CFF),
                uncheckedColor = Color(0xFF49454F)
            ),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun BottomBar(onProceedClick: () -> Unit) {
    val accentGreen = Color(0xFF4CAF50) // This color is an accent and can stay fixed

    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accentGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Savings", tint = accentGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You Will Save ₹2000 On This Order",
                    color = accentGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onProceedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Proceed to pay",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- The rest of your screen composables using MaterialTheme.colorScheme ---
@Composable
private fun ServiceInfoSection() {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow(label = "Service Type", value = "Doorstep service")
        InfoRow(label = "Car Type", value = "Audi")
    }
}

@Composable
private fun FullCarWashServiceCard() {
    val accentYellow = Color(0xFFFFC107) // Accent color
    val accentGreen = Color(0xFF4CAF50) // Accent color

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Full Car Wash Service", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = accentYellow, modifier = Modifier.size(16.dp))
                        Text("4.7", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.AccessTime, contentDescription = "Duration", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Text("35mins", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "A combination of dry wash, Vacuum Cleaning More..",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
                Box(modifier = Modifier.size(80.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.car_wash),
                        contentDescription = "Car Wash Service",
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "POPULAR",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .background(accentYellow, shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough, color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append("₹5000")
                    }
                })
                Spacer(modifier = Modifier.width(8.dp))
                Text("₹3456", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "50% OFF",
                    color = accentGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(accentGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DateAndDurationSection() {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow(label = "Date", value = "12 Nov 2024 At 11:30 AM")
        InfoRow(label = "Duration", value = "35min")
    }
}

@Composable
private fun AboutVendorSection() {
    val accentYellow = Color(0xFFFFC107) // Accent color

    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("About Vendor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.person),
                contentDescription = "Vendor",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("James Williams", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = accentYellow, modifier = Modifier.size(14.dp))
                    Text("4.8", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("(45 Reviews)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("6.5Km | New Delhi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AboutCustomerSection() {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("About Customer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = "Customer",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("James Williams", fontWeight = FontWeight.Bold)
                Text("Rohini Nagar, New Delhi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SpecialDealsSection() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.gift),
                    contentDescription = "Special Deals",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Special Deals Unlocked", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("Frequently Get Service Together", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            DealItem(imageRes = R.drawable.logo, title = "Seat Cleaning", price = "₹ 3456")
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
            DealItem(imageRes = R.drawable.car_wash, title = "Seat Cleaning", price = "₹ 3456")
        }
    }
}

@Composable
private fun DealItem(imageRes: Int, title: String, price: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(price, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = { /* TODO */ },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("+ Add", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun ExclusiveBenefitBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.EmojiEmotions, contentDescription = "Benefit", tint = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Exclusive") }
                append(" visitor card benefit ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("100 coins cashback") }
            },
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PayWithSuperCoins() {
    val accentYellow = Color(0xFFFFC107) // Accent color

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = false, onCheckedChange = {})
        Column {
            Text("Pay using super coins")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Available balance : ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = accentYellow, modifier = Modifier.size(14.dp))
                Text(" 600", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PriceDetailsSection() {
    Column(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Price Detail", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        PriceRow(label = "Price", amount = "₹120")
        PriceRow(label = "Discount (5% off)", amount = "- ₹15.12", amountColor = MaterialTheme.colorScheme.error)
        PriceRow(label = "Taxes", amount = "₹15.12")
        PriceRow(label = "Platform Fee", amount = "₹100")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Coupon", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("-₹10", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("₹255.12", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun PriceRow(label: String, amount: String, amountColor: Color = LocalContentColor.current) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(text = amount, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = amountColor)
    }
}

@Composable
private fun HorizontalDivider() {
    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
}

@Preview(showBackground = true, device = "id:pixel_6", name = "Light Mode")
@Preview(showBackground = true, device = "id:pixel_6", uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun BookingSummaryScreenPreview() {
    CarZorroTheme {
        BookingSummaryScreen(navController = rememberNavController())
    }
}

