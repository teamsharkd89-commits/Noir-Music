package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores dinámica de NOIR Music adaptada al tema activo.
 */
data class NoirColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceCard: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val button: Color,
    val icons: Color,
    val bottomNav: Color,
    val miniPlayer: Color,
    val progressBar: Color,
    val playerControls: Color,
    val selectedItem: Color,
    val error: Color = Color(0xFFEF4444),
    val success: Color = Color(0xFF10B981),
    val isDark: Boolean = true
)

val defaultNoirColors = AppThemeConfig.NOIR_ORIGINAL.toComposeColors()

val LocalNoirColors = staticCompositionLocalOf { defaultNoirColors }

object NoirTheme {
    val colors: NoirColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNoirColors.current
}

@Composable
fun NoirMusicTheme(
    themeConfig: AppThemeConfig = AppThemeConfig.NOIR_ORIGINAL,
    content: @Composable () -> Unit
) {
    val noirColors = themeConfig.toComposeColors()

    val materialColorScheme = if (themeConfig.isDark) {
        darkColorScheme(
            primary = noirColors.accent,
            onPrimary = noirColors.background,
            primaryContainer = noirColors.surfaceElevated,
            onPrimaryContainer = noirColors.accent,
            secondary = noirColors.button,
            onSecondary = noirColors.background,
            secondaryContainer = noirColors.surfaceCard,
            onSecondaryContainer = noirColors.textPrimary,
            tertiary = noirColors.accent,
            onTertiary = noirColors.background,
            background = noirColors.background,
            onBackground = noirColors.textPrimary,
            surface = noirColors.surface,
            onSurface = noirColors.textPrimary,
            surfaceVariant = noirColors.surfaceElevated,
            onSurfaceVariant = noirColors.textSecondary,
            outline = noirColors.border,
            outlineVariant = noirColors.border,
            error = noirColors.error,
            onError = noirColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = noirColors.accent,
            onPrimary = Color.White,
            primaryContainer = noirColors.surfaceElevated,
            onPrimaryContainer = noirColors.accent,
            secondary = noirColors.button,
            onSecondary = Color.White,
            secondaryContainer = noirColors.surfaceCard,
            onSecondaryContainer = noirColors.textPrimary,
            tertiary = noirColors.accent,
            onTertiary = Color.White,
            background = noirColors.background,
            onBackground = noirColors.textPrimary,
            surface = noirColors.surface,
            onSurface = noirColors.textPrimary,
            surfaceVariant = noirColors.surfaceElevated,
            onSurfaceVariant = noirColors.textSecondary,
            outline = noirColors.border,
            outlineVariant = noirColors.border,
            error = noirColors.error,
            onError = Color.White
        )
    }

    CompositionLocalProvider(LocalNoirColors provides noirColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}
