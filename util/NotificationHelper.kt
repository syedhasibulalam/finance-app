package com.achievemeaalk.freedjf.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.RecurringTransaction
import java.text.NumberFormat
import java.util.concurrent.TimeUnit
import com.achievemeaalk.freedjf.di.LocaleAwareContext
import javax.inject.Inject
import javax.inject.Singleton
import com.achievemeaalk.freedjf.util.formatCurrency

@Singleton
class NotificationHelper @Inject constructor(
    @LocaleAwareContext private val localeAwareContext: Context,
    private val preferencesRepository: com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
) {

    companion object {
        private const val BUDGET_CHANNEL_ID = "budget_reminders"
        private const val RECURRING_CHANNEL_ID = "recurring_reminders"
        private const val SMART_ASSISTANT_CHANNEL_ID = "smart_assistant_reminders"
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = localeAwareContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Budget reminder channel
            val budgetChannel = NotificationChannel(
                BUDGET_CHANNEL_ID,
                localeAwareContext.getString(R.string.budget_reminders_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = localeAwareContext.getString(R.string.budget_reminders_channel_description)
            }

            // Recurring transaction reminder channel
            val recurringChannel = NotificationChannel(
                RECURRING_CHANNEL_ID,
                localeAwareContext.getString(R.string.recurring_reminders_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = localeAwareContext.getString(R.string.recurring_reminders_channel_description)
            }

            // Smart assistant channel
            val smartAssistantChannel = NotificationChannel(
                SMART_ASSISTANT_CHANNEL_ID,
                localeAwareContext.getString(R.string.smart_assistant_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = localeAwareContext.getString(R.string.smart_assistant_channel_description)
            }

            notificationManager.createNotificationChannels(listOf(budgetChannel, recurringChannel, smartAssistantChannel))
        }
    }

    // Legacy function for backwards compatibility
    fun createNotificationChannel(context: Context) {
        createNotificationChannels()
    }

    fun showBudgetReminderNotification(categoryName: String, remainingAmount: String, spentPercentage: Int) {
        val notificationManager = localeAwareContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = localeAwareContext.getString(R.string.budget_alert_title, categoryName)
        val message = when {
            spentPercentage >= 100 -> localeAwareContext.getString(R.string.budget_exceeded_message)
            spentPercentage >= 90 -> localeAwareContext.getString(R.string.budget_remaining_message, remainingAmount, 100 - spentPercentage)
            else -> localeAwareContext.getString(R.string.budget_remaining_message_simple, remainingAmount)
        }

        val notification = NotificationCompat.Builder(localeAwareContext, BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(categoryName.hashCode(), notification)
    }

    fun showRecurringTransactionReminder(transactionName: String, message: String, transactionId: Int) {
        val notificationManager = localeAwareContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = localeAwareContext.getString(R.string.payment_reminder_title)
        val fullMessage = "$transactionName - $message"

        val notification = NotificationCompat.Builder(localeAwareContext, RECURRING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(fullMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(transactionId + 10000, notification) // Add offset to avoid conflicts
    }

    fun showSmartBillReminder(bill: RecurringTransaction, account: Account) {
        val notificationManager = localeAwareContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currencyCode = preferencesRepository.currency.value

        val title: String
        val message: String
        val priority: Int

        val daysUntilDue = TimeUnit.MILLISECONDS.toDays(bill.nextDueDate - System.currentTimeMillis()).toInt()
        val dueDateDescription = when (daysUntilDue) {
            0 -> localeAwareContext.getString(R.string.due_today)
            1 -> localeAwareContext.getString(R.string.due_tomorrow)
            else -> localeAwareContext.getString(R.string.due_in_days, daysUntilDue)
        }

        if (account.balance < bill.amount) {
            title = localeAwareContext.getString(R.string.low_balance_alert_title)
            message = localeAwareContext.getString(
                R.string.low_balance_message,
                bill.name,
                formatCurrency(bill.amount, currencyCode),
                dueDateDescription,
                account.name,
                formatCurrency(account.balance, currencyCode)
            )
            priority = NotificationCompat.PRIORITY_HIGH
        } else {
            title = localeAwareContext.getString(R.string.payment_reminder_title)
            message = localeAwareContext.getString(
                R.string.payment_reminder_message,
                bill.name,
                formatCurrency(bill.amount, currencyCode),
                dueDateDescription
            )
            priority = NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(localeAwareContext, RECURRING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(bill.id + 20000, notification) // Use a different offset
    }

    fun showSmartBudgetAlert(
        categoryName: String,
        spentAmount: Double,
        plannedAmount: Double,
        daysLeftInMonth: Int,
        currencyCode: String
    ) {
        val notificationManager = localeAwareContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = localeAwareContext.getString(R.string.spending_alert_title, categoryName)
        val message = localeAwareContext.getString(R.string.smart_budget_alert_message, formatCurrency(spentAmount, currencyCode), formatCurrency(plannedAmount, currencyCode), daysLeftInMonth)

        val notification = NotificationCompat.Builder(localeAwareContext, SMART_ASSISTANT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()

        // Use a unique ID for each category's smart alert to avoid overwriting
        notificationManager.notify(categoryName.hashCode() + 30000, notification)
    }

    fun showSmartAssistantNotification(title: String, message: String) {
        val notificationManager = localeAwareContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(localeAwareContext, SMART_ASSISTANT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }
}
