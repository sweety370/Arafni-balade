package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GoogleMapsBlue,
    onPrimary = Color.White,
    primaryContainer = GoogleMapsBlueLight,
    onPrimaryContainer = GoogleMapsBlueDark,
    secondary = GoogleMapsPinRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE8E6),
    onSecondaryContainer = Color(0xFFC5221F),
    tertiary = GoogleMapsPinGreen,
    background = GoogleMapsLand,
    onBackground = GoogleMapsTextPrimary,
    surface = GoogleMapsSurface,
    onSurface = GoogleMapsTextPrimary,
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = GoogleMapsTextSecondary,
    outline = Color(0xFFDADCE0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF174EA6),
    primaryContainer = Color(0xFF1A73E8),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = Color(0xFFF28B82),
    onSecondary = Color(0xFF601410),
    background = GoogleMapsLandDark,
    onBackground = Color(0xFFE8EAED),
    surface = GoogleMapsSurfaceDark,
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF303134),
    onSurfaceVariant = Color(0xFFBDC1C6),
    outline = Color(0xFF5F6368)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Clean default Google Maps light mode as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
