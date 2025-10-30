package com.achievemeaalk.freedjf.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils

// --- NEW Dark Theme Colors ---
val DarkPrimary = Color(0xFF2ECC71) // Emerald Green
val DarkOnPrimary = Color.White
val DarkBackground = Color(0xFF1C1C1E) // Almost Black
val DarkSurface = Color(0xFF2C2C2E) // Dark Gray
val DarkOnSurface = Color(0xFFEAEAEB) // Light Gray
val DarkOnSurfaceVariant = Color(0xFFC7C7CC) // Medium Gray - Adjusted for contrast
val DarkError = Color(0xFFFF453A) // Softer Red

// --- NEW Light Theme Colors ---
val LightPrimary = Color(0xFF27AE60) // Emerald Green
val LightOnPrimary = Color.White
val LightBackground = Color(0xFFF2F2F7) // Very Light Gray
val LightSurface = Color.White
val LightOnSurface = Color(0xFF1C1C1E) // Almost Black
val LightOnSurfaceVariant = Color(0xFF5A5A5F) // Gray - Adjusted for contrast
val LightError = Color(0xFFFF3B30) // Bright Red

// --- Updated Semantic Colors ---
val IncomeColor = Color(0xFF34C759) // Brighter Green
val OnIncomeColor = Color(0xFF1E8449)
val ExpenseColor = Color(0xFFFF3B30) // Modern Red
val OnExpenseColor = Color(0xFFC0392B)
val TransferColor = Color(0xFF007AFF) // Professional Blue
val BudgetSpent = Color(0xFF007AFF) // Using the new transfer color for consistency
val BudgetRemaining = Color(0xFF34C759) // Using the new income color
val InactiveIndicator = Color(0x33000000) // Black with 20% opacity
val InactiveOnboardingIndicator = Color(0x4DEAEAEB)
val DisabledBorder = Color(0x4D34C759) // Primary color with 30% opacity
val WarningColor = Color(0xFFFF9F0A) // Orange
val ProgressBarError = Color(0x4DFF3B30) // Error color with 30% opacity
val SurfaceVariant70 = Color(0xB32C2C2E) // Surface variant with 70% opacity
val OnSurface70 = Color(0xB3EAEAEB) // OnSurface with 70% opacity
val OnSurface80 = Color(0xCCEAEAEB) // OnSurface with 80% opacity
val Success = Color(0xFF34C759)
val OnSuccess = Color.White
// --- Chart & Category Colors (Vibrant and Modern) ---
val ChartGray = Color(0xFFE5E5EA)
val ChartColors = listOf(
    Color(0xFF0A84FF), // Blue
    Color(0xFF34C759), // Green
    Color(0xFFFF9F0A), // Orange
    Color(0xFFFF453A), // Red
    Color(0xFFBF5AF2), // Purple
    Color(0xFF64D2FF), // Teal
    Color(0xFFFFD60A), // Yellow
    Color(0xFFAC8E68), // Brown
    Color(0xFF5E5CE6), // Indigo
    Color(0xFFFF375F), // Pink
    Color(0xFF40C8E0), // Cyan
    Color(0xFF787AE8)  // Lavender
)
val CategoryColors = ChartColors

fun Color.isColorDark(): Boolean {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.hashCode(), hsl)
    return hsl[2] < 0.5f
}
