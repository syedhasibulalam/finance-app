package com.achievemeaalk.freedjf.ui.transactions

import androidx.compose.animation.AnimatedVisibility
import com.achievemeaalk.freedjf.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.ui.components.AccountBottomSheet
import com.achievemeaalk.freedjf.ui.components.PremiumAwareBannerAd
import com.achievemeaalk.freedjf.ui.components.CategoryBottomSheet
import com.achievemeaalk.freedjf.ui.components.SheetContent
import com.achievemeaalk.freedjf.ui.dashboard.DashboardViewModel
import com.achievemeaalk.freedjf.ui.dashboard.EmptyStateAnimation
import com.achievemeaalk.freedjf.ui.dashboard.RecentTransactionItem
import com.achievemeaalk.freedjf.ui.dashboard.TransactionDetailDialog
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import kotlinx.coroutines.launch
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInVertically
import com.achievemeaalk.freedjf.ui.theme.Motion
import kotlinx.coroutines.delay
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    navController: NavController,
    viewModel: TransactionsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    categoryId: String?
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    var showDetailDialog by remember { mutableStateOf<DashboardViewModel.TransactionDetail?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<MyFinTransaction?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<TransactionType?>(null) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    var selectedDateRange by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    LaunchedEffect(categoryId) {
        if (categoryId != null) {
            viewModel.applyFilters(categoryId = categoryId)
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var currentSheet by remember { mutableStateOf(SheetContent.None) }
    var selectedAccountFilter by remember { mutableStateOf<com.achievemeaalk.freedjf.data.model.Account?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<com.achievemeaalk.freedjf.data.model.Category?>(null) }
    val isConfirmEnabled by remember {
        derivedStateOf {
            dateRangePickerState.selectedStartDateMillis != null &&
                dateRangePickerState.selectedEndDateMillis != null
        }
    }
    if (currentSheet != SheetContent.None) {
        ModalBottomSheet(
            onDismissRequest = { currentSheet = SheetContent.None },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = null
        ) {
            when (currentSheet) {
                SheetContent.Account -> AccountBottomSheet(
                    accounts = accounts,
                    onAccountSelected = { account ->
                        selectedAccountFilter = account
                        currentSheet = SheetContent.Filter
                    },
                    onAddNewAccount = {
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            currentSheet = SheetContent.None
                            navController.navigate("addEditAccount")
                        }
                    },
                    currencyCode = currency
                )
                SheetContent.Category -> CategoryBottomSheet(
                    categories = categories,
                    onCategorySelected = { category ->
                        selectedCategoryFilter = category
                        currentSheet = SheetContent.Filter
                    },
                    onAddNewCategory = {
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            currentSheet = SheetContent.None
                            navController.navigate("categories?showBottomSheet=true")
                        }
                    }                )
                SheetContent.Filter -> TransactionFilterBottomSheet(
                    selectedAccount = selectedAccountFilter,
                    selectedCategory = selectedCategoryFilter,
                    selectedDateRange = selectedDateRange,
                    onApplyFilters = {
                        viewModel.applyFilters(
                            accountId = selectedAccountFilter?.accountId,
                            categoryId =selectedCategoryFilter?.categoryId.toString(),
                            startDate = selectedDateRange?.first,
                            endDate = selectedDateRange?.second
                        )
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            currentSheet = SheetContent.None
                        }
                    },
                    onResetFilters = {
                        viewModel.resetFilters()
                        selectedDateRange = null
                        selectedAccountFilter = null
                        selectedCategoryFilter = null
                        dateRangePickerState.setSelection(null, null)
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            currentSheet = SheetContent.None
                        }
                    },
                    onShowDateRangePicker = { showDateRangePicker = true },
                    onShowAccountPicker = { currentSheet = SheetContent.Account },
                    onShowCategoryPicker = { currentSheet = SheetContent.Category }
                )
                else -> {}
            }
        }
    }

    if (showDateRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDateRangePicker = false
                        // Save the selected range
                        selectedDateRange = Pair(
                            dateRangePickerState.selectedStartDateMillis ?: 0L,
                            dateRangePickerState.selectedEndDateMillis ?: 0L
                        )
                    },
                    // The button is only enabled when a full range is selected
                    enabled = isConfirmEnabled
                ) {
                    Text(stringResource(id = R.string.ok), style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text(stringResource(id = R.string.cancel), style = MaterialTheme.typography.labelLarge)
                }
            }
        ) {
            DateRangePicker(state = dateRangePickerState)
        }
    }
    val filteredTransactions = allTransactions.filter {
        (selectedFilter == null || it.type == selectedFilter) &&
                (searchQuery.isBlank() || it.description.contains(searchQuery, ignoreCase = true))
    }

    showDetailDialog?.let { detail ->
        TransactionDetailDialog(
            transactionDetail = detail,
            onDismissRequest = { showDetailDialog = null },
            onEditClick = { transactionId ->
                showDetailDialog = null
                navController.navigate("addEditTransaction/$transactionId")
            },
            onDeleteClick = {
                showDetailDialog = null
                showDeleteConfirmation = detail.transaction
            }
        )
    }

    showDeleteConfirmation?.let { transactionToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text(stringResource(R.string.delete_transaction_title), style = MaterialTheme.typography.headlineSmall) },
            text = { Text(stringResource(R.string.delete_transaction_message), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteTransaction(transactionToDelete)
                    showDeleteConfirmation = null
                }) { Text(stringResource(R.string.delete), style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation = null }) { Text(stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge) }
            }
        )
    }

    val isPremium = true

    Scaffold { padding ->
                   Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.screenPadding)
            ) {
                Header(
                    onFilterClick = { currentSheet = SheetContent.Filter },
                    onBackClick = { navController.popBackStack() }
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
                FilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }

                )
                if (filteredTransactions.isEmpty()) {
                    EmptyStateAnimation(
                        title = stringResource(R.string.no_transactions_title),
                        subtitle = if (searchQuery.isNotBlank() || selectedFilter != null)
                            "No transactions match your search criteria."
                        else
                            null,
                        lottieResourceId = R.raw.transactions
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f), // Use weight to make the list scrollable
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        items(filteredTransactions) { transaction ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(filteredTransactions.indexOf(transaction) * 100L)
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = Motion.Animation.ListItemEntrance) + slideInVertically(animationSpec = tween(250, easing = LinearOutSlowInEasing))
                            ) {
                                val transactionDetail = DashboardViewModel.TransactionDetail(
                                    transaction = transaction,
                                    category = categories.find { it.categoryId == transaction.categoryId },
                                    account = accounts.find { it.accountId == transaction.accountId }
                                )
                                RecentTransactionItem(
                                    transactionDetail = transactionDetail,
                                    onClick = { showDetailDialog = transactionDetail },
                                    currency = currency,
                                    allAccounts = accounts
                                )
                            }
                        }
                    }
                }
                PremiumAwareBannerAd(adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID, isPremium = isPremium)
            }
        }
    }

    @Composable
    fun Header(onFilterClick: () -> Unit, onBackClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = stringResource(R.string.all_transactions),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onFilterClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_list_alt_white_24dp),
                    contentDescription = stringResource(R.string.filter),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.screenPadding), // Removed background modifier from here
        placeholder = { Text(stringResource(R.string.search_transactions_placeholder)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
        // --- START: MODIFIED COLORS ---
        shape = MaterialTheme.shapes.medium, // Added shape for rounded corners
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            // Set the background color to match transaction cards
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
        // --- END: MODIFIED COLORS ---
    )
}

@Composable
fun FilterChips(
    selectedFilter: TransactionType?,
    onFilterSelected: (TransactionType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
    ) {
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
                            label = { Text(stringResource(R.string.all), style = MaterialTheme.typography.labelLarge) }
        )
        FilterChip(
            selected = selectedFilter == TransactionType.INCOME,
            onClick = { onFilterSelected(TransactionType.INCOME) },
            label = { Text(stringResource(R.string.income), style = MaterialTheme.typography.labelLarge) }
        )
        FilterChip(
            selected = selectedFilter == TransactionType.EXPENSE,
            onClick = { onFilterSelected(TransactionType.EXPENSE) },
            label = { Text(stringResource(R.string.expenses), style = MaterialTheme.typography.labelLarge) }
        )
        FilterChip(
            selected = selectedFilter == TransactionType.TRANSFER,
            onClick = { onFilterSelected(TransactionType.TRANSFER) },
            label = { Text(stringResource(R.string.transfers), style = MaterialTheme.typography.labelLarge) }
        )
    }
}
