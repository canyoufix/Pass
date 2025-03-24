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

    // Определение информации для заголовка и иконки
    val screenInfo = remember(currentRoute) {
        when (currentRoute) {
            "storage" -> "Хранилище" to Icons.Default.Lock
            "generator" -> "Генератор" to Icons.Default.Settings
            "settings" -> "Настройки" to Icons.Default.Settings
            else -> "Менеджер паролей" to Icons.Default.Lock
        }
    }


    val isNestedScreen = currentRoute in listOf("password", "card", "note")

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = screenInfo.second, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(screenInfo.first)
            }
        },
        navigationIcon = {
            if (isNestedScreen) { // Стрелка назад только для вложенных экранов
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