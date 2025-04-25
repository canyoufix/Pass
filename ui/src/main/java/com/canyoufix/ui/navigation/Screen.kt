package com.canyoufix.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.canyoufix.ui.R

sealed class Screen(
    val route: String,
    val title: String,
    @DrawableRes private val iconRes: Int
) {
    object Storage : Screen("storage", "Хранилище", R.drawable.ic_storage)
    object Generator : Screen("generator", "Генератор", R.drawable.ic_generator)
    object Settings : Screen("settings", "Настройки", R.drawable.ic_settings)

    @Composable
    fun icon(): Painter = painterResource(id = iconRes)
}