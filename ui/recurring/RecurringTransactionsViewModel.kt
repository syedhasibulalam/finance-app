
package com.achievemeaalk.freedjf.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.RecurringTransaction
import com.achievemeaalk.freedjf.data.model.RecurrenceFrequency
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import com.achievemeaalk.freedjf.domain.repository.AccountsRepository
import com.achievemeaalk.freedjf.domain.repository.RecurringTransactionRepository
import com.achievemeaalk.freedjf.ui.ads.AdViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringTransactionsState(
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val subscriptions: List<RecurringTransaction> = emptyList(),
    val bills: List<RecurringTransaction> = emptyList(),
    val dueSoon: List<RecurringTransaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0
)

@HiltViewModel
class RecurringTransactionsViewModel @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val accountsRepository: AccountsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val adViewModel: AdViewModel
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)

    val state: StateFlow<RecurringTransactionsState> = combine(
        recurringTransactionRepository.getAllRecurringTransactions(),
        recurringTransactionRepository.getSubscriptions(),
        recurringTransactionRepository.getBills(),
        preferencesRepository.dueSoonDays,
        accountsRepository.getAllAccounts()
    ) { all, subscriptions, bills, dueSoonDays, accounts ->
        val dueSoonTimestamp = System.currentTimeMillis() + (dueSoonDays * 24 * 60 * 60 * 1000)
        val dueSoon = all.filter { it.nextDueDate <= dueSoonTimestamp }

        RecurringTransactionsState(
            recurringTransactions = all,
            subscriptions = subscriptions,
            bills = bills,
            dueSoon = dueSoon,
            accounts = accounts,
            isLoading = false,
            selectedTab = _selectedTab.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecurringTransactionsState()
    )

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun addOrUpdateRecurringTransaction(
        id: Int = 0,
        name: String,
        amount: Double,
        accountId: Int,
        categoryId: Int,
        nextDueDate: Long,
        frequency: RecurrenceFrequency,
        isSubscription: Boolean,
        transactionType: com.achievemeaalk.freedjf.data.model.TransactionType,
        context: android.content.Context
    ) {
        viewModelScope.launch {
            val recurringTransaction = RecurringTransaction(
                id = id,
                name = name,
                amount = amount,
                accountId = accountId,
                categoryId = categoryId,
                nextDueDate = nextDueDate,
                frequency = frequency,
                isSubscription = isSubscription,
                transactionType = transactionType
            )
            if (id == 0) {
                recurringTransactionRepository.insertRecurringTransaction(recurringTransaction)
            } else {
                recurringTransactionRepository.updateRecurringTransaction(recurringTransaction)
            }
            adViewModel.showTransactionCompletionAd(context)
        }
    }


    fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        viewModelScope.launch {
            recurringTransactionRepository.deleteRecurringTransaction(recurringTransaction)
        }
    }

    fun toggleTransactionActiveState(transaction: RecurringTransaction) {
        viewModelScope.launch {
            val updatedTransaction = transaction.copy(isActive = !transaction.isActive)
            recurringTransactionRepository.updateRecurringTransaction(updatedTransaction)
        }
    }

    fun markAsProcessed(recurringTransaction: RecurringTransaction) {
        viewModelScope.launch {
            recurringTransactionRepository.markAsProcessed(recurringTransaction)
        }
    }

    fun getDaysUntilDue(nextDueDate: Long): Int {
        val currentTime = System.currentTimeMillis()
        val timeDifference = nextDueDate - currentTime
        return (timeDifference / (24 * 60 * 60 * 1000)).toInt()
    }

    fun isOverdue(nextDueDate: Long): Boolean {
        return nextDueDate < System.currentTimeMillis()
    }
}