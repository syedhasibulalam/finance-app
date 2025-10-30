package com.achievemeaalk.freedjf.ui.accounts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.BuildConfig
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.ui.components.PremiumAwareBannerAd
import com.achievemeaalk.freedjf.ui.dashboard.EmptyStateAnimation
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.theme.AppTheme
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.theme.displayMediumBold
import com.achievemeaalk.freedjf.util.IconProvider
import com.achievemeaalk.freedjf.util.formatCurrency
import com.canopas.lib.showcase.IntroShowcaseScope
import com.canopas.lib.showcase.component.ShowcaseShape
import com.canopas.lib.showcase.component.ShowcaseStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroShowcaseScope.AccountsScreen(
    navController: NavController,
    viewModel: AccountsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    val isPremium = true

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }
    val rectangularShowcaseStyle = ShowcaseStyle.Default.copy(
        showcaseShape = ShowcaseShape.RECTANGLE(roundCorner = Dimensions.cornerRadiusExtraLarge)
    )


    if (showDeleteDialog && accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(stringResource(R.string.delete_account_message)) },
            confirmButton = {
                Button(onClick = {
                    accountToDelete?.let { viewModel.deleteAccount(it) }
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold { padding ->
       Column(
           modifier = Modifier
               .padding(padding)
               .padding(horizontal = Dimensions.screenPaddingHorizontal)
       ) {
           TotalBalanceCard(
               totalBalance = totalBalance,
               currencyCode = currency,
               modifier = Modifier.introShowCaseTarget(
                   index = 5,
                   style = rectangularShowcaseStyle,
                   content = {
                       Column {
                           Text(
                               text = stringResource(R.string.total_balance),
                               style = MaterialTheme.typography.headlineSmall,
                               color = MaterialTheme.colorScheme.onPrimary,
                           )
                           Text(
                               text = stringResource(R.string.total_balance_tooltip),
                               style = MaterialTheme.typography.bodyLarge,
                               color = MaterialTheme.colorScheme.onPrimary
                           )
                       }
                   }
               )
           )
           Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

           Row(
               modifier = Modifier.fillMaxWidth(),
               horizontalArrangement = Arrangement.SpaceBetween,
               verticalAlignment = Alignment.CenterVertically
           ) {
               Text(stringResource(R.string.my_accounts), style = MaterialTheme.typography.titleLarge)
                IconButton(
                    onClick = {
                        selectedAccount = null
                        showBottomSheet = true
                    },
                   modifier = Modifier.introShowCaseTarget(
                       index = 4,
                       content = {
                           Column {
                               Text(
                                   text = stringResource(R.string.add_account_tooltip_title),
                                   style = MaterialTheme.typography.headlineSmall,
                                   color = MaterialTheme.colorScheme.onPrimary,
                               )
                               Text(
                                   text = stringResource(R.string.add_account_tooltip_message),
                                   style = MaterialTheme.typography.bodyLarge,
                                   color = MaterialTheme.colorScheme.onPrimary
                               )
                           }
                       }
                   )
               ) {
                   Icon(
                       imageVector = Icons.Default.AddCircle,
                       contentDescription = stringResource(R.string.add_account),
                       tint = MaterialTheme.colorScheme.primary,
                       modifier = Modifier.size(Dimensions.iconSizeLarge)
                   )
               }
           }
           Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

           if (accounts.isEmpty()) {
               EmptyStateAnimation(
                   title = stringResource(R.string.no_accounts_title),
                   subtitle = stringResource(R.string.no_accounts_subtitle),
                   lottieResourceId = R.raw.wallet
               )
           } else {
               Column(modifier = Modifier.fillMaxSize()) {
                   LazyColumn(
                       modifier = Modifier.weight(1f),
                       verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                   ) {
                       val groupedAccounts = accounts.groupBy { it.accountType }

                       groupedAccounts.forEach { (type, accountList) ->
                           item {
                               Text(
                                   text = stringResource(R.string.accounts_group_title, type),
                                   style = MaterialTheme.typography.titleMedium,
                                   modifier = Modifier.padding(vertical = Dimensions.spacingSmall)
                               )
                           }
                           items(accountList) { account ->
                               AccountItem(
                                   account = account,
                                   onClick = {
                                       selectedAccount = account
                                       showBottomSheet = true
                                   },
                                   onDeleteClick = {
                                       accountToDelete = account
                                       showDeleteDialog = true
                                   },
                                   currencyCode = currency
                               )
                           }
                       }
                   }
                    PremiumAwareBannerAd(adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID, isPremium = isPremium)
               }
           }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = null
        ) {
            AddEditAccountBottomSheet(
                account = selectedAccount,
                onSave = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TotalBalanceCard(
    totalBalance: Double,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(Dimensions.cardPadding)) {
            Text(
                text = stringResource(R.string.total_balance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatCurrency(totalBalance, currencyCode),
                style = MaterialTheme.typography.displayMediumBold,
            )
        }
    }
}

@Composable
fun AccountItem(
    account: Account,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    currencyCode: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(Dimensions.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimensions.avatarSizeLarge)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = IconProvider.getIconPainter(account.icon),
                        contentDescription = account.name,
                        modifier = Modifier.size(Dimensions.iconSizeMedium),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(Dimensions.spacingLarge))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = account.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = account.accountType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatCurrency(account.balance, currencyCode),
                    style = MaterialTheme.typography.bodyLarge
                )
                val deleteInteractionSource = remember { MutableInteractionSource() }
                val isDeletePressed by deleteInteractionSource.collectIsPressedAsState()
                val deleteScale by animateFloatAsState(
                    targetValue = if (isDeletePressed) 0.95f else 1f,
                    label = "deleteScale"
                )
                IconButton(
                    onClick = onDeleteClick,
                    interactionSource = deleteInteractionSource,
                    modifier = Modifier.graphicsLayer {
                        scaleX = deleteScale
                        scaleY = deleteScale
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_account_title)
                    )
                }
            }

            if (account.accountType == stringResource(R.string.credit_card) && account.creditLimit != null && account.creditLimit > 0) {
                val utilization = (account.balance / account.creditLimit).toFloat()
                val progressColor = when {
                    utilization < 0.3f -> MaterialTheme.colorScheme.primary
                    utilization < 0.7f -> AppTheme.colors.warning
                    else -> MaterialTheme.colorScheme.error
                }

                Spacer(Modifier.height(Dimensions.spacingSmall))
                LinearProgressIndicator(
                    progress = utilization,
                    modifier = Modifier.fillMaxWidth(),
                    color = progressColor
                )
                Spacer(Modifier.height(Dimensions.spacingSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.used,
                            formatCurrency(account.balance, currencyCode)
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(
                            R.string.credit_limit_label,
                            formatCurrency(account.creditLimit, currencyCode)
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
