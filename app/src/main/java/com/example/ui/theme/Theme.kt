package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CyberColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = CyberCyan.copy(alpha = 0.1f),
    onPrimaryContainer = CyberCyan,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkBackground,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSlate,
    onSurfaceVariant = TextSecondary,
    secondary = CyberCyan,
    onSecondary = Color.Black,
    error = CyberError,
    onError = Color.Black,
    outline = BorderWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic for forced cyber look
    content: @Composable () -> Unit,
) {
    val colorScheme = CyberColorScheme // Force the cyber theme for futuristic look

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
