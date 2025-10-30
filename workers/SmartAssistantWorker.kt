package com.achievemeaalk.freedjf.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import com.achievemeaalk.freedjf.domain.repository.BudgetsRepository
import com.achievemeaalk.freedjf.domain.repository.TransactionsRepository
import com.achievemeaalk.freedjf.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@HiltWorker
class SmartAssistantWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val budgetsRepository: BudgetsRepository,
    private val transactionsRepository: TransactionsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val currencyCode = preferencesRepository.currency.first()
            val today = LocalDate.now()
            val currentMonth = YearMonth.from(today)
            val budget = budgetsRepository.getBudgetByMonthAndYear(currentMonth.monthValue, currentMonth.year).first()

            if (budget!= null) {
                val budgetCategories = budgetsRepository.getBudgetCategoriesWithCategory(budget.budgetId).first()
                val startOfMonth = currentMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val monthlyTransactions = transactionsRepository.getTransactionsForDateRange(startOfMonth, endOfMonth).first()
                    .filter { it.type == TransactionType.EXPENSE }

                val daysInMonth = currentMonth.lengthOfMonth()
                val dayOfMonth = today.dayOfMonth
                val timePercentage = (dayOfMonth.toDouble() / daysInMonth.toDouble()) * 100

                budgetCategories.forEach { budgetedCategory ->
                    val spentAmount = monthlyTransactions
                        .filter { it.categoryId == budgetedCategory.category.categoryId }
                        .sumOf { it.amount }
                    val plannedAmount = budgetedCategory.budgetCategory.plannedAmount

                    if (plannedAmount > 0) {
                        val spentPercentage = (spentAmount / plannedAmount) * 100

                        // Smart Alert Logic: Notify if spending pace is significantly ahead of time pace
                        // e.g., Spent 75% of budget but only 50% of the month has passed.
                        if (spentPercentage >= 75 && spentPercentage > timePercentage + 20) {
                            val daysLeft = daysInMonth - dayOfMonth
                            notificationHelper.showSmartBudgetAlert(
                                categoryName = budgetedCategory.category.name,
                                spentAmount = spentAmount,
                                plannedAmount = plannedAmount,
                                daysLeftInMonth = daysLeft,
                                currencyCode = currencyCode
                            )
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "smart_assistant_work"

        fun enqueue(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<SmartAssistantWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
