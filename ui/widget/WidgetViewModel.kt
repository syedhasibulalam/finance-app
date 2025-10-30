package com.achievemeaalk.freedjf.ui.widget

import android.util.Log
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import com.achievemeaalk.freedjf.domain.repository.RecurringTransactionRepository
import com.achievemeaalk.freedjf.util.formatCurrency
import com.achievemeaalk.freedjf.di.ApplicationScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationScope private val coroutineScope: CoroutineScope
) {
    private val TAG = "WIDGET_DEBUG"

    init {
        Log.d(TAG, "WidgetViewModel initialized")
    }

    val state: StateFlow<WidgetState> = combine(
        recurringTransactionRepository.getActiveRecurringTransactions(),
        preferencesRepository.currency
    ) { bills, currencyCode ->
        val isPremium = true
        Log.d(TAG, "Combining data. Found ${bills.size} bills. Currency: $currencyCode. Is premium: $isPremium")
        WidgetState(
            bills = bills.take(3).map {
                val isOverdue = it.nextDueDate < System.currentTimeMillis()

                // Smart date formatting logic
                val dueDateString = if (isOverdue) {
                    "Overdue"
                } else {
                    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                    sdf.format(Date(it.nextDueDate))
                }

                val bill = Bill(
                    id = it.id,
                    name = it.name,
                    amount = formatCurrency(it.amount, currencyCode),
                    dueDate = dueDateString, // Use the newly formatted date string
                    isOverdue = isOverdue
                )
                Log.d(TAG, "Mapped bill: ${bill.name}, Amount: ${bill.amount}, Due: ${bill.dueDate}")
                bill
            },
            isPremium = isPremium
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Lazily,
        initialValue = WidgetState(emptyList())
    )

    suspend fun markBillAsPaid(transactionId: Int) {
        val transaction = recurringTransactionRepository.getRecurringTransactionById(transactionId)
        if (transaction != null) {
            recurringTransactionRepository.markAsProcessed(transaction)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Provider {
        fun widgetViewModel(): WidgetViewModel
    }
}
