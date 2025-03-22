package com.canyoufix.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.canyoufix.ui.screens.menu.GeneratorScreen
import com.canyoufix.ui.screens.menu.SettingsScreen
import com.canyoufix.ui.screens.menu.StorageScreen
import com.canyoufix.ui.screens.storage.CardScreen
import com.canyoufix.ui.screens.storage.NoteScreen
import com.canyoufix.ui.screens.storage.PasswordScreen


@Composable
fun AppNavigation(navController: NavHostController, padding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "storage",
        modifier = Modifier.padding(padding)
    ) {
        // Main menu
        composable("storage") {
            StorageScreen(navController = navController)
        }
        composable("generator") {
            GeneratorScreen()
        }
        composable("settings") {
            SettingsScreen()
        }


        // Storage
        composable("login") {
            PasswordScreen(navController = navController)
        }
        composable("card") {
            CardScreen(navController = navController)
        }
        composable("note") {
            NoteScreen(navController = navController)
        }
    }
}