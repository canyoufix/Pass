package com.canyoufix.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.canyoufix.data.viewmodel.CardViewModel
import com.canyoufix.data.viewmodel.NoteViewModel
import com.canyoufix.data.viewmodel.PasswordViewModel
import com.canyoufix.ui.screens.menu.GeneratorScreen
import com.canyoufix.ui.screens.menu.SettingsScreen
import com.canyoufix.ui.screens.menu.StorageScreen
import com.canyoufix.ui.screens.storage.CardScreen
import com.canyoufix.ui.screens.storage.NoteScreen
import com.canyoufix.ui.screens.storage.PasswordScreen
import org.koin.androidx.compose.koinViewModel


@Composable
fun AppNavigation(navController: NavHostController, padding: PaddingValues) {
    // Создаем ViewModel в корневом Composable
    val passwordViewModel: PasswordViewModel = koinViewModel()
    val cardViewModel: CardViewModel = koinViewModel()
    val noteViewModel: NoteViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = "storage",
        modifier = Modifier.padding(padding)
    ) {
        // Main menu
        composable("storage") {
            StorageScreen(
                navController = navController,
                passwordViewModel = passwordViewModel,
                cardViewModel = cardViewModel,
                noteViewModel = noteViewModel
            )
        }

        composable("generator") {
            GeneratorScreen()
        }

        composable("settings") {
            SettingsScreen()
        }

        // Storage
        composable("password") {
            PasswordScreen(
                navController = navController,
                viewModel = passwordViewModel // Передаем тот же экземпляр
            )
        }

        composable("card") {
            CardScreen(
                navController = navController,
                viewModel = cardViewModel // Передаем тот же экземпляр
            )
        }

        composable("note") {
            NoteScreen(
                navController = navController,
                viewModel = noteViewModel // Передаем тот же экземпляр
            )
        }
    }
}