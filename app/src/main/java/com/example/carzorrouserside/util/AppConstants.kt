package com.example.carzorrouserside.util // Or com.example.carzorrouserside.util

object AppConstants {
    // Action string for Local Broadcast
    const val ACTION_BOOKING_UPDATE = "com.example.carzorrouserside.BOOKING_UPDATE"

    // FCM Notification Types
    // User-specific notification types
    const val NOTIFICATION_TYPE_BOOKING_ACCEPTED = "BookingAccepted"  // Vendor accepts booking request
    const val NOTIFICATION_TYPE_BOOKING_REQUEST = "BookingRequest"    // Vendor re-bids for a booking
    
    // Legacy notification types (for backward compatibility)
    const val NOTIFICATION_TYPE_BOOKING_CONFIRMED = "BOOKING_CONFIRMED"
    const val NOTIFICATION_TYPE_VENDOR_ASSIGNED = "VENDOR_ASSIGNED"
    const val NOTIFICATION_TYPE_STATUS_UPDATE = "STATUS_UPDATE"
    const val NOTIFICATION_TYPE_BOOKING_CANCELLED = "BOOKING_CANCELLED"
    const val NOTIFICATION_TYPE_BOOKING_COMPLETED = "BOOKING_COMPLETED"
    const val NOTIFICATION_TYPE_PROMO = "PROMO"
}