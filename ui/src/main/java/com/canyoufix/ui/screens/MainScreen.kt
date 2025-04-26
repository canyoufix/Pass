package com.canyoufix.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.canyoufix.ui.components.bar.AppTopBar
import com.canyoufix.ui.components.bar.AppNavigationBar
import com.canyoufix.ui.navigation.AppNavigation

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { AppTopBar(navController) },  // Используем AppTopBar из components
        bottomBar = { AppNavigationBar(navController) }
    ) { padding ->
        AppNavigation(navController = navController, padding = padding)
    }
}