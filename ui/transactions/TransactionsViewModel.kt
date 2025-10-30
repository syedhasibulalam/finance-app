// main/java/com/achievemeaalk/freedjf/ui/transactions/TransactionsViewModel.kt
package com.achievemeaalk.freedjf.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import com.achievemeaalk.freedjf.domain.repository.AccountsRepository
import com.achievemeaalk.freedjf.domain.repository.CategoriesRepository
import com.achievemeaalk.freedjf.domain.repository.TransactionsRepository
import com.achievemeaalk.freedjf.domain.service.BalanceCalculationService
import com.achievemeaalk.freedjf.ui.ads.AdViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModel @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val accountsRepository: AccountsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val preferencesRepository: PreferencesRepository,
    private val balanceCalculationService: BalanceCalculationService,
    val adViewModel: AdViewModel,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val transactionType: String? = savedStateHandle.get<String>("type")
    private val transactionId: Int? = savedStateHandle.get<Int>("transactionId")

    val transaction: StateFlow<MyFinTransaction?> =
        if (transactionId != null && transactionId != 0) {
            transactionsRepository.getTransactionById(transactionId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                )
        } else {
            MutableStateFlow(null)
        }


    fun addTransaction(transaction: MyFinTransaction) {
        viewModelScope.launch {
            transactionsRepository.insertTransaction(transaction)
            balanceCalculationService.updateBalanceForNewTransaction(transaction)
            preferencesRepository.incrementTransactionsAddedCount()
        }
    }

    fun updateTransaction(transaction: MyFinTransaction) {
        viewModelScope.launch {
            val oldTransaction = transaction.transactionId.let { id ->
                if (id > 0) transactionsRepository.getTransactionById(id).first() else null
            }
            transactionsRepository.updateTransaction(transaction)
            if (oldTransaction != null) {
                balanceCalculationService.updateBalanceForModifiedTransaction(oldTransaction, transaction)
            }
        }
    }

    fun deleteTransaction(transaction: MyFinTransaction) {
        viewModelScope.launch {
            transactionsRepository.deleteTransaction(transaction)
            balanceCalculationService.updateBalanceForDeletedTransaction(transaction)
        }
    }

    private val _accountIdFilter = MutableStateFlow<Int?>(null)
    private val _categoryIdFilter = MutableStateFlow<Int?>(null)
    private val _startDateFilter = MutableStateFlow<Long?>(null)
    private val _endDateFilter = MutableStateFlow<Long?>(null)

    val allTransactions: StateFlow<List<MyFinTransaction>> = combine(
        transactionsRepository.getAllTransactions(),
        _accountIdFilter,
        _categoryIdFilter,
        _startDateFilter,
        _endDateFilter
    ) { transactions, accountId, categoryId, startDate, endDate ->
        transactions.filter { transaction ->
            val accountMatch = accountId == null || transaction.accountId == accountId
            val categoryMatch = categoryId == null || transaction.categoryId == categoryId
            val dateMatch = startDate == null || endDate == null || (transaction.dateTimestamp in startDate..endDate)
            accountMatch && categoryMatch && dateMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    init {
        viewModelScope.launch {
            accountsRepository.getAllAccounts().collect { accounts ->
                _accounts.value = accounts
            }
        }
        viewModelScope.launch {
            categoriesRepository.getAllCategories().collect { categories ->
                _categories.value = categories
            }
        }
    }

    fun applyFilters(accountId: Int? = null, categoryId: String? = null, startDate: Long? = null, endDate: Long? = null) {
        _accountIdFilter.value = accountId
        _categoryIdFilter.value = categoryId?.toIntOrNull()
        _startDateFilter.value = startDate
        _endDateFilter.value = endDate
    }

    fun resetFilters() {
        _accountIdFilter.value = null
        _categoryIdFilter.value = null
        _startDateFilter.value = null
        _endDateFilter.value = null
    }

}