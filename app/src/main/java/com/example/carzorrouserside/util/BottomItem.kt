package com.example.carzorrouserside.util

import com.example.carzorrouserside.R
import com.example.carzorrouserside.ui.theme.navigation.Routes

sealed class BottomNavItem(val route: String, val label: String, val iconId: Int) {
    object Home : BottomNavItem("home_screen", "Home", R.drawable.home)
    object Favorites : BottomNavItem(Routes.FAVOURITES_SCREEN, "Favorites", R.drawable.fav)
    object Calender : BottomNavItem(Routes.BOOKING_HISTORY_SCREEN, "My Bookings", R.drawable.calendar)
    object Notifications : BottomNavItem(Routes.NOTIFICATION_SCREEN, "Notifications", R.drawable.notification)
    object Profile : BottomNavItem(Routes.PROFILE_SCREEN, "Profile", R.drawable.baseline_person_24)
}