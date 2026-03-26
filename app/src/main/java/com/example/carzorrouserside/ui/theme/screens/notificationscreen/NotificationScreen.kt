package com.example.carzorrouserside.ui.theme.screens.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.carzorrouserside.R
import com.example.carzorrouserside.ui.theme.CarZorroTheme
import com.example.carzorrouserside.ui.theme.appPrimary

// --- Data Models for Notifications ---

enum class NotificationType {
    REQUEST, COMMENT, ALERT
}

data class NotificationItemData(
    val id: Int,
    val type: NotificationType,
    val user: String,
    val userImage: Int? = null, // Drawable resource ID
    val actionText: String,
    val subject: String? = null, // e.g., "Full car wash service"
    val time: String,
    val comment: String? = null,
    val dateGroup: String // "Today" or "Yesterday"
)

// --- Expanded Dummy Data (10 items) ---

val dummyNotifications = listOf(
    // Today
    NotificationItemData(1, NotificationType.REQUEST, "Ashwin Bose", null, "is requesting for the", "car wash service at 5:00 PM.", "2m", dateGroup = "Today"),
    NotificationItemData(2, NotificationType.COMMENT, "Patrick", R.drawable.logo, "added a comment on", "Full car wash service", "8h", comment = "\"Looks perfect!\"", dateGroup = "Today"),
    NotificationItemData(3, NotificationType.COMMENT, "Jane Doe", R.drawable.logo, "replied to your review", "on Satbir Motors", "10h", comment = "\"Thanks for the feedback!\"", dateGroup = "Today"),

    // Yesterday
    NotificationItemData(4, NotificationType.ALERT, "New Feature Alert!", null, "We're pleased to introduce the latest enhancements in our templating experience.", null, "24h", dateGroup = "Yesterday"),
    NotificationItemData(5, NotificationType.REQUEST, "Rajit Gupta", null, "has accepted your", "booking for tomorrow.", "1d", dateGroup = "Yesterday"),
    NotificationItemData(6, NotificationType.COMMENT, "CarZorro Support", R.drawable.logo, "updated your ticket", "#CZ-12345", "1d", comment = "\"We have resolved the issue.\"", dateGroup = "Yesterday"),
    NotificationItemData(7, NotificationType.ALERT, "Special Offer!", null, "Get 20% off on all Wash & Coat packages this weekend. Use code WEEKEND20.", null, "1d", dateGroup = "Yesterday"),

    // Older
    NotificationItemData(8, NotificationType.REQUEST, "Pravesh Singh", null, "completed the service on", "your Hyundai Creta.", "2d", dateGroup = "Older"),
    NotificationItemData(9, NotificationType.COMMENT, "Admin", R.drawable.logo, "sent a new message", "regarding your subscription", "3d", comment = "\"Your monthly plan is about to renew.\"", dateGroup = "Older"),
    NotificationItemData(10, NotificationType.ALERT, "Account Security", null, "A new device was used to log into your account. If this wasn't you, please secure your account.", null, "4d", dateGroup = "Older")
)


// --- Main Screen Composable ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    // Group notifications by the 'dateGroup' property
    val groupedNotifications = dummyNotifications.groupBy { it.dateGroup }
        .entries.sortedBy {
            when (it.key) { // Custom sort order
                "Today" -> 0
                "Yesterday" -> 1
                "Older" -> 2
                else -> 3
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Use app theme's primary color
                    titleContentColor = MaterialTheme.colorScheme.onPrimary, // Text color on primary
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary // Icon color on primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background // Use theme's background color
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedNotifications.forEach { (date, notifications) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(notifications, key = { it.id }) { notification ->
                    when (notification.type) {
                        NotificationType.REQUEST -> RequestNotificationItem(notification)
                        NotificationType.COMMENT -> CommentNotificationItem(notification)
                        NotificationType.ALERT -> AlertNotificationItem(notification)
                    }
                }
            }
        }
    }
}

// --- Notification Item Composables ---

@Composable
fun RequestNotificationItem(data: NotificationItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicator dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(appPrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Card content
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = appPrimary.copy(alpha = 0.08f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(appPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.user.split(" ").take(2).map { it.first() }.joinToString(""),
                        color = appPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(data.user)
                            }
                            append(" ${data.actionText} ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(data.subject)
                            }
                        },
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { /* Handle Accept */ },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = appPrimary),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text("Accept")
                        }
                        // Decline Button styled to match screenshot
                        Button(
                            onClick = { /* Handle Decline */ },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text("Decline")
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = data.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    IconButton(onClick = { /* More options */ }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                }
            }
        }
    }
}


@Composable
fun CommentNotificationItem(data: NotificationItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = data.userImage!!),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(data.user)
                        }
                        append(" ${data.actionText} on ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(data.subject)
                        }
                    },
                    lineHeight = 20.sp
                )

                data.comment?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.background,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = data.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = { /* More options */ }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }
            }
        }
    }
}

@Composable
fun AlertNotificationItem(data: NotificationItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Feature Icon",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = data.user, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = data.actionText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Try Now */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appPrimary.copy(alpha = 0.1f)),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Try now", color = appPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = data.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = { /* More options */ }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }
            }
        }
    }
}


// --- Preview ---

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewNotificationScreen() {
    CarZorroTheme(darkTheme = false) {
        NotificationScreen(rememberNavController())
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun PreviewNotificationScreenDark() {
    CarZorroTheme(darkTheme = true) {
        NotificationScreen(rememberNavController())
    }
}