package com.achievemeaalk.freedjf.ui.recurring

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.data.model.CategoryType
import com.achievemeaalk.freedjf.data.model.RecurringTransaction
import com.achievemeaalk.freedjf.data.model.RecurrenceFrequency
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.ui.categories.CategoriesViewModel
import com.achievemeaalk.freedjf.ui.components.AccountBottomSheet
import com.achievemeaalk.freedjf.ui.components.AnimatedOutlinedButton
import com.achievemeaalk.freedjf.ui.components.AnimatedPrimaryButton
import com.achievemeaalk.freedjf.ui.components.CategoryBottomSheet
import com.achievemeaalk.freedjf.ui.components.ClickableField
import com.achievemeaalk.freedjf.ui.components.SheetContent
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.theme.ExpenseColor
import com.achievemeaalk.freedjf.ui.theme.SurfaceVariant70
import com.achievemeaalk.freedjf.util.formatCurrency
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecurringTransactionsScreen(
    navController: NavController,
    viewModel: RecurringTransactionsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    categoriesViewModel: CategoriesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    var showAddEditSheet by remember { mutableStateOf<RecurringTransaction?>(null) }
    var showDeleteDialog by remember { mutableStateOf<RecurringTransaction?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    val categoriesState by categoriesViewModel.categoriesState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 4 })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectTab(pagerState.currentPage)
    }

    LaunchedEffect(state.selectedTab) {
        if (state.selectedTab != pagerState.currentPage) {
            scope.launch {
                pagerState.animateScrollToPage(state.selectedTab)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.bills_and_subscriptions),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isEditing = false
                        showAddEditSheet = RecurringTransaction(
                            id = 0,
                            name = "",
                            amount = 0.0,
                            accountId = 0,
                            categoryId = 0,
                            nextDueDate = System.currentTimeMillis(),
                            frequency = RecurrenceFrequency.MONTHLY,
                            isActive = true,
                            isSubscription = true
                        )
                        scope.launch { bottomSheetState.show() }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_recurring_transaction))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets(Dimensions.elevationNone)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent

            ) {
                val tabs = listOf(
                    stringResource(R.string.all) to state.recurringTransactions.size,
                    stringResource(R.string.subscriptions) to state.subscriptions.size,
                    stringResource(R.string.bills) to state.bills.size,
                    stringResource(R.string.due_soon) to state.dueSoon.size
                )
                tabs.forEachIndexed { index, (title, count) ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(stringResource(R.string.transaction_count_format, title, count)) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val displayList = when (page) {
                    1 -> state.subscriptions
                    2 -> state.bills
                    3 -> state.dueSoon
                    else -> state.recurringTransactions
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (displayList.isEmpty()) {
                    EmptyStateCard(tabIndex = page)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Dimensions.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                    ) {
                        items(displayList) { recurringTransaction ->
                            RecurringTransactionItem(
                                recurringTransaction = recurringTransaction,
                                currency = currency,
                                onMarkProcessed = { viewModel.markAsProcessed(it) },
                                onEdit = {
                                    isEditing = true
                                    showAddEditSheet = it
                                    scope.launch { bottomSheetState.show() }
                                },
                                onDelete = { showDeleteDialog = it },
                                onTogglePause = { viewModel.toggleTransactionActiveState(it) },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddEditSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { showAddEditSheet = null },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = null
        ) {
            AddEditRecurringTransactionSheet(
                transaction = showAddEditSheet,
                isEditing = isEditing,
                onDismiss = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showAddEditSheet = null
                        }
                    }
                },
                onSave = { id, name, amount, accountId, frequency, isSubscription, transactionType, categoryId, nextDueDate ->
                    viewModel.addOrUpdateRecurringTransaction(
                        id = id,
                        name = name,
                        amount = amount,
                        accountId = accountId,
                        categoryId = categoryId,
                        nextDueDate = nextDueDate,
                        frequency = frequency,
                        isSubscription = isSubscription,
                        transactionType = transactionType,
                        context = navController.context
                    )
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showAddEditSheet = null
                        }
                    }
                },
                currencyCode = currency,
                expenseCategories = categoriesState.expenseCategories,
                incomeCategories = categoriesState.incomeCategories,
                accounts = categoriesState.accounts,
                navController = navController
            )
        }
    }

    showDeleteDialog?.let { recurringTransaction ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_recurring_transaction_title)) },
            text = { Text(stringResource(R.string.delete_recurring_transaction_message, recurringTransaction.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecurringTransaction(recurringTransaction)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun RecurringTransactionItem(
    recurringTransaction: RecurringTransaction,
    currency: String,
    onMarkProcessed: (RecurringTransaction) -> Unit,
    onEdit: (RecurringTransaction) -> Unit,
    onDelete: (RecurringTransaction) -> Unit,
    onTogglePause: (RecurringTransaction) -> Unit,
    viewModel: RecurringTransactionsViewModel
) {
    val isOverdue = viewModel.isOverdue(recurringTransaction.nextDueDate)
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !recurringTransaction.isActive -> SurfaceVariant70
                isOverdue -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimensions.avatarSizeLarge)
                            .clip(CircleShape)
                            .background(
                                if (recurringTransaction.isSubscription)
                                    MaterialTheme.colorScheme.primary
                                else
                                    ExpenseColor
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (recurringTransaction.isSubscription)
                                Icons.Default.Subscriptions
                            else
                                Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(Dimensions.iconSizeMedium)
                        )
                    }

                    Spacer(modifier = Modifier.width(Dimensions.spacingLarge))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = recurringTransaction.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (!recurringTransaction.isActive)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )

                            if (!recurringTransaction.isActive) {
                                Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                                Surface(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(Dimensions.cornerRadiusSmall),
                                    modifier = Modifier.padding(horizontal = Dimensions.spacingExtraSmall)
                                ) {
                                    Text(
                                        text = stringResource(R.string.paused),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(
                                            horizontal = Dimensions.spacingSmall,
                                            vertical = Dimensions.tagVerticalPaddingSmall
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${if (recurringTransaction.isSubscription) stringResource(R.string.subscription) else stringResource(R.string.bill)} • ${recurringTransaction.frequency.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (!recurringTransaction.isActive)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit)) },
                            onClick = {
                                onEdit(recurringTransaction)
                                menuExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit)) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                onDelete(recurringTransaction)
                                menuExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = formatCurrency(recurringTransaction.amount, currency),
                        style = MaterialTheme.typography.headlineSmall,
                        color = ExpenseColor
                    )
                    Text(
                        text = stringResource(R.string.due_prefix) + formatter.format(Date(recurringTransaction.nextDueDate)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isOverdue) {
                        Text(
                            text = stringResource(R.string.overdue_caps),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
                ) {
                    AnimatedOutlinedButton(
                        onClick = { onTogglePause(recurringTransaction) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (recurringTransaction.isActive)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (recurringTransaction.isActive) stringResource(R.string.pause) else stringResource(R.string.resume))
                    }

                    if (recurringTransaction.isActive) {
                        AnimatedPrimaryButton(
                            onClick = { onMarkProcessed(recurringTransaction) }
                        ) {
                            Text(stringResource(R.string.mark_paid))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(tabIndex: Int) {
    val (title, subtitle, imageRes) = when (tabIndex) {
        1 -> Triple(stringResource(R.string.no_subscriptions), stringResource(R.string.no_subscriptions_message), R.drawable.subscription)
        2 -> Triple(stringResource(R.string.no_bills), stringResource(R.string.no_bills_message), R.drawable.bill)
        3 -> Triple(stringResource(R.string.nothing_due_soon), stringResource(R.string.nothing_due_soon_message), R.drawable.times)
        else -> Triple(stringResource(R.string.no_recurring_transactions), stringResource(R.string.no_recurring_transactions_message), R.drawable.empty)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.emptyStateImageHeight)
        )
        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringTransactionSheet(
    transaction: RecurringTransaction?,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, String, Double, Int, RecurrenceFrequency, Boolean, TransactionType, Int, Long) -> Unit,
    currencyCode: String,
    expenseCategories: List<Category>,
    incomeCategories: List<Category>,
    accounts: List<Account>,
    navController: NavController
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var currentSheet by remember { mutableStateOf(SheetContent.None) }

    var name by remember(transaction) { mutableStateOf(transaction?.name ?: "") }
    val initialAmount = transaction?.amount?.takeIf { it != 0.0 }?.toString() ?: ""
    var amount by remember(transaction) { mutableStateOf(initialAmount) }
    var frequency by remember(transaction) { mutableStateOf(transaction?.frequency ?: RecurrenceFrequency.MONTHLY) }
    var isSubscription by remember(transaction) { mutableStateOf(transaction?.isSubscription != false) }
    var selectedTransactionType by remember(transaction) { mutableStateOf(transaction?.transactionType ?: TransactionType.EXPENSE) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember(transaction) { mutableStateOf((expenseCategories + incomeCategories).find { it.categoryId == transaction?.categoryId }) }
    var selectedAccount by remember(transaction) { mutableStateOf(accounts.find { it.accountId == transaction?.accountId }) }

    val focusRequester = remember { FocusRequester() }
    val categories: List<Category> = remember(expenseCategories, incomeCategories) { expenseCategories + incomeCategories }

    // Next due date selection
    var selectedNextDueDate by remember(transaction) { mutableStateOf(transaction?.nextDueDate ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    LaunchedEffect(isEditing) {
        if (!isEditing) {
            delay(300)
            focusRequester.requestFocus()
        }
    }

    if (currentSheet != SheetContent.None) {
        ModalBottomSheet(
            onDismissRequest = { currentSheet = SheetContent.None },
            sheetState = bottomSheetState,
            dragHandle = null
        ) {
            when (currentSheet) {
                SheetContent.Account -> AccountBottomSheet(
                    accounts=accounts,
                    onAccountSelected={ account ->
                        selectedAccount=account
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if(!bottomSheetState.isVisible) {
                                currentSheet=SheetContent.None
                            }
                        }
                    },
                    onAddNewAccount={
                        navController.navigate("addEditAccount")
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if(!bottomSheetState.isVisible) {
                                currentSheet=SheetContent.None
                            }
                        }
                    },
                    currencyCode = currencyCode
                )
                SheetContent.Category -> CategoryBottomSheet(
                    categories = categories,
                    onCategorySelected = { category ->
                        selectedCategory = category
                        selectedTransactionType = if (category.type == CategoryType.INCOME) TransactionType.INCOME else TransactionType.EXPENSE
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                currentSheet = SheetContent.None
                            }
                        }
                    },
                    onAddNewCategory = {
                        navController.navigate("categories?showBottomSheet=true")
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                currentSheet = SheetContent.None
                            }
                        }
                    }
                )
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier.padding(Dimensions.spacingExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
    ) {
        Text(
            text = if (isEditing) stringResource(R.string.edit_transaction) else stringResource(if (isSubscription) R.string.add_subscription else R.string.add_bill),
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            FilterChip(
                onClick = { isSubscription = true },
                label = { Text(stringResource(R.string.subscription)) },
                selected = isSubscription,
                leadingIcon = { Icon(Icons.Default.Subscriptions, contentDescription = null) }
            )
            FilterChip(
                onClick = { isSubscription = false },
                label = { Text(stringResource(R.string.bill)) },
                selected = !isSubscription,
                leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) }
            )
        }

        // Transaction type is inferred from the selected category

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name_placeholder)) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            singleLine = true,
            shape = RoundedCornerShape(Dimensions.cornerRadiusMedium),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        TextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(R.string.amount)) },
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text(text = "$currencyCode ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(Dimensions.cornerRadiusMedium),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        ClickableField(
            label = stringResource(R.string.account),
            value = selectedAccount?.name ?: "",
            placeholder = stringResource(R.string.select_account),
            onClick = {
                currentSheet = SheetContent.Account
                scope.launch { bottomSheetState.show() }
            }
        )

        ClickableField(
            label = stringResource(R.string.category),
            value = selectedCategory?.name ?: "",
            placeholder = stringResource(R.string.select_category),
            onClick = {
                currentSheet = SheetContent.Category
                scope.launch { bottomSheetState.show() }
            }
        )

        // Next Due Date selector
        ClickableField(
            label = stringResource(R.string.next_due_date),
            value = dateFormatter.format(Date(selectedNextDueDate)),
            onClick = { showDatePicker = true }
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = frequency.name,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.frequency)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.cornerRadiusMedium),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                RecurrenceFrequency.values().forEach { freq ->
                    DropdownMenuItem(
                        text = { Text(freq.name) },
                        onClick = {
                            frequency = freq
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            AnimatedOutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancel))
            }
            AnimatedPrimaryButton(
                onClick = {
                    if (selectedCategory != null && selectedAccount != null) {
                        onSave(
                            transaction?.id ?: 0,
                            name,
                            amount.toDoubleOrNull() ?: 0.0,
                            selectedAccount!!.accountId,
                            frequency,
                            isSubscription,
                            selectedTransactionType,
                            selectedCategory!!.categoryId,
                            selectedNextDueDate
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && amount.toDoubleOrNull() != null && selectedCategory != null && selectedAccount != null
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedNextDueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                AnimatedPrimaryButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val todayStart = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        selectedNextDueDate = maxOf(it, todayStart)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
