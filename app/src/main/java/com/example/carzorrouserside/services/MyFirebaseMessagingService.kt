package com.example.carzorrouserside.services // Use your actual package name

// --- Add ALL necessary imports here ---
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager // For UI updates
import com.example.carzorrouserside.MainActivity // Adjust import to your MainActivity
import com.example.carzorrouserside.R // Import your R file
import com.example.carzorrouserside.data.repository.login.UserAuthRepository // Import YOUR repo // Import if using constants for broadcast
import com.example.carzorrouserside.util.AppConstants
import com.example.carzorrouserside.util.Resource // Import your Resource class (if used for result handling)
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
// import com.example.carzorrouserside.data.repository.booking.BookingRepository // Import if needed

@AndroidEntryPoint // For Hilt dependency injection
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    // Inject the repository that handles API calls
    @Inject lateinit var userAuthRepository: UserAuthRepository
    // @Inject lateinit var bookingRepository: BookingRepository // Inject if needed

    /**
     * Called when a message is received from FCM.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "🔥 Full RemoteMessage received: ${remoteMessage}")
        Log.d(TAG, "🔥 RemoteMessage.data: ${remoteMessage.data}")
        Log.d(TAG, "🔥 RemoteMessage.notification: title=${remoteMessage.notification?.title}, body=${remoteMessage.notification?.body}")


        // Prioritize handling messages with a data payload (sent from your backend)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // Extract meaningful data sent from your server
            val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title
            val body = remoteMessage.data["body"]   ?: remoteMessage.notification?.body
            // **Crucial:** Use the actual key your backend sends for the notification type
            val notificationType = remoteMessage.data["type"] // Or maybe "event", "action", etc.
            val bookingId = remoteMessage.data["bookingId"]

            Log.d(TAG, "Received Data - Type: $notificationType, Booking ID: $bookingId")

            // --- Implement Specific Logic Based on Notification Type ---
            when (notificationType) {
                // User-specific notification types
                AppConstants.NOTIFICATION_TYPE_BOOKING_ACCEPTED -> {
                    // When vendor accepts booking request
                    Log.d(TAG, "✅ Handling BookingAccepted: Vendor accepted booking request")
                    handleBookingNotification(notificationType, title, body, bookingId)
                }

                AppConstants.NOTIFICATION_TYPE_BOOKING_REQUEST -> {
                    // When vendor re-bids for a booking
                    Log.d(TAG, "✅ Handling BookingRequest: Vendor re-bid for booking")
                    handleBookingNotification(notificationType, title, body, bookingId)
                }

                // Legacy booking-related types (keep for backward compatibility)
                AppConstants.NOTIFICATION_TYPE_BOOKING_CONFIRMED,
                AppConstants.NOTIFICATION_TYPE_VENDOR_ASSIGNED,
                AppConstants.NOTIFICATION_TYPE_STATUS_UPDATE,
                AppConstants.NOTIFICATION_TYPE_BOOKING_CANCELLED,
                AppConstants.NOTIFICATION_TYPE_BOOKING_COMPLETED -> {
                    Log.d(TAG, "Handling booking-related update: $notificationType")
                    handleBookingNotification(notificationType, title, body, bookingId)
                }

                AppConstants.NOTIFICATION_TYPE_PROMO -> {
                    Log.d(TAG, "Handling promotional message.")
                    // Usually just show the notification for promos
                    sendNotification(title, body, null) // No specific ID needed
                }

                else -> {
                    Log.w(TAG, "Received unhandled notification type: $notificationType")
                    // Show a generic notification if title/body exist
                    if (!title.isNullOrBlank() || !body.isNullOrBlank()){
                        sendNotification(title, body, bookingId) // Show notification anyway
                    }
                }
            }
            // --- End Specific Logic ---

        } else if (remoteMessage.notification != null) {
            // Fallback for messages that *only* have a notification payload
            Log.d(TAG, "Received Notification-only message: ${remoteMessage.notification?.body}")
            // Show notification using the payload's title/body
            sendNotification(remoteMessage.notification?.title, remoteMessage.notification?.body, null)
        }
    }

    /**
     * Handles booking-related notifications with broadcast and notification display
     */
    private fun handleBookingNotification(
        notificationType: String?,
        title: String?,
        body: String?,
        bookingId: String?
    ) {
        // Send local broadcast to update UI if screens are open
        val updateIntent = Intent(AppConstants.ACTION_BOOKING_UPDATE)
        updateIntent.putExtra("bookingId", bookingId)
        updateIntent.putExtra("message", body)
        updateIntent.putExtra("type", notificationType)
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent)
        Log.d(TAG, "Sent local broadcast ACTION_BOOKING_UPDATE for booking $bookingId with type $notificationType")

        // Show notification with booking deep link
        sendNotification(title, body, bookingId)
    }

    /**
     * Called when FCM generates a new token or invalidates the current one.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed FCM token: $token")
        sendRegistrationToServer(token)
    }

    /**
     * Sends the FCM registration token to your backend server via the repository.
     */
    private fun sendRegistrationToServer(token: String?) {
        if (token == null) {
            Log.w(TAG, "Cannot send null FCM token to server.")
            Log.d(TAG, ">>> Preparing FCM Token Update Request:")
            //Log.d(TAG, "userId = $currentUserId")
            Log.d(TAG, "deviceId = ${fetchDeviceId()}")
            Log.d(TAG, "fcmToken = $token")

            return
        }

        // Get the current User ID (ensure this function exists and works in UserAuthRepository)
        val currentUserId = userAuthRepository.getCurrentUserId()

        if (currentUserId != null) {
            Log.d(TAG, "Sending FCM token to server via repository for user: $currentUserId")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = userAuthRepository.updateFcmToken(
                        userId = currentUserId,
                        fcmToken = token,
                        deviceId =  fetchDeviceId()
                    )

                    if (result is Resource.Error) {
                        Log.e(TAG, "Failed to send FCM token via repository: ${result.message}")
                        // TODO: Implement retry logic or better error handling if needed
                    } else {
                        Log.d(TAG, "FCM token sent to server successfully.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling updateFcmToken from service", e)
                }
            }
        } else {
            Log.w(TAG, "Cannot send FCM token: User ID not found. Token needs sending after login.")
        }
    }
     fun fetchDeviceId(): String {
        return try {
            android.provider.Settings.Secure.getString(
                contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "unknown_device"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device ID: ${e.localizedMessage}")
            "unknown_device"
        }
    }

    /**
     * Creates and displays a system notification based on received message data.
     */
    private fun sendNotification(title: String?, messageBody: String?, bookingId: String?) {
        Log.d(TAG, "Attempting to display notification: Title='$title', Body='$messageBody', BookingID='$bookingId'")

        // 1. Create Intent for MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (bookingId != null) {
            intent.putExtra("bookingId", bookingId) // Pass bookingId for tap handling
            Log.d(TAG, "Adding bookingId extra to intent: $bookingId")
        }

        // 2. Create PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            bookingId?.hashCode() ?: System.currentTimeMillis().toInt() /* Unique Request code */,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Channel ID and Sound
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 4. Build Notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo) // *** YOUR ICON ***
            .setContentTitle(title ?: getString(R.string.app_name))
            .setContentText(messageBody ?: "$title")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent) // Tap action
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For heads-up
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody ?: "$messageBody")) // Show full text
            .setColor(getColor(R.color.purple_500)) // *** YOUR COLOR ***
        // Optional: .setLargeIcon(bitmap)
        // Optional: .addAction(...)

        // 5. Get Notification Manager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 6. Create Channel (Android 8+) - Ensure this matches Application class code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Booking Updates", // User-visible name
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about your car wash bookings"
            }
            notificationManager.createNotificationChannel(channel)
            // Log.d(TAG, "Notification channel checked/created: $channelId") // Optional log
        }

        // 7. Show Notification
        val notificationId = bookingId?.hashCode() ?: System.currentTimeMillis().toInt() // Unique ID
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notification successfully displayed with ID: $notificationId")
    }
}