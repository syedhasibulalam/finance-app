package com.achievemeaalk.freedjf.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.RecurrenceFrequency
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.ui.components.AnimatedPrimaryButton
import com.achievemeaalk.freedjf.ui.recurring.RecurringTransactionsViewModel
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.theme.ExpenseColor
import com.achievemeaalk.freedjf.ui.theme.IncomeColor
import com.achievemeaalk.freedjf.ui.theme.headlineLargeSemiBold
import com.achievemeaalk.freedjf.util.IconProvider
import com.achievemeaalk.freedjf.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionDetailDialog(
    transactionDetail: DashboardViewModel.TransactionDetail,
    onDismissRequest: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    recurringTransactionsViewModel: RecurringTransactionsViewModel = hiltViewModel()
) {
    val transaction = transactionDetail.transaction
    val category = transactionDetail.category
    val account = transactionDetail.account
    val currency by settingsViewModel.currency.collectAsState()
    val context = LocalContext.current


    val (dialogColor, sign) = when (transaction.type) {
        TransactionType.INCOME -> Pair(IncomeColor.copy(alpha = 0.9f), "+")
        TransactionType.EXPENSE -> Pair(ExpenseColor.copy(alpha = 0.9f), "-")
        TransactionType.TRANSFER -> Pair(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), "")
    }

    val formatter = remember { SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // Header section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(dialogColor)
                        .padding(16.dp)
                ) {
                    IconButton(onClick = onDismissRequest, modifier = Modifier.align(Alignment.TopStart)) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Row(modifier = Modifier.align(Alignment.TopEnd)) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = { onEditClick(transaction.transactionId) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = transaction.type.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "$sign${formatCurrency(transaction.amount, currency)}",
                            style = MaterialTheme.typography.headlineLargeSemiBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = formatter.format(Date(transaction.dateTimestamp)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                }

                // Details Section
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    DetailRow(title = stringResource(R.string.account), value = account?.name ?: stringResource(R.string.not_applicable), icon = account?.icon ?: "account_balance_wallet")

                    if (transaction.type != TransactionType.TRANSFER) {
                        DetailRow(title = stringResource(R.string.category), value = category?.name ?: stringResource(R.string.uncategorized), icon = category?.icon ?: "help")
                    }

                    if (transaction.description.isNotBlank()) {
                        DetailRow(title = stringResource(R.string.notes), value = transaction.description, icon = "receipt")
                    } else {
                        DetailRow(title = stringResource(R.string.notes), value = stringResource(R.string.no_notes), icon = "receipt")
                    }


                    if (transaction.type == TransactionType.EXPENSE || transaction.type == TransactionType.INCOME) {

                        AnimatedPrimaryButton(
                            onClick = {
                                recurringTransactionsViewModel.addOrUpdateRecurringTransaction(
                                    name = category?.name ?: transaction.description.take(20),
                                    amount = transaction.amount,
                                    accountId = transaction.accountId,
                                    categoryId = transaction.categoryId ?: 0,
                                    nextDueDate = transaction.dateTimestamp,
                                    frequency = RecurrenceFrequency.MONTHLY,
                                    isSubscription = true,
                                    transactionType = transaction.type,
                                    context = context
                                )
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.make_recurring))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(title: String, value: String, icon: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp)
        )
        Spacer(Modifier.width(16.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = IconProvider.getIconPainter(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}