package com.canyoufix.ui.components.bar

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val title = when (currentRoute) {
        "storage" -> "Хранилище"
        "generator" -> "Генератор"
        "settings" -> "Настройки"

        "password" -> "Пароли"
        "card" -> "Карты"
        "note" -> "Заметки"

        "passwordDetail/{passwordId}" -> "Подробно"
        "cardDetail/{cardId}" -> "Подробно"
        "noteDetail/{noteId}" -> "Подробно"

        "security_settings" -> "Безопасность"
        "sync_settings" -> "Синхронизация"
        "storage_settings" -> "Хранилище"
        "appearance_settings" -> "Внешний вид"


        else -> "Менеджер паролей"
    }

    val isNestedScreen = currentRoute in listOf(
        "password", "card", "note",
        "passwordDetail/{passwordId}", "cardDetail/{cardId}", "noteDetail/{noteId}",
        "security_settings", "storage_settings", "appearance_settings", "sync_settings"
    )

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (isNestedScreen) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
