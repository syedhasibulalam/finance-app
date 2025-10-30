package com.achievemeaalk.freedjf

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import com.google.android.gms.ads.MobileAds
import com.achievemeaalk.freedjf.util.NotificationHelper
import com.achievemeaalk.freedjf.workers.BudgetReminderWorker
import com.achievemeaalk.freedjf.workers.RecurringTransactionReminderWorker
import com.achievemeaalk.freedjf.workers.SmartAssistantWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MonefyApplication : Application(), Configuration.Provider {
    companion object {
        lateinit var appContext: Context
            private set
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var preferencesRepository: PreferencesRepository



    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Initialize RevenueCat

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(this)

        notificationHelper.createNotificationChannels()
        scheduleBudgetReminderWorker()
        scheduleRecurringTransactionReminderWorker()
        scheduleSmartAssistantWorker()

        preferencesRepository.incrementAppLaunchCount()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun scheduleBudgetReminderWorker() {
        val workRequest = PeriodicWorkRequestBuilder<BudgetReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "budget_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleRecurringTransactionReminderWorker() {
        val workRequest = PeriodicWorkRequestBuilder<RecurringTransactionReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_transaction_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleSmartAssistantWorker() {
        val workRequest = PeriodicWorkRequestBuilder<SmartAssistantWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "smart_assistant_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
