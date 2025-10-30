package com.achievemeaalk.freedjf.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.data.model.CategoryType
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.domain.repository.AccountsRepository
import com.achievemeaalk.freedjf.domain.repository.CategoriesRepository
import com.achievemeaalk.freedjf.domain.repository.TransactionsRepository
import com.achievemeaalk.freedjf.ui.ads.AdViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle

data class CategoriesScreenState(
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val expenseSoFar: Double = 0.0,
    val incomeSoFar: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository,
    private val transactionsRepository: TransactionsRepository,
    private val accountsRepository: AccountsRepository,
    private val adViewModel: AdViewModel, // Add AdViewModel
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val category = categoriesRepository.getCategory(savedStateHandle.get<String>("categoryId")?.toIntOrNull() ?: 0)

    val categoriesState: StateFlow<CategoriesScreenState> = combine(
        categoriesRepository.getAllCategories(),
        transactionsRepository.getTransactions(""),
        accountsRepository.getAllAccounts()
    ) { categories, transactions, accounts ->
        val expenseCategories = categories.filter { it.type == CategoryType.EXPENSE }
        val incomeCategories = categories.filter { it.type == CategoryType.INCOME }

        val expenseSoFar = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        val incomeSoFar = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        CategoriesScreenState(
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            accounts = accounts,
            expenseSoFar = expenseSoFar,
            incomeSoFar = incomeSoFar,
            isLoading = false
        )
    }.stateIn(
            scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoriesScreenState()
    )

    fun addCategory(category: Category, context: android.content.Context, onCategoryAdded: (String) -> Unit) = viewModelScope.launch { // Pass context
        val newCategoryId = categoriesRepository.insertCategory(category)
        adViewModel.showTransactionCompletionAd(context) // Show ad
        onCategoryAdded(newCategoryId.toString())
    }

    fun updateCategory(category: Category, onCategoryUpdated: (String) -> Unit) = viewModelScope.launch {
        categoriesRepository.updateCategory(category)
        onCategoryUpdated(category.categoryId.toString())
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        categoriesRepository.deleteCategory(category)
    }
} 