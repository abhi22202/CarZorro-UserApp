package com.example.carzorrouserside.ui.theme.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.carzorrouserside.ui.theme.viewmodel.session.BookingSummaryViewModel

// Custom Colors from the image for the dialog
val DialogSubmitButton = Color(0xFF907AFF)
val DialogSelectionColor = Color(0xFF907AFF)

@Composable
fun CancelBookingDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (selectedReasonId: Int?, customReason: String?) -> Unit,
    viewModel: BookingSummaryViewModel
) {
    var cancelReasons by remember { mutableStateOf<List<com.example.carzorrouserside.data.model.booking.CancelReason>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedReasonId by remember { mutableStateOf<Int?>(null) }
    var customReason by remember { mutableStateOf("") }

    // Fetch cancellation reasons from API - no hardcoded fallbacks
    LaunchedEffect(Unit) {
        viewModel.getCancelReasons(
            onSuccess = { reasons ->
                // Filter: only active reasons with non-null, non-blank text
                cancelReasons = reasons.filter { 
                    it.is_active == 1 && 
                    !it.reason.isNullOrBlank()
                }
                isLoading = false
                errorMessage = null
            },
            onError = { error ->
                // Show error message - no hardcoded fallback reasons
                errorMessage = error
                isLoading = false
            }
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Header with Title and Close Icon ---
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Please tell the reason to cancel the service.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(end = 32.dp) // Make space for the icon
                    )
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- List of Reasons ---
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    errorMessage != null -> {
                        // Show error message if API fails - no hardcoded fallback
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Failed to load cancellation reasons",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            Text(
                                text = errorMessage ?: "Please try again later",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                    cancelReasons.isEmpty() -> {
                        // Show message if no reasons available from API
                        Text(
                            text = "No cancellation reasons available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    else -> {
                        // Show reasons from API
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            cancelReasons.forEach { reason ->
                                // Filter out reasons with null or blank text
                                val reasonText = reason.reason?.takeIf { it.isNotBlank() } ?: return@forEach
                                ReasonRow(
                                    text = reasonText,
                                    isSelected = selectedReasonId == reason.id,
                                    onClick = { selectedReasonId = reason.id }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Custom Reason Input ---
                OutlinedTextField(
                    value = customReason,
                    onValueChange = { customReason = it },
                    placeholder = { Text("My reasons are not listed here") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DialogSubmitButton,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Submit Button ---
                Button(
                    onClick = {
                        val finalCustomReason = if (customReason.isNotEmpty()) customReason else null
                        onSubmit(selectedReasonId, finalCustomReason)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DialogSubmitButton),
                    enabled = (selectedReasonId != null || customReason.isNotEmpty()) && errorMessage == null
                ) {
                    Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReasonRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isSelected) DialogSelectionColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = if (isSelected) DialogSelectionColor else MaterialTheme.colorScheme.onSurface
        )
        CustomSelectBox(isSelected = isSelected)
    }
}

@Composable
private fun CustomSelectBox(isSelected: Boolean) {
    val boxColor = if (isSelected) DialogSelectionColor else Color.Transparent
    val borderColor = if (isSelected) DialogSelectionColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color = boxColor, shape = RoundedCornerShape(6.dp))
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


// Preview commented out - requires ViewModel instance
// @Preview(showBackground = true, backgroundColor = 0x80000000)
// @Composable
// fun CancelBookingDialogPreview() {
//     MaterialTheme {
//         // CancelBookingDialog requires a ViewModel instance
//     }
// }