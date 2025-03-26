package com.canyoufix.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.data.viewmodel.CardViewModel
import com.canyoufix.data.viewmodel.NoteViewModel
import com.canyoufix.data.viewmodel.PasswordViewModel
import com.canyoufix.ui.screens.MainScreen
import com.canyoufix.ui.screens.auth.AuthScreen
import com.canyoufix.ui.screens.auth.SetupScreen
import com.canyoufix.ui.screens.menu.GeneratorScreen
import com.canyoufix.ui.screens.menu.SettingsScreen
import com.canyoufix.ui.screens.menu.StorageScreen
import com.canyoufix.ui.screens.storage.CardScreen
import com.canyoufix.ui.screens.storage.NoteScreen
import com.canyoufix.ui.screens.storage.PasswordScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun RootNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefsManager = remember { SecurePrefsManager(context) }

    // Проверяем, установлен ли мастер-пароль
    val startDestination = remember {
        if (prefsManager.getSalt() == null) "setup" else "auth"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Авторизационный поток
        composable("setup") {
            SetupScreen {
                navController.navigate("main") {
                    popUpTo("setup") { inclusive = true }
                }
            }
        }

        composable("auth") {
            AuthScreen(
                onSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onFail = { error ->
                    // Можно показать ошибку или обработать иначе
                }
            )
        }

        // Основной поток приложения
        composable("main") {
            MainScreen()
        }
    }
}