package com.example.carzorrouserside.ui.theme.screens.bottomnav

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.carzorrouserside.ui.theme.appPrimary
import com.example.carzorrouserside.util.BottomNavItem

@Composable
fun BottomNavBar(
    navController: NavController, currentRoute: String
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorites,
        BottomNavItem.Calender,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        // Use theme-aware colors for the container and elevation
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconId),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                        // The tint is now handled by NavigationBarItemDefaults below
                    )
                },
                // Use theme-aware colors for item states
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = appPrimary,
                    selectedTextColor = appPrimary,
                    // Use a theme color for the selection indicator
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    // Use theme colors for the unselected state
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}