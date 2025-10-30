package com.achievemeaalk.freedjf.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.ui.components.ClickableField
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionFilterBottomSheet(
    selectedAccount: Account?,
    selectedCategory: Category?,
    selectedDateRange: Pair<Long, Long>?,
    onApplyFilters: () -> Unit,
    onResetFilters: () -> Unit,
    onShowDateRangePicker: () -> Unit,
    onShowAccountPicker: () -> Unit,
    onShowCategoryPicker: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.filter_transactions), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        ClickableField(
            label = stringResource(R.string.account),
            value = selectedAccount?.name ?: "",
            placeholder = stringResource(R.string.select_account),
            onClick = onShowAccountPicker
        )

        Spacer(modifier = Modifier.height(16.dp))

        ClickableField(
            label = stringResource(R.string.category),
            value = selectedCategory?.name ?: "",
            placeholder = stringResource(R.string.select_category),
            onClick = onShowCategoryPicker
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onShowDateRangePicker,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val start = if (selectedDateRange?.first != 0L && selectedDateRange?.first != null) formatter.format(Date(selectedDateRange.first)) else stringResource(R.string.start_date)
            val end = if (selectedDateRange?.second != 0L && selectedDateRange?.second != null) formatter.format(Date(selectedDateRange.second)) else stringResource(R.string.end_date)
            Text(stringResource(R.string.date_range_format, start, end), style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onResetFilters) {
                Text(stringResource(R.string.reset), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onApplyFilters) {
                Text(stringResource(R.string.apply), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
