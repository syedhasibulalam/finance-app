package com.achievemeaalk.freedjf.domain.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import com.achievemeaalk.freedjf.data.model.CategoryType
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.ui.dashboard.FinancialInsight
import com.achievemeaalk.freedjf.ui.dashboard.InsightType
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightsRepository @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val budgetsRepository: BudgetsRepository
) {
    suspend fun generateInsights(): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()
        val transactions = transactionsRepository.getTransactions("").first()
        val categories = categoriesRepository.getAllCategories().first()
        val thisMonth = YearMonth.now()
        val budget = budgetsRepository.getBudgetByMonthAndYear(thisMonth.monthValue, thisMonth.year).first()
        val budgetDetails = budget?.let { budgetsRepository.getBudgetCategoriesWithCategory(it.budgetId).first() }?: emptyList()

        // --- Logic moved from SmartAssistantWorker ---
        val today = LocalDate.now()
        val daysInMonth = today.lengthOfMonth()
        val dayOfMonth = today.dayOfMonth
        val timePercentage = (dayOfMonth.toFloat() / daysInMonth.toFloat()) * 100

        budgetDetails.forEach { detail ->
            val plannedAmount = detail.budgetCategory.plannedAmount
            if (plannedAmount > 0) {
                val spentAmount = transactions.filter {
                    it.categoryId == detail.category.categoryId &&
                    it.type == TransactionType.EXPENSE &&
                    it.dateTimestamp.toYearMonth() == thisMonth
                }.sumOf { it.amount }

                val spentPercentage = (spentAmount / plannedAmount) * 100

                if (spentPercentage >= 75 && spentPercentage > timePercentage + 20) {
                    insights.add(
                        FinancialInsight(
                            id = "overspending_${detail.category.categoryId}",
                            type = InsightType.OverspendingWarning,
                            title = "Spending Alert: ${detail.category.name}",
                            message = "You've spent ${spentPercentage.toInt()}% of your budget with ${daysInMonth - dayOfMonth} days left this month.",
                            icon = Icons.Default.Warning,
                            priority = 1 // High priority
                        )
                    )
                }
            }
        }

        // --- Logic moved from DashboardViewModel ---
        val lastMonth = thisMonth.minusMonths(1)

        // Insight: Spending Trend
        val spendingChanges = categories
           .filter { it.type == CategoryType.EXPENSE }
           .mapNotNull { category ->
                val thisMonthSpending = transactions.filter { it.categoryId == category.categoryId && it.dateTimestamp.toYearMonth() == thisMonth }.sumOf { it.amount }
                val lastMonthSpending = transactions.filter { it.categoryId == category.categoryId && it.dateTimestamp.toYearMonth() == lastMonth }.sumOf { it.amount }
                if (lastMonthSpending > 0 && thisMonthSpending > lastMonthSpending * 1.3) { // 30% increase
                    val percentageIncrease = ((thisMonthSpending - lastMonthSpending) / lastMonthSpending) * 100
                    Triple(category.name, percentageIncrease, thisMonthSpending)
                } else null
            }.maxByOrNull { it.second }

        spendingChanges?.let { (categoryName, percentage, _) ->
            insights.add(
                FinancialInsight(
                    id = "spending_trend_$categoryName",
                    type = InsightType.SpendingTrend,
                    title = "Spending Alert",
                    message = "Your spending on '$categoryName' is up ${percentage.toInt()}% this month.",
                    icon = Icons.Default.ArrowUpward,
                    priority = 5
                )
            )
        }

        // Insight 2: Great Budgeting! (dynamic)
        budgetDetails.forEach { budgetDetail ->
            val category = budgetDetail.category
            val budgetAmount = budgetDetail.budgetCategory.plannedAmount

            if (budgetAmount > 0) {
                val lastTwoMonthsSpending = transactions.filter {
                    it.categoryId == category.categoryId &&
                        it.dateTimestamp.toYearMonth().let { month ->
                            month == thisMonth || month == lastMonth
                        }
                }.groupBy {
                    it.dateTimestamp.toYearMonth()
                }

                val thisMonthSpent = lastTwoMonthsSpending[thisMonth]?.sumOf { it.amount } ?: 0.0
                val lastMonthSpent = lastTwoMonthsSpending[lastMonth]?.sumOf { it.amount } ?: 0.0

                if (lastTwoMonthsSpending.size == 2 && thisMonthSpent < budgetAmount && lastMonthSpent < budgetAmount) {
                    insights.add(
                        FinancialInsight(
                            id = "budget_achievement_${category.categoryId}",
                            type = InsightType.BudgetAchievement,
                            title = "Great Budgeting!",
                            message = "You've successfully stayed under your '${category.name}' budget for two consecutive months. Keep it up!",
                            icon = Icons.Default.CheckCircle,
                            priority = 8
                        )
                    )
                    return@forEach // Add only one such insight for now
                }
            }
        }


        // Insight 3: Unusually high income
        val thisMonthIncome = transactions.filter {
            it.type == TransactionType.INCOME && it.dateTimestamp.toYearMonth() == thisMonth
        }.sumOf { it.amount }

        val lastMonthIncome = transactions.filter {
            it.type == TransactionType.INCOME && it.dateTimestamp.toYearMonth() == lastMonth
        }.sumOf { it.amount }

        if (thisMonthIncome > lastMonthIncome * 1.5 && lastMonthIncome > 0) {
            insights.add(
                FinancialInsight(
                    id = "income_boost",
                    type = InsightType.IncomeBoost,
                    title = "Income Boost!",
                    message = "Your income is significantly higher this month. Great job!",
                    icon = Icons.Default.TrendingUp,
                    priority = 3
                )
            )
        }

        return insights.sortedBy { it.priority } // Return insights sorted by priority
    }

    private fun Long.toYearMonth(): YearMonth {
        val cal = Calendar.getInstance().apply { timeInMillis = this@toYearMonth }
        return YearMonth.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }
}