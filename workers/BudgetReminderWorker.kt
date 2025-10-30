package com.achievemeaalk.freedjf.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.achievemeaalk.freedjf.domain.repository.BudgetsRepository
import com.achievemeaalk.freedjf.domain.repository.TransactionsRepository
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import com.achievemeaalk.freedjf.util.NotificationHelper
import com.achievemeaalk.freedjf.util.formatCurrency
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.time.YearMonth
import java.time.ZoneId

@HiltWorker
class BudgetReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val budgetsRepository: BudgetsRepository,
    private val transactionsRepository: TransactionsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
          applicationContext
            val date = YearMonth.now()
            val currency = preferencesRepository.currency.first()
            val budget = budgetsRepository.getBudgetByMonthAndYear(date.monthValue, date.year).first()

            if (budget != null) {
                val budgetCategoriesWithCategory = budgetsRepository.getBudgetCategoriesWithCategory(budget.budgetId).first()
                val startOfMonth = date.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = date.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val monthlyTransactions = transactionsRepository.getTransactionsForDateRange(startOfMonth, endOfMonth).first()

                budgetCategoriesWithCategory.forEach { detail ->
                    if (detail.budgetCategory.reminderEnabled) {
                        val spent = monthlyTransactions
                            .filter { it.categoryId == detail.category.categoryId }
                            .sumOf { it.amount }
                        val remaining = detail.budgetCategory.plannedAmount - spent
                        val spentPercentage = ((spent / detail.budgetCategory.plannedAmount) * 100).toInt()

                        // Notify if spending is between 80% and 100%+ of the budget
                        if (spentPercentage >= 80) {
                            notificationHelper.showBudgetReminderNotification(
                                detail.category.name,
                                formatCurrency(remaining.coerceAtLeast(0.0), currency),
                                spentPercentage
                            )
                        }
                    }
                }
            }

            Result.success()
        } catch (e: IOException) {
            Result.retry()
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 