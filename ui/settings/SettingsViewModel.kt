// main/java/com/achievemeaalk/freedjf/ui/settings/SettingsViewModel.kt
package com.achievemeaalk.freedjf.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achievemeaalk.freedjf.data.db.MyFinDatabase
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import com.achievemeaalk.freedjf.data.preferences.Theme
import com.achievemeaalk.freedjf.domain.repository.*
import com.achievemeaalk.freedjf.util.BackupRestoreHelper
import com.achievemeaalk.freedjf.util.CsvExporter
import com.achievemeaalk.freedjf.util.ShareHelper
import com.achievemeaalk.freedjf.ui.ads.AdViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.achievemeaalk.freedjf.util.RestartUtil
import kotlinx.coroutines.flow.combine
import com.achievemeaalk.freedjf.R

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val transactionsRepository: TransactionsRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val accountsRepository: AccountsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val budgetsRepository: BudgetsRepository,
    private val database: MyFinDatabase,
    private val adViewModel: AdViewModel,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val theme: StateFlow<Theme> = preferencesRepository.theme
    val dueSoonDays: StateFlow<Int> = preferencesRepository.dueSoonDays
    val completedShowcaseRoutes: StateFlow<Set<String>> = preferencesRepository.completedShowcaseRoutes

    val categories: StateFlow<List<Category>> = categoriesRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currency: StateFlow<String> = preferencesRepository.currency
    val language: StateFlow<String> = preferencesRepository.language

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    private val _backupResult = MutableStateFlow<String?>(null)
    val backupResult: StateFlow<String?> = _backupResult.asStateFlow()

    private val _restoreResult = MutableStateFlow<String?>(null)
    val restoreResult: StateFlow<String?> = _restoreResult.asStateFlow()

    // Paywall removed

    val isBalanceVisible: StateFlow<Boolean> = preferencesRepository.isBalanceVisible
    val isIncomeSpentVisible: StateFlow<Boolean> = preferencesRepository.isIncomeSpentVisible
    val isSpendingChartVisible: StateFlow<Boolean> = preferencesRepository.isSpendingChartVisible
    val isUpcomingBillsVisible: StateFlow<Boolean> = preferencesRepository.isUpcomingBillsVisible
    val isRecentTransactionsVisible: StateFlow<Boolean> = preferencesRepository.isRecentTransactionsVisible
    val onboardingCompleted: StateFlow<Boolean> = preferencesRepository.onboardingCompleted
    val getStartedCompleted: StateFlow<Boolean> = preferencesRepository.getStartedCompleted
    val firstAccountPromptCompleted: StateFlow<Boolean> = preferencesRepository.firstAccountPromptCompleted
    val userName: StateFlow<String> = preferencesRepository.userName

    // SubscriptionStatus removed

    init {
        viewModelScope.launch {
            // This will ensure that the initial values are loaded before isLoading becomes false
            preferencesRepository.onboardingCompleted.first()
            _isLoading.value = false
        }
        adViewModel.preloadSettingsInterstitial(context) // Preload the ad
    }

    // Paywall navigation removed

    fun setGetStartedCompleted(completed: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setGetStartedCompleted(completed)
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(completed)
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.setTheme(theme)
        }
    }

    fun setCurrency(currencyCode: String) {
        viewModelScope.launch {
            preferencesRepository.setCurrency(currencyCode)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
        }
    }

    fun setDueSoonDays(days: Int) {
        viewModelScope.launch {
            preferencesRepository.setDueSoonDays(days)
        }
    }

    fun exportTransactionsToCsv(context: Context) {
        viewModelScope.launch {
            try {
                _isExporting.value = true
                _exportResult.value = null

                val transactions = transactionsRepository.getTransactions("").first()
                val accounts = accountsRepository.getAllAccounts().first()
                val csvData = CsvExporter.exportTransactions(context, transactions, accounts)
                ShareHelper.shareCsv(context, csvData)

                _exportResult.value = context.getString(R.string.export_successful, transactions.size)
            } catch (e: Exception) {
                _exportResult.value = context.getString(R.string.export_failed, e.message ?: "")
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun clearExportResult() {
        _exportResult.value = null
    }

    fun addCompletedShowcaseRoute(route: String) {
        viewModelScope.launch {
            preferencesRepository.addCompletedShowcaseRoute(route)
        }
    }

    fun createBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                BackupRestoreHelper.createBackup(context, database, uri)
                _backupResult.value = context.getString(R.string.backup_successful)
            } catch (e: Exception) {
                _backupResult.value = context.getString(R.string.backup_failed, e.message ?: "")
            }
        }
    }

    fun restoreBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                BackupRestoreHelper.restoreBackup(context, database, uri)
                _restoreResult.value = context.getString(R.string.restore_successful)
            } catch (e: Exception) {
                _restoreResult.value = context.getString(R.string.restore_failed, e.message ?: "")
            }
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            database.transactionDao().clearAllTransactions()
            database.recurringTransactionDao().clearAllRecurringTransactions()
            adViewModel.showSettingsActionAd(context) // Show ad
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            deleteAllRecords()
            database.accountDao().clearAllAccounts()
            database.categoryDao().clearAllCategories()
            database.budgetDao().clearAllBudgets()
            adViewModel.showSettingsActionAd(context) // Show ad
        }
    }

    fun resetApplication() {
        viewModelScope.launch {
            deleteAllData()
            preferencesRepository.clearAll()
            database.databaseDao().resetAllAutoIncrementCounters()
            adViewModel.showSettingsActionAd(context) // Show ad
            RestartUtil.restartApp(context)
        }
    }

    fun setBalanceVisible(visible: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBalanceVisible(visible)
        }
    }

    fun setIncomeSpentVisible(visible: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setIncomeSpentVisible(visible)
        }
    }

    fun setSpendingChartVisible(visible: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSpendingChartVisible(visible)
        }
    }

    fun setUpcomingBillsVisible(visible: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUpcomingBillsVisible(visible)
        }
    }

    fun setRecentTransactionsVisible(visible: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setRecentTransactionsVisible(visible)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferencesRepository.setUserName(name)
        }
    }

    // Debug subscription toggles removed
}
