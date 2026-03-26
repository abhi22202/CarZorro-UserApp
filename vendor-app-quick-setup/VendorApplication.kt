package com.yourvendorpackage

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

class VendorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channel = NotificationChannel(
                channelId,
                "Booking Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about new booking requests"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("VendorApp", "Notification channel created: $channelId")
        }
    }
}


