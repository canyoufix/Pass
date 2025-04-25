package com.canyoufix.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.canyoufix.ui.navigation.Screen
import androidx.compose.foundation.layout.size

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Storage,
        Screen.Generator,
        Screen.Settings
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface, // фон навигационной панели
        contentColor = MaterialTheme.colorScheme.onSurface  // цвет иконок по умолчанию
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = screen.icon(),
                        contentDescription = screen.title,
                        modifier = Modifier.size(26.dp) // увеличь по вкусу
                    )
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    unselectedTextColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) // "фон" под иконкой при выборе
                )
            )
        }
    }
}