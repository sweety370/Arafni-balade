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
    primary = Color(0xFF8AB4F8), // Luminous soft cobalt
    onPrimary = Color(0xFF0D2552),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = Color(0xFFF28B82), // Soft coral
    onSecondary = Color(0xFF49120F),
    secondaryContainer = Color(0xFF3C2020),
    onSecondaryContainer = Color(0xFFFCE8E6),
    tertiary = Color(0xFF81C995), // Sage emerald
    onTertiary = Color(0xFF0A3818),
    tertiaryContainer = Color(0xFF1A3D24),
    onTertiaryContainer = Color(0xFFCEEAD6),
    background = Color(0xFF121417), // Premium midnight background
    onBackground = Color(0xFFE3E5E8),
    surface = Color(0xFF1E2126), // Elevated dark card surface
    onSurface = Color(0xFFF0F2F5),
    surfaceVariant = Color(0xFF282C34), // Subtle elevated borders and pills
    onSurfaceVariant = Color(0xFFC4C7C5),
    outline = Color(0xFF3F444E),
    outlineVariant = Color(0xFF2D3139)
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
