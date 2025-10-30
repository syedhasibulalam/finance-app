package com.achievemeaalk.freedjf.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achievemeaalk.freedjf.data.model.Budget
import com.achievemeaalk.freedjf.data.model.BudgetCategory
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.data.model.CategoryType
import com.achievemeaalk.freedjf.domain.repository.BudgetsRepository
import com.achievemeaalk.freedjf.domain.repository.CategoriesRepository
import com.achievemeaalk.freedjf.domain.repository.RecurringTransactionRepository
import com.achievemeaalk.freedjf.domain.repository.TransactionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class BudgetedCategory(
    val category: Category,
    val budgetCategory: BudgetCategory,
    val spentAmount: Double
) {
    val remainingAmount: Double = budgetCategory.plannedAmount - spentAmount
}

data class BudgetScreenState(
    val selectedDate: YearMonth = YearMonth.now(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val budgetedCategories: List<BudgetedCategory> = emptyList(),
    val unbudgetedCategories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val budgetId: Int? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetsRepository: BudgetsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val transactionsRepository: TransactionsRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(YearMonth.now())
    val selectedDate: StateFlow<YearMonth> = _selectedDate.asStateFlow()

    private val _recurringTotalForCategory = MutableStateFlow(0.0)
    val recurringTotalForCategory: StateFlow<Double> = _recurringTotalForCategory.asStateFlow()

    val budgetState: StateFlow<BudgetScreenState> = _selectedDate.flatMapLatest { date ->
        // Get or create budget for the month
        budgetsRepository.getBudgetByMonthAndYear(date.monthValue, date.year).flatMapLatest { budget ->
            val budgetId = budget?.budgetId
            val budgetFlow = if (budget == null) {
                flowOf(null)
            } else {
                budgetsRepository.getBudgetCategoriesWithCategory(budgetId!!)
            }

            combine(
                budgetFlow,
                categoriesRepository.getAllCategories(),
                transactionsRepository.getTransactions("")
            ) { budgetCategoriesWithCategory, allCategories, allTransactions ->

                val startOfMonth = date.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = date.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val monthlyTransactions = allTransactions.filter { it.dateTimestamp in startOfMonth..endOfMonth }
                val totalSpent = monthlyTransactions.filter { it.type == com.achievemeaalk.freedjf.data.model.TransactionType.EXPENSE }.sumOf { it.amount }

                val budgetedCategoryDetails = budgetCategoriesWithCategory?: emptyList()
                val budgetedCategoryIds = budgetedCategoryDetails.map { it.category.categoryId }.toSet()

                val budgetedItems = budgetedCategoryDetails.map { detail ->
                    val spent = monthlyTransactions
                       .filter { it.categoryId == detail.category.categoryId }
                       .sumOf { it.amount }
                    BudgetedCategory(
                        category = detail.category,
                        budgetCategory = detail.budgetCategory,
                        spentAmount = spent
                    )
                }

                val totalBudget = budgetedItems.sumOf { it.budgetCategory.plannedAmount }

                val unbudgetedItems = allCategories
                   .filter { it.type == CategoryType.EXPENSE && it.categoryId!in budgetedCategoryIds }

                BudgetScreenState(
                    selectedDate = date,
                    totalBudget = totalBudget,
                    totalSpent = totalSpent,
                    budgetedCategories = budgetedItems,
                    unbudgetedCategories = unbudgetedItems,
                    isLoading = false,
                    budgetId = budgetId
                )
            }
        }
    }.stateIn(
                scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetScreenState()
            )

    fun changeMonth(amount: Int) {
        _selectedDate.value = _selectedDate.value.plusMonths(amount.toLong())
    }

    fun setBudgetForCategory(categoryId: Int, amount: Double, reminderEnabled: Boolean) {
        viewModelScope.launch {
            val date = _selectedDate.value
            var budget = budgetsRepository.getBudgetByMonthAndYear(date.monthValue, date.year).first()
            if (budget == null) {
                val newBudgetId = budgetsRepository.insertBudget(
                    Budget(month = date.monthValue, year = date.year, observations = "")
                )
                budget = Budget(budgetId = newBudgetId.toInt(), month = date.monthValue, year = date.year, observations = "")
    }

            val budgetCategory = BudgetCategory(
                budgetId = budget.budgetId,
                categoryId = categoryId,
                plannedAmount = amount,
                reminderEnabled = reminderEnabled
            )
            budgetsRepository.insertBudgetCategory(budgetCategory)
        }
    }

    fun updateBudgetForCategory(budgetCategory: BudgetCategory, newAmount: Double, reminderEnabled: Boolean) {
        viewModelScope.launch {
            budgetsRepository.updateBudgetCategory(budgetCategory.copy(plannedAmount = newAmount, reminderEnabled = reminderEnabled))
        }
    }

    fun removeBudgetForCategory(budgetCategory: BudgetCategory) {
        viewModelScope.launch {
            budgetsRepository.deleteBudgetCategory(budgetCategory)
        }
    }

    fun calculateRecurringTotalForCategory(categoryId: Int) {
        viewModelScope.launch {
            val total = recurringTransactionRepository.getActiveRecurringTransactions()
               .first()
               .filter { it.categoryId == categoryId }
               .sumOf { it.amount }
            _recurringTotalForCategory.value = total
        }
    }

    fun clearRecurringTotal() {
        _recurringTotalForCategory.value = 0.0
    }
}
