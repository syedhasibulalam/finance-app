package com.achievemeaalk.freedjf.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.achievemeaalk.freedjf.domain.repository.AccountsRepository
import com.achievemeaalk.freedjf.domain.repository.RecurringTransactionRepository
import com.achievemeaalk.freedjf.util.NotificationHelper
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringTransactionReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val accountsRepository: AccountsRepository,
    private val notificationHelper: NotificationHelper,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
          applicationContext
            val currentTime = System.currentTimeMillis()
            val dueSoonDays = preferencesRepository.dueSoonDays.first()

            // 1. Handle Overdue Transactions
            val overdueTransactions = recurringTransactionRepository
                .getActiveRecurringTransactionsDueBefore(currentTime)
                .first()

            overdueTransactions.forEach { transaction ->
                // Mark as processed (which now creates a transaction record)
                recurringTransactionRepository.markAsProcessed(transaction)

                // Notify user of the automatic payment
                notificationHelper.showRecurringTransactionReminder(
                    transaction.name,
                    "This payment was overdue and has been automatically processed.",
                    transaction.id
                )
            }

            // 2. Handle Reminders for Upcoming Transactions (that are not overdue)
            val reminderStartDate = currentTime + 1 
            val reminderEndDate = currentTime + (dueSoonDays * 24 * 60 * 60 * 1000)
            val upcomingTransactions = recurringTransactionRepository
                .getActiveRecurringTransactionsDueBetween(reminderStartDate, reminderEndDate)
                .first()

            upcomingTransactions.forEach { transaction ->
                val account = accountsRepository.getAccount(transaction.accountId).first()
                if (account != null) {
                    notificationHelper.showSmartBillReminder(
                        transaction,
                        account
                    )
                }
            }

            Result.success()
        } catch (e: IOException) {
            // For transient issues like network problems, retry the work.
            Result.retry()
        } catch (e: Exception) {
            // For other errors, such as bugs or data corruption, fail the work permanently.
            // Consider logging the exception to a crash reporting service.
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "recurring_transaction_reminder_work"

        fun enqueue(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<RecurringTransactionReminderWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .setRequiresCharging(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}