// com/achievemeaalk/freedjf/data/preferences/PreferencesRepository.kt
package com.achievemeaalk.freedjf.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.achievemeaalk.freedjf.ui.dashboard.FinancialInsight
import com.achievemeaalk.freedjf.util.CurrencyUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
  private val sharedPreferences: SharedPreferences,
  @ApplicationContext private val context: Context // Inject context
) {
  private val _theme = MutableStateFlow(getCurrentTheme())
  val theme: StateFlow<Theme> = _theme

  // Use a lazy delegate to get the currency from locale only once
  private val defaultCurrency by lazy {
    CurrencyUtils.getCurrencyCodeForLocale(context)
  }

  private val _currency = MutableStateFlow(getCurrentCurrency())
  val currency: StateFlow<String> = _currency

  private val _language = MutableStateFlow(getCurrentLanguage())
  val language: StateFlow<String> = _language


  private val _dueSoonDays = MutableStateFlow(getDueSoonDays())
  val dueSoonDays: StateFlow<Int> = _dueSoonDays

  private val _completedShowcaseRoutes = MutableStateFlow(getCompletedShowcaseRoutes())
  val completedShowcaseRoutes: StateFlow<Set<String>> = _completedShowcaseRoutes

  private val _acknowledgedInsights = MutableStateFlow(getAcknowledgedInsights())
  val acknowledgedInsights: StateFlow<Set<String>> = _acknowledgedInsights

  private val _appLaunchCount = MutableStateFlow(getAppLaunchCount())
  val appLaunchCount: StateFlow<Int> = _appLaunchCount

  private val _transactionsAddedCount = MutableStateFlow(getTransactionsAddedCount())
  val transactionsAddedCount: StateFlow<Int> = _transactionsAddedCount

  private val _reviewPromptCompleted = MutableStateFlow(isReviewPromptCompleted())
  val reviewPromptCompleted: StateFlow<Boolean> = _reviewPromptCompleted

  private val _isBalanceVisible = MutableStateFlow(isBalanceVisible())
  val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible

  private val _isIncomeSpentVisible = MutableStateFlow(isIncomeSpentVisible())
  val isIncomeSpentVisible: StateFlow<Boolean> = _isIncomeSpentVisible

  private val _isSpendingChartVisible = MutableStateFlow(isSpendingChartVisible())
  val isSpendingChartVisible: StateFlow<Boolean> = _isSpendingChartVisible

  private val _isUpcomingBillsVisible = MutableStateFlow(isUpcomingBillsVisible())
  val isUpcomingBillsVisible: StateFlow<Boolean> = _isUpcomingBillsVisible

  private val _isRecentTransactionsVisible = MutableStateFlow(isRecentTransactionsVisible())
  val isRecentTransactionsVisible: StateFlow<Boolean> = _isRecentTransactionsVisible

  private val _onboardingCompleted = MutableStateFlow(isOnboardingCompleted())
  val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted

  private val _getStartedCompleted = MutableStateFlow(isGetStartedCompleted())
  val getStartedCompleted: StateFlow<Boolean> = _getStartedCompleted

  private val _firstAccountPromptCompleted = MutableStateFlow(isFirstAccountPromptCompleted())
      val firstAccountPromptCompleted: StateFlow<Boolean> = _firstAccountPromptCompleted

    private val _userName = MutableStateFlow(getUserName())
    val userName: StateFlow<String> = _userName

    fun setUserName(name: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
        _userName.value = name
    }

    private fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
    }

    fun setOnboardingCompleted(completed: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    _onboardingCompleted.value = completed
  }

  fun setGetStartedCompleted(completed: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_GET_STARTED_COMPLETED, completed).apply()
    _getStartedCompleted.value = completed
  }



  fun setTheme(theme: Theme) {
    sharedPreferences.edit().putString(KEY_THEME, theme.name).apply()
    _theme.value = theme
  }

  fun setCurrency(currencyCode: String) {
    sharedPreferences.edit().putString(KEY_CURRENCY, currencyCode).apply()
    _currency.value = currencyCode
  }

  fun setLanguage(language: String) {
    sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()
    _language.value = language
  }

  fun setDueSoonDays(days: Int) {
    sharedPreferences.edit().putInt(KEY_DUE_SOON_DAYS, days).apply()
    _dueSoonDays.value = days
  }

  private fun getCurrentTheme(): Theme {
    val themeName = sharedPreferences.getString(KEY_THEME, Theme.SYSTEM.name)
    return Theme.valueOf(themeName ?: Theme.SYSTEM.name)
  }

  private fun getCurrentCurrency(): String {
    return sharedPreferences.getString(KEY_CURRENCY, defaultCurrency) ?: defaultCurrency
  }

  private fun getCurrentLanguage(): String {
    return sharedPreferences.getString(KEY_LANGUAGE, "en") ?: "en"
  }

  private fun getDueSoonDays(): Int {
    return sharedPreferences.getInt(KEY_DUE_SOON_DAYS, 7)
  }

  fun addCompletedShowcaseRoute(route: String) {
    val currentRoutes = getCompletedShowcaseRoutes().toMutableSet()
    if (currentRoutes.add(route)) {
      sharedPreferences.edit().putStringSet(KEY_COMPLETED_SHOWCASE_ROUTES, currentRoutes).apply()
      _completedShowcaseRoutes.value = currentRoutes
    }
  }

  private fun getCompletedShowcaseRoutes(): Set<String> {
    return sharedPreferences.getStringSet(KEY_COMPLETED_SHOWCASE_ROUTES, emptySet()) ?: emptySet()
  }

  fun acknowledgeInsights(insights: List<FinancialInsight>) {
    val currentAcknowledged = getAcknowledgedInsights().toMutableSet()
    val insightId = generateInsightId(insights)
    if (currentAcknowledged.add(insightId)) {
      sharedPreferences.edit().putStringSet(KEY_ACKNOWLEDGED_INSIGHTS, currentAcknowledged).apply()
      _acknowledgedInsights.value = currentAcknowledged
    }
  }

  private fun getAcknowledgedInsights(): Set<String> {
    return sharedPreferences.getStringSet(KEY_ACKNOWLEDGED_INSIGHTS, emptySet()) ?: emptySet()
  }

  fun incrementAppLaunchCount() {
    val count = getAppLaunchCount() + 1
    sharedPreferences.edit().putInt(KEY_APP_LAUNCH_COUNT, count).apply()
    _appLaunchCount.value = count
  }

  private fun getAppLaunchCount(): Int {
    return sharedPreferences.getInt(KEY_APP_LAUNCH_COUNT, 0)
  }

  fun incrementTransactionsAddedCount() {
    val count = getTransactionsAddedCount() + 1
    sharedPreferences.edit().putInt(KEY_TRANSACTION_ADD_COUNT, count).apply()
    _transactionsAddedCount.value = count
  }

  private fun getTransactionsAddedCount(): Int {
    return sharedPreferences.getInt(KEY_TRANSACTION_ADD_COUNT, 0)
  }

  fun setReviewPromptCompleted(completed: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_REVIEW_PROMPT_COMPLETED, completed).apply()
    _reviewPromptCompleted.value = completed
  }

  private fun isReviewPromptCompleted(): Boolean {
    return sharedPreferences.getBoolean(KEY_REVIEW_PROMPT_COMPLETED, false)
  }

  fun setBalanceVisible(visible: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_HOME_BALANCE_VISIBLE, visible).apply()
    _isBalanceVisible.value = visible
  }

  private fun isBalanceVisible(): Boolean {
    return sharedPreferences.getBoolean(KEY_HOME_BALANCE_VISIBLE, true)
  }

  fun setIncomeSpentVisible(visible: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_HOME_INCOME_SPENT_VISIBLE, visible).apply()
    _isIncomeSpentVisible.value = visible
  }

  private fun isIncomeSpentVisible(): Boolean {
    return sharedPreferences.getBoolean(KEY_HOME_INCOME_SPENT_VISIBLE, true)
  }

  fun setSpendingChartVisible(visible: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_HOME_SPENDING_CHART_VISIBLE, visible).apply()
    _isSpendingChartVisible.value = visible
  }

  private fun isSpendingChartVisible(): Boolean {
    return sharedPreferences.getBoolean(KEY_HOME_SPENDING_CHART_VISIBLE, true)
  }

  fun setUpcomingBillsVisible(visible: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_HOME_UPCOMING_BILLS_VISIBLE, visible).apply()
    _isUpcomingBillsVisible.value = visible
  }

  private fun isUpcomingBillsVisible(): Boolean {
    return sharedPreferences.getBoolean(KEY_HOME_UPCOMING_BILLS_VISIBLE, true)
  }

  fun setRecentTransactionsVisible(visible: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_HOME_RECENT_TRANSACTIONS_VISIBLE, visible).apply()
    _isRecentTransactionsVisible.value = visible
  }

  private fun isRecentTransactionsVisible(): Boolean {
    return sharedPreferences.getBoolean(KEY_HOME_RECENT_TRANSACTIONS_VISIBLE, true)
  }

  private fun isOnboardingCompleted(): Boolean {
    return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
  }

  private fun isGetStartedCompleted(): Boolean {
    return sharedPreferences.getBoolean(KEY_GET_STARTED_COMPLETED, false)
  }

  private fun isFirstAccountPromptCompleted(): Boolean {
    return sharedPreferences.getBoolean(KEY_FIRST_ACCOUNT_PROMPT_COMPLETED, false)
  }
  fun setFirstAccountPromptCompleted(completed: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_FIRST_ACCOUNT_PROMPT_COMPLETED, completed).apply()
    _firstAccountPromptCompleted.value = completed
  }



  fun clearAll() {
    sharedPreferences.edit().clear().apply()
  }

  companion object {
    const val KEY_THEME = "app_theme"
    const val KEY_CURRENCY = "app_currency"
    const val KEY_LANGUAGE = "app_language"
    const val KEY_DUE_SOON_DAYS = "due_soon_days"
    const val KEY_COMPLETED_SHOWCASE_ROUTES = "completed_showcase_routes"
    const val KEY_ACKNOWLEDGED_INSIGHTS = "acknowledged_insights"
    const val KEY_APP_LAUNCH_COUNT = "app_launch_count"
    const val KEY_TRANSACTION_ADD_COUNT = "transaction_add_count"
    const val KEY_REVIEW_PROMPT_COMPLETED = "review_prompt_completed"
    const val KEY_HOME_BALANCE_VISIBLE = "home_balance_visible"
    const val KEY_HOME_INCOME_SPENT_VISIBLE = "home_income_spent_visible"
    const val KEY_HOME_SPENDING_CHART_VISIBLE = "home_spending_chart_visible"
    const val KEY_HOME_UPCOMING_BILLS_VISIBLE = "home_upcoming_bills_visible"
    const val KEY_HOME_RECENT_TRANSACTIONS_VISIBLE = "home_recent_transactions_visible"
    const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    const val KEY_GET_STARTED_COMPLETED = "get_started_completed"
    const val KEY_FIRST_ACCOUNT_PROMPT_COMPLETED = "first_account_prompt_completed"
    const val KEY_USER_NAME = "user_name"
  }
}

fun generateInsightId(insights: List<FinancialInsight>): String {
  return insights.joinToString(separator = "|") { it.title + it.message }.hashCode().toString()
}

enum class Theme {
  LIGHT, DARK, SYSTEM
}
