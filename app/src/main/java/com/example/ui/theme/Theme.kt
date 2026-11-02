package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MusikkuColorScheme = darkColorScheme(
    primary = NeonLime,
    onPrimary = Color(0xFF0C1001),
    primaryContainer = Color(0xFF2E3B05),
    onPrimaryContainer = NeonLime,
    secondary = ElectricViolet,
    onSecondary = Color(0xFF1B0836),
    secondaryContainer = Color(0xFF33205A),
    onSecondaryContainer = SoftLavender,
    tertiary = RadiantPink,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = DividerColor
)

@Composable
fun MusikkuTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MusikkuColorScheme,
        typography = Typography,
        content = content
    )
}
