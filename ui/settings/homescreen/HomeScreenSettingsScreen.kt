package com.achievemeaalk.freedjf.ui.settings.homescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.ui.theme.Dimensions

@Composable
fun HomeScreenSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isBalanceVisible by viewModel.isBalanceVisible.collectAsState()
    val isIncomeSpentVisible by viewModel.isIncomeSpentVisible.collectAsState()
    val isSpendingChartVisible by viewModel.isSpendingChartVisible.collectAsState()
    val isUpcomingBillsVisible by viewModel.isUpcomingBillsVisible.collectAsState()
    val isRecentTransactionsVisible by viewModel.isRecentTransactionsVisible.collectAsState()

    val settings = listOf(
        Triple(stringResource(R.string.home_screen_settings_total_balance_card), isBalanceVisible, viewModel::setBalanceVisible),
        Triple(stringResource(R.string.home_screen_settings_income_and_spent_section), isIncomeSpentVisible, viewModel::setIncomeSpentVisible),
        Triple(stringResource(R.string.home_screen_settings_spending_by_category_card), isSpendingChartVisible, viewModel::setSpendingChartVisible),
        Triple(stringResource(R.string.home_screen_settings_upcoming_bills_card), isUpcomingBillsVisible, viewModel::setUpcomingBillsVisible),
        Triple(stringResource(R.string.home_screen_settings_recent_transactions), isRecentTransactionsVisible, viewModel::setRecentTransactionsVisible)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
    ) {
        settings.forEach { (title, isChecked, onCheckedChange) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isChecked,
                        onCheckedChange = onCheckedChange
                    )
                }
            }
        }
    }
}
