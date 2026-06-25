package com.jamia.madinatulilm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
    primary = LuxuryGold,
    onPrimary = ForestGreen,
    secondary = ForestGreenLight,
    onSecondary = SoftCream,
    background = Color(0xFF1A1C19),
    surface = Color(0xFF1A1C19),
    onSurface = SoftCream,
    onBackground = SoftCream
)

private val LightColorPalette = lightColorScheme(
    primary = ForestGreen,
    onPrimary = SoftCream,
    secondary = LuxuryGold,
    onSecondary = ForestGreen,
    tertiary = DeepGold,
    background = SoftCream,
    surface = Color.White,
    onSurface = ForestGreen,
    onBackground = ForestGreen,
    error = StatusDisapproved,
    onError = Color.White
)

@Composable
fun JamiaMadinatulIlmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
