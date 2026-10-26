package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightIslamicColorScheme = lightColorScheme(
    primary = PetrolPrimary,
    onPrimary = PureWhite,
    primaryContainer = PetrolMedium,
    onPrimaryContainer = PureWhite,
    secondary = EmeraldPrimary,
    onSecondary = PureWhite,
    secondaryContainer = EmeraldSoft,
    onSecondaryContainer = PetrolDark,
    tertiary = OrangeAccent,
    onTertiary = PureWhite,
    tertiaryContainer = OrangeSoftBg,
    onTertiaryContainer = CharcoalText,
    background = IvoryBackground,
    onBackground = CharcoalText,
    surface = PureWhite,
    onSurface = CharcoalText,
    surfaceVariant = SoftGrayBorder,
    onSurfaceVariant = MutedTextLight,
    outline = SoftGrayBorder
)

private val DarkIslamicColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = PureWhite,
    primaryContainer = PetrolPrimary,
    onPrimaryContainer = PureWhite,
    secondary = OrangeAccent,
    onSecondary = IslamicBlack,
    secondaryContainer = PetrolSurfaceDark,
    onSecondaryContainer = PureWhite,
    tertiary = OrangeLight,
    onTertiary = IslamicBlack,
    tertiaryContainer = OrangeDarkBg,
    onTertiaryContainer = OrangeLight,
    background = IslamicBlack,
    onBackground = PureWhite,
    surface = DarkCardSurface,
    onSurface = PureWhite,
    surfaceVariant = PetrolSurfaceDark,
    onSurfaceVariant = MutedTextDark,
    outline = PetrolMedium
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkIslamicColorScheme else LightIslamicColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
