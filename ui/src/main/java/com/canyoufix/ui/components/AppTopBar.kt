package com.canyoufix.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

        else -> "Менеджер паролей"
    }

    val isNestedScreen = currentRoute in listOf(
        "password", "card", "note",
        "passwordDetail/{passwordId}", "cardDetail/{cardId}", "noteDetail/{noteId}",
        "security_settings", "storage_settings", "appearance_settings"
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
