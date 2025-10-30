package com.achievemeaalk.freedjf.ui.dashboard

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import com.achievemeaalk.freedjf.data.preferences.generateInsightId
import com.achievemeaalk.freedjf.domain.repository.AccountsRepository
import com.achievemeaalk.freedjf.domain.repository.BudgetsRepository
import com.achievemeaalk.freedjf.domain.repository.CategoriesRepository
import com.achievemeaalk.freedjf.domain.repository.RecurringTransactionRepository
import com.achievemeaalk.freedjf.domain.repository.TransactionsRepository
import com.achievemeaalk.freedjf.domain.service.BalanceCalculationService
import com.achievemeaalk.freedjf.domain.usecase.GenerateInsightsUseCase
import com.achievemeaalk.freedjf.review.InAppReviewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

enum class TimeFilter {
    DAILY, WEEKLY, MONTHLY, YEARLY, ALL
}

data class CategorySpending(
    val categoryId: Int,
    val categoryName: String,
    val amount: Double,
    val categoryColor: String
)

data class BudgetSummary(
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val month: YearMonth = YearMonth.now()
) {
    val remainingBudget: Double get() = totalBudget - totalSpent
}

data class UpcomingBill(
    val id: Int,
    val name: String,
    val amount: Double,
    val dueDate: Long,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String,
    val isOverdue: Boolean,
    val daysUntilDue: Int
)

data class UpcomingBillsSummary(
    val upcomingBills: List<UpcomingBill> = emptyList(),
    val totalMonthlyAmount: Double = 0.0,
    val overdueCount: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val accountsRepository: AccountsRepository,
    private val budgetsRepository: BudgetsRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val preferencesRepository: PreferencesRepository,
    private val balanceCalculationService: BalanceCalculationService,
    private val inAppReviewManager: InAppReviewManager,
    private val generateInsightsUseCase: GenerateInsightsUseCase
) : ViewModel() {

    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.MONTHLY)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    private val _insightsAcknowledged = MutableStateFlow(false)
    val insightsAcknowledged: StateFlow<Boolean> = _insightsAcknowledged.asStateFlow()

    private var lastSeenInsights: List<FinancialInsight> = emptyList()

    val isBalanceVisible: StateFlow<Boolean> = preferencesRepository.isBalanceVisible
    val isIncomeSpentVisible: StateFlow<Boolean> = preferencesRepository.isIncomeSpentVisible
    val isSpendingChartVisible: StateFlow<Boolean> = preferencesRepository.isSpendingChartVisible
    val isUpcomingBillsVisible: StateFlow<Boolean> = preferencesRepository.isUpcomingBillsVisible
    val isRecentTransactionsVisible: StateFlow<Boolean> = preferencesRepository.isRecentTransactionsVisible
    val userName: StateFlow<String> = preferencesRepository.userName

    data class TransactionDetail(
        val transaction: MyFinTransaction,
        val category: Category?,
        val account: Account?
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardState: StateFlow<DashboardState> = _selectedTimeFilter
        .flatMapLatest { filter ->
            val budgetFlow = budgetsRepository.getBudgetByMonthAndYear(
                YearMonth.now().monthValue,
                YearMonth.now().year
            ).flatMapLatest { budget ->
                if (budget == null) {
                    flowOf(emptyList())
                } else {
                    budgetsRepository.getBudgetCategoriesWithCategory(budget.budgetId)
                }
            }

            combine(
                transactionsRepository.getTransactions(""),
                categoriesRepository.getAllCategories(),
                accountsRepository.getAllAccounts(),
                budgetFlow,
                recurringTransactionRepository.getAllRecurringTransactions()
            ) { transactions, categories, accounts, budgetDetails, recurringTransactions ->

                val filteredTransactions = filterTransactions(transactions, filter)
                val totalBalance = accounts.sumOf { it.balance }

                val totalIncome = filteredTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }

                val totalSpent = filteredTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                val spendingByCategory = filteredTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .map { (categoryId, transactions) ->
                        val category = categories.find { it.categoryId == categoryId }
                        CategorySpending(
                            categoryId = categoryId ?: -1,
                            categoryName = category?.name ?: "Uncategorized",
                            amount = transactions.sumOf { it.amount },
                            categoryColor = category?.color ?: "#808080"
                        )
                    }

                val recentTransactionsDetails = transactions.take(5).map { transaction ->
                    TransactionDetail(
                        transaction = transaction,
                        category = categories.find { it.categoryId == transaction.categoryId },
                        account = accounts.find { it.accountId == transaction.accountId }
                    )
                }
                val date = YearMonth.now()
                val startOfMonth = date.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = date.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val monthlyTransactions = transactions.filter { it.dateTimestamp in startOfMonth..endOfMonth }
                val totalBudget = budgetDetails.sumOf { it.budgetCategory.plannedAmount }
                val totalSpentForBudget = monthlyTransactions
                    .filter { transaction ->
                        transaction.type == TransactionType.EXPENSE && budgetDetails.any { it.category.categoryId == transaction.categoryId }
                    }.sumOf { it.amount }

                val budgetSummary = BudgetSummary(
                    totalBudget = totalBudget,
                    totalSpent = totalSpentForBudget,
                    month = date
                )

                val currentTime = System.currentTimeMillis()
                val upcomingBills = recurringTransactions
                    .sortedBy { it.nextDueDate }
                    .take(3)
                    .map { recurring ->
                        val category = categories.find { it.categoryId == recurring.categoryId }
                        val daysUntilDue = ((recurring.nextDueDate - currentTime) / (1000 * 60 * 60 * 24)).toInt()
                        UpcomingBill(
                            id = recurring.id,
                            name = recurring.name,
                            amount = recurring.amount,
                            dueDate = recurring.nextDueDate,
                            categoryName = category?.name ?: "Uncategorized",
                            categoryColor = category?.color ?: "#808080",
                            categoryIcon = category?.icon ?: "attach_money",
                            isOverdue = recurring.nextDueDate < currentTime,
                            daysUntilDue = daysUntilDue
                        )
                    }

                val totalMonthlyRecurring = recurringTransactions
                    .filter { it.nextDueDate >= startOfMonth && it.nextDueDate <= endOfMonth }
                    .sumOf { it.amount }

                val overdueCount = recurringTransactions.count { it.nextDueDate < currentTime }

                val upcomingBillsSummary = UpcomingBillsSummary(
                    upcomingBills = upcomingBills,
                    totalMonthlyAmount = totalMonthlyRecurring,
                    overdueCount = overdueCount
                )
                
                val insights: List<FinancialInsight> = generateInsightsUseCase()

                DashboardState(
                    totalBalance = totalBalance,
                    totalIncome = totalIncome,
                    totalSpent = totalSpent,
                    spendingByCategory = spendingByCategory,
                    recentTransactions = recentTransactionsDetails,
                    budgetSummary = budgetSummary,
                    upcomingBillsSummary = upcomingBillsSummary,
                    financialInsights = insights,
                    accounts = accounts
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DashboardState()
        )

    init {
        viewModelScope.launch {
            dashboardState.map { it.financialInsights }.distinctUntilChanged().collect { newInsights ->
                if (newInsights.isNotEmpty() && newInsights != lastSeenInsights) {
                    _insightsAcknowledged.value = false
                }
            }
        }
    }

    val showInsightsBadge: StateFlow<Boolean> = combine(
        dashboardState.map { it.financialInsights }.distinctUntilChanged(),
        preferencesRepository.acknowledgedInsights
    ) { insights, acknowledgedIds ->
        insights.isNotEmpty() && generateInsightId(insights) !in acknowledgedIds
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun acknowledgeInsights() {
        viewModelScope.launch {
            preferencesRepository.acknowledgeInsights(dashboardState.value.financialInsights)
        }
    }

    fun selectTimeFilter(filter: TimeFilter) {
        _selectedTimeFilter.value = filter
    }

    fun deleteTransaction(transaction: MyFinTransaction) = viewModelScope.launch {
        transactionsRepository.deleteTransaction(transaction)
        balanceCalculationService.updateBalanceForDeletedTransaction(transaction)
    }

    fun checkAndTriggerInAppReview(activity: Activity) {
        viewModelScope.launch {
            if (preferencesRepository.appLaunchCount.value >= 5 && preferencesRepository.transactionsAddedCount.value >= 10 && !preferencesRepository.reviewPromptCompleted.value) {
                inAppReviewManager.requestReview(activity)
                preferencesRepository.setReviewPromptCompleted(true)
            }
        }
    }

    private fun filterTransactions(transactions: List<MyFinTransaction>, filter: TimeFilter): List<MyFinTransaction> {
        if (filter == TimeFilter.ALL) return transactions

        val startCal = Calendar.getInstance()
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)

        val endCal = startCal.clone() as Calendar

        when (filter) {
            TimeFilter.DAILY -> {
                endCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            TimeFilter.WEEKLY -> {
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                endCal.time = startCal.time
                endCal.add(Calendar.WEEK_OF_YEAR, 1)
            }
            TimeFilter.MONTHLY -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                endCal.time = startCal.time
                endCal.add(Calendar.MONTH, 1)
            }
            TimeFilter.YEARLY -> {
                startCal.set(Calendar.DAY_OF_YEAR, 1)
                endCal.time = startCal.time
                endCal.add(Calendar.YEAR, 1)
            }
            TimeFilter.ALL -> return transactions
        }

        return transactions.filter { it.dateTimestamp >= startCal.timeInMillis && it.dateTimestamp < endCal.timeInMillis }
    }

    private fun Long.toLocalDate(): LocalDate {
        return this.let {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it
            cal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }
}

data class DashboardState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalSpent: Double = 0.0,
    val spendingByCategory: List<CategorySpending> = emptyList(),
    val recentTransactions: List<DashboardViewModel.TransactionDetail> = emptyList(),
    val budgetSummary: BudgetSummary = BudgetSummary(),
    val upcomingBillsSummary: UpcomingBillsSummary = UpcomingBillsSummary(),
    val financialInsights: List<FinancialInsight> = emptyList(),
    val accounts: List<Account> = emptyList()
)
