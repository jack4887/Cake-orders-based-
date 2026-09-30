package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CleanLightPosColorScheme = lightColorScheme(
    primary = PosAccentBlue,
    onPrimary = OnPosAccentBlue,
    primaryContainer = PosPrimaryContainer,
    onPrimaryContainer = OnPosPrimaryContainer,
    secondary = PosSecondaryEmerald,
    onSecondary = OnPosSecondaryEmerald,
    secondaryContainer = PosSecondaryContainer,
    onSecondaryContainer = OnPosSecondaryContainer,
    tertiary = PosTertiaryAmber,
    onTertiary = OnPosTertiaryAmber,
    tertiaryContainer = PosTertiaryContainer,
    onTertiaryContainer = OnPosTertiaryContainer,
    background = PosLightBackground,
    onBackground = OnPosLightBackground,
    surface = PosPureWhiteSurface,
    onSurface = OnPosPureWhiteSurface,
    surfaceVariant = PosNeutralSurfaceVariant,
    onSurfaceVariant = OnPosNeutralSurfaceVariant,
    outline = PosHairlineBorder,
    error = PosErrorRed,
    onError = OnPosErrorRed,
    errorContainer = PosErrorContainer,
    onErrorContainer = OnPosErrorContainer
)

val CleanPosShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun MyApplicationTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CleanLightPosColorScheme,
        typography = Typography,
        shapes = CleanPosShapes,
        content = content
    )
}
