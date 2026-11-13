package com.canyoufix.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

internal fun glassColors(dark: Boolean): ColorScheme =
    if (dark)
        darkColorScheme(
            primary = Color(0xFFF2F2F7),
            onPrimary = Color(0xFF1C1C1E),
            primaryContainer = Color(0xFF3A3A3C),
            onPrimaryContainer = Color(0xFFFFFFFF),
            secondary = Color(0xFFAEAEB2),
            onSecondary = Color(0xFF1C1C1E),
            secondaryContainer = Color(0xFF3A3A3C),
            onSecondaryContainer = Color(0xFFF2F2F7),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF2F2F7),
            surface = Color(0xFF1C1C1E),
            onSurface = Color(0xFFF2F2F7),
            surfaceVariant = Color(0xFF2C2C2E),
            onSurfaceVariant = Color(0xFFAEAEB2),
            surfaceContainer = Color(0xFF1C1C1E),
            surfaceContainerLow = Color(0xFF121212),
            surfaceContainerHigh = Color(0xFF2C2C2E),
            surfaceContainerHighest = Color(0xFF3A3A3C),
            outline = Color(0xFF636366),
            outlineVariant = Color(0xFF38383A),
            error = Color(0xFFE6AAA7),
        )
    else
        lightColorScheme(
            primary = Color(0xFF3A3A3C),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE5E5EA),
            onPrimaryContainer = Color(0xFF1C1C1E),
            secondary = Color(0xFF636366),
            secondaryContainer = Color(0xFFE5E5EA),
            background = Color(0xFFF2F2F7),
            onBackground = Color(0xFF1C1C1E),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1C1C1E),
            surfaceVariant = Color(0xFFE5E5EA),
            onSurfaceVariant = Color(0xFF636366),
            outline = Color(0xFF8E8E93),
            outlineVariant = Color(0xFFD1D1D6),
        )

internal val GlassShapes =
    Shapes(
        extraSmall = RoundedCornerShape(10.dp),
        small = RoundedCornerShape(14.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(26.dp),
        extraLarge = RoundedCornerShape(32.dp),
    )

@Composable
internal fun GlassBackdrop(content: @Composable BoxScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize().background(colors.background)) {
        CompositionLocalProvider(LocalContentColor provides colors.onBackground) { content() }
    }
}

@Composable
internal fun GlassPanel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .clip(shape)
            .background(colors.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = {
            CompositionLocalProvider(LocalContentColor provides colors.onSurface) { content() }
        },
    )
}
