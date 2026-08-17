package com.exmaple.taskmanagement.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = KineticPrimary,
    onPrimary = KineticOnPrimary,
    primaryContainer = KineticPrimaryContainer,
    onPrimaryContainer = KineticOnPrimaryContainer,
    secondary = KineticSecondary,
    onSecondary = KineticOnSecondary,
    secondaryContainer = KineticSecondaryContainer,
    onSecondaryContainer = KineticOnSecondaryContainer,
    background = KineticBackground,
    onBackground = KineticOnSurface,
    surface = KineticSurface,
    onSurface = KineticOnSurface,
    surfaceVariant = KineticSurfaceVariant,
    onSurfaceVariant = KineticOnSurfaceVariant,
    outline = KineticOutline,
    outlineVariant = KineticOutlineVariant,
    errorContainer = StatusOverdueBg,
    onErrorContainer = StatusOverdueText
)

private val DarkColorScheme = lightColorScheme( // Consistent clean Enterprise theme
    primary = KineticPrimary,
    onPrimary = KineticOnPrimary,
    primaryContainer = KineticPrimaryContainer,
    onPrimaryContainer = KineticOnPrimaryContainer,
    background = KineticBackground,
    onBackground = KineticOnSurface,
    surface = KineticSurface,
    onSurface = KineticOnSurface,
    surfaceVariant = KineticSurfaceVariant,
    onSurfaceVariant = KineticOnSurfaceVariant,
    outline = KineticOutline
)

val KineticShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun TaskManagementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = KineticShapes,
        typography = Typography,
        content = content
    )
}