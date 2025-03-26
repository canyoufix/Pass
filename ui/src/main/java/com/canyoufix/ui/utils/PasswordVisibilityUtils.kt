package com.canyoufix.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Утилита для управления видимостью пароля в текстовых полях
 */
data class PasswordVisibilityState(
    val isVisible: Boolean,
    val icon: ImageVector,
    val description: String,
    val visualTransformation: VisualTransformation,
    val toggle: () -> Unit
)

/**
 * Запоминаемое состояние видимости пароля
 *
 * @param initiallyVisible Начальное состояние видимости (по умолчанию false - скрыт)
 */
@Composable
fun rememberPasswordVisibilityState(
    initiallyVisible: Boolean = false
): PasswordVisibilityState {
    var isVisible by remember { mutableStateOf(initiallyVisible) }

    return PasswordVisibilityState(
        isVisible = isVisible,
        icon = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
        description = if (isVisible) "Скрыть пароль" else "Показать пароль",
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        toggle = { isVisible = !isVisible }
    )
}