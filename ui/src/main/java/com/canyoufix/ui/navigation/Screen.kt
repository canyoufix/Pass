package com.canyoufix.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Storage : Screen("storage", "Хранилище", Icons.Default.Lock)
    object Generator : Screen("generator", "Генератор", Icons.Default.Build)
    object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
}