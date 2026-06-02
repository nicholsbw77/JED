package com.jed.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JedColorScheme = darkColorScheme(
    primary = OrangeAccent,
    onPrimary = Charcoal,
    secondary = BlueCool,
    onSecondary = OffWhite,
    background = Charcoal,
    onBackground = OffWhite,
    surface = DarkGray,
    onSurface = OffWhite,
    surfaceVariant = CardBackground,
    onSurfaceVariant = LightGray,
    error = RedAlert,
    onError = OffWhite,
    outline = MediumGray
)

@Composable
fun JedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JedColorScheme,
        typography = JedTypography,
        content = content
    )
}
