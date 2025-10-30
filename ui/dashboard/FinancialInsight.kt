package com.achievemeaalk.freedjf.ui.dashboard

import androidx.compose.ui.graphics.vector.ImageVector

// Sealed class to define the types of insights
sealed class InsightType {
    data object SpendingTrend : InsightType()
    data object BudgetAchievement : InsightType()
    data object IncomeBoost : InsightType()
    data object OverspendingWarning : InsightType()
}

// Enhanced data class for a single financial insight
data class FinancialInsight(
    val id: String, // A unique, stable ID for this insight
    val type: InsightType,
    val title: String,
    val message: String,
    val icon: ImageVector,
    val priority: Int = 10 // Lower number = higher priority
)
