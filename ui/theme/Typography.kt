package com.achievemeaalk.freedjf.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Complete Material3 Typography system for consistent text styling
val Typography = Typography(
    // Display styles
    displayLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Custom text styles
val Typography.displayMediumBold: TextStyle
    @Composable
    get() = displayMedium.copy(fontWeight = FontWeight.Bold)

val Typography.titleLargeSemiBold: TextStyle
    @Composable
    get() = titleLarge.copy(fontWeight = FontWeight.SemiBold)

val Typography.titleLargeBold: TextStyle
    @Composable
    get() = titleLarge.copy(fontWeight = FontWeight.Bold)

val Typography.titleMediumBold: TextStyle
    @Composable
    get() = titleMedium.copy(fontWeight = FontWeight.Bold)

val Typography.headlineLargeSemiBold: TextStyle
    @Composable
    get() = headlineLarge.copy(fontWeight = FontWeight.SemiBold)

val Typography.headlineMediumMedium: TextStyle
    @Composable
    get() = headlineMedium.copy(fontWeight = FontWeight.Medium)

val Typography.bodyLargeBold: TextStyle
    @Composable
    get() = bodyLarge.copy(fontWeight = FontWeight.Bold)

val Typography.headlineSmallSemiBold: TextStyle
    @Composable
    get() = headlineSmall.copy(fontWeight = FontWeight.SemiBold)

val Typography.headlineSmallBold: TextStyle
    @Composable
    get() = headlineSmall.copy(fontWeight = FontWeight.Bold)

val Typography.labelSmallSemiBold: TextStyle
    @Composable
    get() = labelSmall.copy(fontWeight = FontWeight.SemiBold)

val Typography.bodyLargeSemiBold: TextStyle
    @Composable
    get() = bodyLarge.copy(fontWeight = FontWeight.SemiBold)

val Typography.bodyMediumSemiBold: TextStyle
    @Composable
    get() = bodyMedium.copy(fontWeight = FontWeight.SemiBold)