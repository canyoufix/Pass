package com.canyoufix.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.ui.screens.MainScreen
import com.canyoufix.ui.screens.auth.AuthScreen
import com.canyoufix.ui.screens.auth.SetupScreen

@Composable
fun AuthNavigation() {
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
                    // Очищаем весь стек до setup включительно
                    popUpTo("setup") { inclusive = true }
                }
            }
        }

        composable("auth") {
            AuthScreen(
                onSuccess = {
                    navController.navigate("main") {
                        // Очищаем весь стек до auth включительно
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onFail = { error -> /* обработка ошибки */ },
                onResetComplete = {
                    navController.navigate("setup") {
                        // Очищаем стек до auth перед переходом на setup
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        // Основной поток приложения
        composable("main") {
            MainScreen()
        }
    }
}