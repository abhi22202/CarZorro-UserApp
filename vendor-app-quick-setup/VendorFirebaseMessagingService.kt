package com.yourvendorpackage.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.yourvendorpackage.MainActivity
import com.yourvendorpackage.R
import com.yourvendorpackage.util.VendorAppConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM Service for Vendor App
 * Handles BookingRequest notifications when user creates a booking
 */
class VendorFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "VendorFCMService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "📨 FCM Message received from: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "📦 Message data: ${remoteMessage.data}")

            val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title
            val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body
            val notificationType = remoteMessage.data["type"]
            val bookingId = remoteMessage.data["bookingId"]

            Log.d(TAG, "Type: $notificationType, Booking ID: $bookingId")

            when (notificationType) {
                VendorAppConstants.NOTIFICATION_TYPE_BOOKING_REQUEST -> {
                    Log.d(TAG, "🆕 New BookingRequest received")
                    handleBookingRequest(title, body, bookingId)
                }
                VendorAppConstants.NOTIFICATION_TYPE_BOOKING_ACCEPTED -> {
                    Log.d(TAG, "✅ BookingAccepted received")
                    handleBookingAccepted(title, body, bookingId)
                }
                else -> {
                    Log.w(TAG, "⚠️ Unhandled type: $notificationType")
                    if (!title.isNullOrBlank() || !body.isNullOrBlank()) {
                        sendNotification(title, body, bookingId)
                    }
                }
            }
        } else if (remoteMessage.notification != null) {
            sendNotification(
                remoteMessage.notification?.title,
                remoteMessage.notification?.body,
                null
            )
        }
    }

    private fun handleBookingRequest(
        title: String?,
        body: String?,
        bookingId: String?
    ) {
        // Send broadcast for UI updates
        val intent = Intent(VendorAppConstants.ACTION_BOOKING_UPDATE).apply {
            putExtra("bookingId", bookingId)
            putExtra("type", "BookingRequest")
            putExtra("message", body)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        
        // Show notification
        sendNotification(title, body, bookingId)
    }

    private fun handleBookingAccepted(
        title: String?,
        body: String?,
        bookingId: String?
    ) {
        val intent = Intent(VendorAppConstants.ACTION_BOOKING_UPDATE).apply {
            putExtra("bookingId", bookingId)
            putExtra("type", "BookingAccepted")
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        sendNotification(title, body, bookingId)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "🔑 New FCM token: $token")
        // TODO: Send token to backend: vendorRepository.updateFcmToken(token)
    }

    private fun sendNotification(
        title: String?,
        messageBody: String?,
        bookingId: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (bookingId != null) {
                putExtra("bookingId", bookingId)
                putExtra("openBookingDetails", true)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            bookingId?.hashCode() ?: System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: "New Booking Request")
            .setContentText(messageBody ?: "You have a new booking request")
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setColor(getColor(R.color.your_primary_color))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Booking Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about new booking requests"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = bookingId?.hashCode() ?: System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "✅ Notification displayed: $notificationId")
    }
}


