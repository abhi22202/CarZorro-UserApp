# Vendor App FCM Notification Implementation Guide

## 🎯 Goal
Display FCM notifications in vendor app when backend sends `BookingRequest` notifications.

---

## 📋 Implementation Checklist

1. ✅ Firebase setup (google-services.json)
2. ✅ Dependencies (build.gradle.kts)
3. ✅ Application class (notification channel)
4. ✅ FCM Service (receive notifications)
5. ✅ AndroidManifest configuration
6. ✅ Constants file
7. ✅ Deep linking (optional)

---

## 🔧 Step-by-Step Implementation

### Step 1: Firebase Setup (If Not Done)

1. Add `google-services.json` to `app/` folder
2. Add plugin in `build.gradle.kts`:
```kotlin
plugins {
    id("com.google.gms.google-services")
}
```

### Step 2: Add Dependencies

In `app/build.gradle.kts`:

```kotlin
dependencies {
    // Firebase Cloud Messaging
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // LocalBroadcastManager (if using for UI updates)
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
}
```

---

## 📁 Step 3: Create Constants File

**File:** `app/src/main/java/com/yourvendorpackage/util/VendorAppConstants.kt`

```kotlin
package com.yourvendorpackage.util

object VendorAppConstants {
    // Action string for Local Broadcast
    const val ACTION_BOOKING_UPDATE = "com.yourvendorpackage.BOOKING_UPDATE"

    // FCM Notification Types for Vendor
    const val NOTIFICATION_TYPE_BOOKING_REQUEST = "BookingRequest"    // New booking request
    const val NOTIFICATION_TYPE_BOOKING_ACCEPTED = "BookingAccepted"   // User accepted re-bid
    
    // Legacy types (optional)
    const val NOTIFICATION_TYPE_BOOKING_CANCELLED = "BOOKING_CANCELLED"
    const val NOTIFICATION_TYPE_BOOKING_COMPLETED = "BOOKING_COMPLETED"
}
```

---

## 📁 Step 4: Create Application Class

**File:** `app/src/main/java/com/yourvendorpackage/VendorApplication.kt`

```kotlin
package com.yourvendorpackage

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp  // If using Hilt, otherwise remove
class VendorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val name = "Booking Requests" // User-visible name
            val descriptionText = "Notifications about new booking requests"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("VendorApp", "Notification channel created: $channelId")
        }
    }
}
```

---

## 📁 Step 5: Create FCM Service (Most Important!)

**File:** `app/src/main/java/com/yourvendorpackage/services/VendorFirebaseMessagingService.kt`

```kotlin
package com.yourvendorpackage.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.yourvendorpackage.MainActivity  // Your main activity
import com.yourvendorpackage.R
import com.yourvendorpackage.util.VendorAppConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint  // If using Hilt

@AndroidEntryPoint  // If using Hilt, otherwise remove
class VendorFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "VendorFCMService"

    /**
     * Called when a message is received from FCM.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "📨 FCM Message received from: ${remoteMessage.from}")

        // Prioritize handling messages with a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "📦 Message data payload: ${remoteMessage.data}")

            // Extract data sent from backend
            val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title
            val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body
            val notificationType = remoteMessage.data["type"]
            val bookingId = remoteMessage.data["bookingId"]
            val customerName = remoteMessage.data["customerName"]
            val amount = remoteMessage.data["amount"]
            val serviceType = remoteMessage.data["serviceType"]

            Log.d(TAG, "✅ Extracted Data:")
            Log.d(TAG, "   Type: $notificationType")
            Log.d(TAG, "   Booking ID: $bookingId")
            Log.d(TAG, "   Customer: $customerName")
            Log.d(TAG, "   Amount: $amount")
            Log.d(TAG, "   Service Type: $serviceType")

            // Handle notification based on type
            when (notificationType) {
                VendorAppConstants.NOTIFICATION_TYPE_BOOKING_REQUEST -> {
                    Log.d(TAG, "🆕 Handling BookingRequest: New booking request received")
                    handleBookingRequestNotification(title, body, bookingId, customerName, amount, serviceType)
                }

                VendorAppConstants.NOTIFICATION_TYPE_BOOKING_ACCEPTED -> {
                    Log.d(TAG, "✅ Handling BookingAccepted: User accepted your re-bid")
                    handleBookingAcceptedNotification(title, body, bookingId)
                }

                else -> {
                    Log.w(TAG, "⚠️ Received unhandled notification type: $notificationType")
                    // Show generic notification anyway
                    if (!title.isNullOrBlank() || !body.isNullOrBlank()) {
                        sendNotification(title, body, bookingId)
                    }
                }
            }

        } else if (remoteMessage.notification != null) {
            // Fallback for notification-only messages
            Log.d(TAG, "📢 Received Notification-only message: ${remoteMessage.notification?.body}")
            sendNotification(
                remoteMessage.notification?.title,
                remoteMessage.notification?.body,
                null
            )
        }
    }

    /**
     * Handle BookingRequest notification (New booking request)
     */
    private fun handleBookingRequestNotification(
        title: String?,
        body: String?,
        bookingId: String?,
        customerName: String?,
        amount: String?,
        serviceType: String?
    ) {
        // Send broadcast to update UI if booking screen is open
        val updateIntent = Intent(VendorAppConstants.ACTION_BOOKING_UPDATE)
        updateIntent.putExtra("bookingId", bookingId)
        updateIntent.putExtra("type", "BookingRequest")
        updateIntent.putExtra("customerName", customerName)
        updateIntent.putExtra("amount", amount)
        updateIntent.putExtra("serviceType", serviceType)
        updateIntent.putExtra("message", body)
        
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent)
        Log.d(TAG, "📡 Sent broadcast: ACTION_BOOKING_UPDATE for booking $bookingId")

        // Show system notification
        sendNotification(title, body, bookingId)
    }

    /**
     * Handle BookingAccepted notification (User accepted re-bid)
     */
    private fun handleBookingAcceptedNotification(
        title: String?,
        body: String?,
        bookingId: String?
    ) {
        // Send broadcast
        val updateIntent = Intent(VendorAppConstants.ACTION_BOOKING_UPDATE)
        updateIntent.putExtra("bookingId", bookingId)
        updateIntent.putExtra("type", "BookingAccepted")
        updateIntent.putExtra("message", body)
        
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent)

        // Show notification
        sendNotification(title, body, bookingId)
    }

    /**
     * Called when FCM generates a new token or invalidates the current one.
     * IMPORTANT: Register this token with your backend!
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "🔑 Refreshed FCM token: $token")
        // TODO: Send token to backend via API
        // Example: vendorRepository.updateFcmToken(token)
    }

    /**
     * Creates and displays a system notification
     */
    private fun sendNotification(
        title: String?,
        messageBody: String?,
        bookingId: String?
    ) {
        Log.d(TAG, "📲 Displaying notification: Title='$title', Body='$messageBody', BookingID='$bookingId'")

        // Create Intent to open booking details when notification is tapped
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (bookingId != null) {
            intent.putExtra("bookingId", bookingId)
            intent.putExtra("openBookingDetails", true)
            Log.d(TAG, "📍 Added bookingId to intent: $bookingId")
        }

        // Create PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            bookingId?.hashCode() ?: System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Channel ID and Sound
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Build Notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)  // Your notification icon
            .setContentTitle(title ?: "New Booking Request")
            .setContentText(messageBody ?: "You have a new booking request")
            .setAutoCancel(true)  // Dismiss when tapped
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)  // Tap action
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // Heads-up notification
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody ?: "New booking request"))
            .setColor(getColor(R.color.your_primary_color))  // Your app's primary color
            .setVibrate(longArrayOf(0, 250, 250, 250))  // Vibration pattern

        // Get Notification Manager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Booking Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about new booking requests"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Show Notification
        val notificationId = bookingId?.hashCode() ?: System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "✅ Notification displayed with ID: $notificationId")
    }
}
```

---

## 📁 Step 6: Update AndroidManifest.xml

Add to `<application>` tag:

```xml
<application
    android:name=".VendorApplication"  <!-- Your Application class -->
    ...>

    <!-- FCM Service -->
    <service
        android:name=".services.VendorFirebaseMessagingService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>

    <!-- FCM Notification Metadata -->
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_icon"
        android:resource="@drawable/ic_notification" />
    
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_color"
        android:resource="@color/your_primary_color" />
    
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_channel_id"
        android:value="@string/default_notification_channel_id" />

    <!-- Your activities... -->
</application>
```

---

## 📁 Step 7: Add String Resource

In `app/src/main/res/values/strings.xml`:

```xml
<string name="default_notification_channel_id" translatable="false">vendor_booking_channel</string>
```

---

## 📁 Step 8: Handle Deep Linking (Optional)

In your `MainActivity.kt`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Handle notification deep link
    handleNotificationIntent(intent)
    
    // ... rest of your code
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleNotificationIntent(intent)
}

private fun handleNotificationIntent(intent: Intent) {
    if (intent.hasExtra("bookingId") && intent.hasExtra("openBookingDetails")) {
        val bookingId = intent.getStringExtra("bookingId")
        Log.d("MainActivity", "📍 Opening booking details for: $bookingId")
        
        // Navigate to booking details screen
        // Example: navController.navigate("booking_details/$bookingId")
        
        // Clear extras to prevent re-navigation
        intent.removeExtra("bookingId")
        intent.removeExtra("openBookingDetails")
    }
}
```

---

## 📱 Step 9: Listen to Broadcast (Optional - For UI Updates)

If you want to update UI when notification arrives while app is open:

**In your Booking Screen/ViewModel:**

```kotlin
// Register broadcast receiver
val broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val bookingId = intent?.getStringExtra("bookingId")
        val type = intent?.getStringExtra("type")
        
        when (type) {
            "BookingRequest" -> {
                // Refresh booking list or show new booking
                viewModel.refreshBookings()
                // Or show snackbar: "New booking request received!"
            }
        }
    }
}

// Register in onCreate/LaunchedEffect
LocalBroadcastManager.getInstance(context).registerReceiver(
    broadcastReceiver,
    IntentFilter(VendorAppConstants.ACTION_BOOKING_UPDATE)
)

// Don't forget to unregister in onDestroy
```

---

## 🧪 Testing

### Test Notification Format

Your backend should send FCM with this format:

```json
{
  "token": "vendor_fcm_token",
  "data": {
    "type": "BookingRequest",
    "title": "New Booking Request",
    "body": "A customer needs your service. Amount: ₹1500",
    "bookingId": "123",
    "customerName": "John Doe",
    "serviceType": "doorstep",
    "amount": "1500",
    "dateTime": "2024-11-12 14:30:00"
  }
}
```

### Test Steps:

1. ✅ Install vendor app on device
2. ✅ Login as vendor
3. ✅ Register FCM token with backend
4. ✅ From user app, create a booking (click "Find Vendors")
5. ✅ Check vendor app - should receive notification
6. ✅ Tap notification - should open booking details

---

## 🔍 Troubleshooting

### Notification Not Showing?

1. ✅ Check FCM token is registered with backend
2. ✅ Check Logcat for FCM service logs
3. ✅ Verify notification channel is created
4. ✅ Check app has notification permissions
5. ✅ Verify backend is sending correct payload

### Logcat Commands:

```bash
# Filter FCM logs
adb logcat | grep "VendorFCMService"

# Check notification channel creation
adb logcat | grep "Notification channel"
```

---

## ✅ Summary

1. ✅ Create `VendorFirebaseMessagingService`
2. ✅ Create `VendorApplication` with notification channel
3. ✅ Update `AndroidManifest.xml`
4. ✅ Add constants file
5. ✅ Handle deep linking in `MainActivity`
6. ✅ (Optional) Listen to broadcasts for UI updates

**Once implemented, vendors will see notifications when:**
- User clicks "Find Vendors"
- Backend calls `startBooking` API
- Backend sends FCM with `type: "BookingRequest"`

---

## 📝 Notes

- **FCM Token Registration**: Don't forget to register vendor's FCM token with backend (similar to user app)
- **Notification Icons**: Make sure you have `ic_notification` drawable
- **Colors**: Use your vendor app's theme colors
- **Deep Linking**: Adjust navigation routes to match your app's structure


