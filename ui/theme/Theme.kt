package com.achievemeaalk.freedjf.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError
)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError
)

val lightExtendedColors = ExtendedColors(
    success = IncomeColor,
    onSuccess = OnIncomeColor,
    warning = WarningColor,
    onWarning = Color.White,
    neutral = ChartGray,
    onNeutral = Color.Black
)

val darkExtendedColors = ExtendedColors(
    success = IncomeColor,
    onSuccess = OnIncomeColor,
    warning = WarningColor,
    onWarning = Color.White,
    neutral = ChartGray,
    onNeutral = Color.Black
)

val LocalExtendedColors = staticCompositionLocalOf {
    lightExtendedColors
}


// Consistent shapes for the design system
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimensions.cornerRadiusSmall),
    small = RoundedCornerShape(Dimensions.cornerRadiusMedium),
    medium = RoundedCornerShape(Dimensions.cornerRadiusLarge),
    large = RoundedCornerShape(Dimensions.cornerRadiusExtraLarge),
    extraLarge = RoundedCornerShape(Dimensions.cornerRadiusExtraLarge)
)

object AppTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}

@Composable
fun MonefyTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val extendedColors = if (useDarkTheme) {
        darkExtendedColors
    } else {
        lightExtendedColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}