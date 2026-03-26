package com.example.carzorrouserside.ui.theme.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.carzorrouserside.data.model.profile.WalletTransaction
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.ui.theme.viewmodel.profile.ProfileViewModel
import com.example.carzorrouserside.ui.theme.viewmodel.profile.WalletViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Coins Screen matching Figma design
 * Design: https://www.figma.com/design/LMVSAOmKLRySyuL4b9EOfV/Carzorro?node-id=12-4647
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinsScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
    
    // Get wallet balance from user basic details
    val availableBalance = (profileState.userBasicDetails?.walletBalance ?: 0.0).toInt()
    
    // Calculate coins added today (transactions with credit type from today)
    val coinsAddedToday = remember(walletState.transactions) {
        val today = java.time.LocalDate.now()
        val todayStr = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        walletState.transactions
            .filter { transaction ->
                transaction.isCredit && 
                transaction.paymentDate?.startsWith(todayStr) == true
            }
            .sumOf { (it.payableAmount ?: 0.0).toInt() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Coins",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Available Balance Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Coin icon and balance
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Coin icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFA500)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Balance amount
                    if (profileState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = appPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "$availableBalance",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = appPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Available balance text
                Text(
                    text = "Available balance",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Super coins added today banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 21.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF64CF06).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coin icon
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFA500)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "$coinsAddedToday",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5CBC06)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Super coins added today",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5CBC06)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction History Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Transaction History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Transaction list
                if (walletState.isLoading && walletState.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = appPrimary)
                    }
                } else if (walletState.error != null && walletState.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = walletState.error ?: "Failed to load transactions",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { walletViewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (walletState.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    walletState.transactions.forEachIndexed { index, transaction ->
                        WalletTransactionRow(transaction = transaction)
                        if (index < walletState.transactions.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(
                                color = Color(0xFFE0E0E0),
                                thickness = 1.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // Load more button if there are more pages
                    if (walletState.hasNextPage && !walletState.isPaginating) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = { walletViewModel.loadNextPage() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Load More")
                        }
                    }
                    
                    if (walletState.isPaginating) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = appPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun WalletTransactionRow(transaction: WalletTransaction) {
    val formattedDate = remember(transaction.paymentDate) {
        transaction.paymentDate?.let { dateStr ->
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                date?.let { outputFormat.format(it) } ?: dateStr
            } catch (e: Exception) {
                dateStr
            }
        } ?: ""
    }
    
    val transactionType = if (transaction.isCredit) TransactionType.CREDIT else TransactionType.DEBIT
    val amount = transaction.payableAmount?.toInt() ?: 0
    val title = transaction.reason ?: transaction.notes ?: "Transaction"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedDate.ifBlank { "Date not available" },
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF91989D)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (transactionType == TransactionType.CREDIT) Icons.Default.Add else Icons.Default.Remove,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (transactionType == TransactionType.CREDIT) Color(0xFF5CBC06) else Color(0xFFE64C3C)
            )
            Text(
                text = "${kotlin.math.abs(amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (transactionType == TransactionType.CREDIT) Color(0xFF5CBC06) else Color(0xFFE64C3C)
            )
        }
    }
}

enum class TransactionType {
    CREDIT,
    DEBIT
}

