package com.achievemeaalk.freedjf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.ui.dashboard.TimeFilter
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.theme.ExpenseColor
import com.achievemeaalk.freedjf.ui.theme.IncomeColor
import com.achievemeaalk.freedjf.ui.theme.TransferColor
import com.achievemeaalk.freedjf.util.formatCurrency
import java.util.Locale

private data class RecordAction(
    val transactionType: TransactionType,
    val title: String,
    val icon: ImageVector? = null,
    val drawableRes: Int? = null,
    val color: Color
)

@Composable
fun AddRecordBottomSheet(
    onActionSelected: (TransactionType) -> Unit,
    onDismissRequest: () -> Unit,
    totalBalance: Double,
    spentAmount: Double,
    timeFilter: TimeFilter,
    accountsCount: Int,
    currencyCode: String
) {

    val actions = listOf(
        RecordAction(
            transactionType = TransactionType.INCOME,
            title = stringResource(id = R.string.transaction_type_income),
            drawableRes = R.drawable.ic_arrow_upward,
            color = IncomeColor,

            ),
        RecordAction(
            transactionType = TransactionType.EXPENSE,
            title = stringResource(id = R.string.transaction_type_expense),
            drawableRes = R.drawable.ic_arrow_downward,
            color = ExpenseColor,
        ),
        RecordAction(
            transactionType = TransactionType.TRANSFER,
            title = stringResource(id = R.string.transaction_type_transfer),
            drawableRes = R.drawable.ic_sync,
            color = TransferColor,
        )
    )
    // --- THIS IS THE FIX ---
    // The background is now applied to the root Column for the entire sheet
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Apply background here
            .padding(Dimensions.bottomSheetPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.add_record), style = MaterialTheme.typography.headlineSmall)
            IconButton(
                onClick = onDismissRequest
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        }
        Spacer(Modifier.height(Dimensions.spacingLarge))

        actions.forEachIndexed { index, action ->
            val subtitle = when (action.transactionType) {
                TransactionType.INCOME -> stringResource(R.string.current_balance, formatCurrency(totalBalance, currencyCode))
                TransactionType.EXPENSE -> {
                    val filterName = timeFilter.name.lowercase(Locale.getDefault())
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    stringResource(R.string.spent_this_month, filterName, formatCurrency(spentAmount, currencyCode))
                }
                TransactionType.TRANSFER -> stringResource(R.string.between_your_accounts, accountsCount)
            }

            ActionItem(
                action = action,
                subtitle = subtitle,
                onClick = { onActionSelected(action.transactionType) }
            )
            if (index < actions.size - 1) {
                Divider(modifier = Modifier.padding(vertical = Dimensions.spacingSmall))
            }
        }
        Spacer(Modifier.height(Dimensions.spacingLarge))
    }
}


@Composable
private fun ActionItem(action: RecordAction, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.spacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimensions.avatarSizeMedium)
                .clip(CircleShape)
                .background(action.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (action.drawableRes != null) {
                Icon(
                    painter = painterResource(id = action.drawableRes),
                    contentDescription = action.title,
                    modifier = Modifier.size(Dimensions.iconSizeMedium),
                    tint = action.color
                )
            } else if (action.icon != null) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    modifier = Modifier.size(Dimensions.iconSizeMedium),
                    tint = action.color
                )
            }
        }

        Spacer(Modifier.width(Dimensions.spacingLarge))
        Column {
            Text(text = action.title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}