package com.cinepass.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FanAccent,
    secondary = FanGold,
    tertiary = FanCyan,
    background = FanBg,
    surface = FanSurface,
    surfaceVariant = FanCard,
    outline = FanBorder,
    onPrimary = FanText,
    onSecondary = FanBg,
    onTertiary = FanBg,
    onBackground = FanText,
    onSurface = FanText,
    onSurfaceVariant = FanMuted,
    error = FanNegative
)

private val LightColorScheme = lightColorScheme(
    primary = FanAccent,
    secondary = FanGold,
    tertiary = FanCyan,
    background = FanBg,
    surface = FanSurface,
    surfaceVariant = FanCard,
    outline = FanBorder,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = FanBg,
    onTertiary = FanBg,
    onBackground = FanText,
    onSurface = FanText,
    onSurfaceVariant = FanMuted,
    error = FanNegative
)

@Composable
fun CinepassTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

