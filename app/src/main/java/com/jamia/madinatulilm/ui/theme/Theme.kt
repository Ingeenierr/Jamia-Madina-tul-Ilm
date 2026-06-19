package com.jamia.madinatulilm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = Color.Black,
    secondary = AmberAccent,
    onSecondary = Color.Black,
    tertiary = AmberAccentLight,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    onBackground = Color.White
)

private val LightColorPalette = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    secondary = AmberAccent,
    onSecondary = Color.Black,
    tertiary = AmberAccentLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = Color.Black,
    onBackground = Color.Black,
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
