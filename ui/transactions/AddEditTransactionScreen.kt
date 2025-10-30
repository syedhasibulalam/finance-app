// main/java/com/achievemeaalk/freedjf/ui/transactions/AddEditTransactionScreen.kt
package com.achievemeaalk.freedjf.ui.transactions

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.*
import com.achievemeaalk.freedjf.ui.accounts.AccountsViewModel
import com.achievemeaalk.freedjf.ui.categories.CategoriesViewModel
import com.achievemeaalk.freedjf.ui.components.AccountBottomSheet
import com.achievemeaalk.freedjf.ui.components.AnimatedOutlinedButton
import com.achievemeaalk.freedjf.ui.components.AnimatedPrimaryButton
import com.achievemeaalk.freedjf.ui.components.CategoryBottomSheet
import com.achievemeaalk.freedjf.ui.components.ClickableField
import com.achievemeaalk.freedjf.ui.components.SheetContent
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.theme.OnSuccess
import com.achievemeaalk.freedjf.ui.theme.Success
import com.achievemeaalk.freedjf.ui.theme.headlineSmallSemiBold
import com.achievemeaalk.freedjf.ui.theme.titleLargeBold
import com.achievemeaalk.freedjf.util.CurrencySymbolProvider
import com.achievemeaalk.freedjf.util.InputValidation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class SaveButtonState {
    Idle,
    Loading,
    Success
}

@Composable
fun AnimatedSaveButton(
    state: SaveButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (state == SaveButtonState.Success) Success else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300)
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && state == SaveButtonState.Idle,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        )
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, 90)) togetherWith
                        fadeOut(animationSpec = tween(90))
            }
        ) { targetState ->
            when (targetState) {
                SaveButtonState.Idle -> {

                    Text(text)
                }
                SaveButtonState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                SaveButtonState.Success -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = OnSuccess
                    )
                }
            }
        }
    }
}
class CurrencyAmountInputVisualTransformation(
    private val prefix: String
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val out = prefix + text.text
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offset + prefix.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= prefix.length) return 0
                return offset - prefix.length
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@Composable
fun CustomAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    showScanButton: Boolean = false,
    onScanClick: () -> Unit = {},
    isError: Boolean = false,
    errorMessage: String? = null
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        textStyle = MaterialTheme.typography.headlineSmallSemiBold.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        visualTransformation = CurrencyAmountInputVisualTransformation(prefix = "$currencySymbol "),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                    .padding(horizontal = Dimensions.screenPaddingHorizontal, vertical = Dimensions.spacingMedium)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.amount),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(Dimensions.spacingExtraSmall))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = "$currencySymbol ",
                                    style = MaterialTheme.typography.headlineSmallSemiBold
                                )
                            }
                            innerTextField()
                        }
                        if (showScanButton) {
                            Spacer(Modifier.width(Dimensions.spacingSmall))
                            IconButton(
                                onClick = onScanClick,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(Dimensions.cornerRadiusFull)
                                    )
                                    .size(Dimensions.iconSizeExtraLarge)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = stringResource(R.string.scan_receipt),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    if (isError && errorMessage != null) {
                        Spacer(Modifier.height(Dimensions.spacingExtraSmall))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    navController: NavController,
    onSave: () -> Unit,
    transactionsViewModel: TransactionsViewModel = hiltViewModel(),
    accountsViewModel: AccountsViewModel = hiltViewModel(),
    categoriesViewModel: CategoriesViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var currentSheet by remember { mutableStateOf(SheetContent.None) }
    var isSelectingFromAccount by remember { mutableStateOf(true) }
    val adViewModel = transactionsViewModel.adViewModel

    val transaction by transactionsViewModel.transaction.collectAsState()
    val accounts by accountsViewModel.accounts.collectAsState()
    val categoriesState by categoriesViewModel.categoriesState.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    val isPremium = true
    val context = LocalContext.current
    val currencySymbol = CurrencySymbolProvider.getSymbol(currency)
    val transactionType = transaction?.type ?: transactionsViewModel.transactionType?.let { TransactionType.valueOf(it) } ?: TransactionType.EXPENSE

    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var notesError by remember { mutableStateOf<String?>(null) }

    val categoriesToShow = when (transactionType) {
        TransactionType.EXPENSE -> categoriesState.expenseCategories
        TransactionType.INCOME -> categoriesState.incomeCategories
        TransactionType.TRANSFER -> emptyList()
    }

    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var selectedDestinationAccount by remember { mutableStateOf<Account?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var saveButtonState by remember { mutableStateOf(SaveButtonState.Idle) }

    val focusRequester = remember { FocusRequester() }

    val currentBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(currentBackStackEntry, categoriesToShow, accounts) {
        val newCategoryId = currentBackStackEntry?.savedStateHandle?.get<String>("new_category")
        if (newCategoryId != null) {
            val foundCategory = categoriesToShow.find { it.categoryId.toString() == newCategoryId }
            if (foundCategory != null) {
                selectedCategory = foundCategory
                Log.d("AddEditTransactionScreen", "New category ID received: $newCategoryId, pre-filled: true")
                currentBackStackEntry.savedStateHandle.remove<String>("new_category")
            } else {
                Log.d("AddEditTransactionScreen", "Category with ID $newCategoryId not found yet, categories list might be stale.")
            }
        }

        val newAccountId = currentBackStackEntry?.savedStateHandle?.get<String>("new_account_id")
        if (newAccountId != null) {
            val foundAccount = accounts.find { it.accountId.toString() == newAccountId }
            if(foundAccount != null) {
                if (transactionType == TransactionType.TRANSFER) {
                    if (isSelectingFromAccount) {
                        if (foundAccount.accountId != selectedDestinationAccount?.accountId) {
                            selectedAccount = foundAccount
                        }
                    } else {
                        if (foundAccount.accountId != selectedAccount?.accountId) {
                            selectedDestinationAccount = foundAccount
                        }
                    }
                } else {
                    selectedAccount = foundAccount
                }
                Log.d("AddEditTransactionScreen", "New account ID received: $newAccountId, pre-filled: ${foundAccount != null}")
                currentBackStackEntry.savedStateHandle.remove<String>("new_account_id")
            } else {
                Log.d("AddEditTransactionScreen", "Account with ID $newAccountId not found yet, accounts list might be stale.")
            }
        }

        val scannedAmount = currentBackStackEntry?.savedStateHandle?.get<String>("scanned_amount")
        val scannedDate = currentBackStackEntry?.savedStateHandle?.get<Long>("scanned_date")
        val scannedSeller = currentBackStackEntry?.savedStateHandle?.get<String>("scanned_seller")

        if (scannedAmount != null) {
            amount = scannedAmount
            currentBackStackEntry.savedStateHandle.remove<String>("scanned_amount")
        }
        if (scannedDate != null) {
            selectedDate = scannedDate
            currentBackStackEntry.savedStateHandle.remove<Long>("scanned_date")
        }
        if (scannedSeller != null) {
            notes = scannedSeller
            currentBackStackEntry.savedStateHandle.remove<String>("scanned_seller")
        }
    }

    LaunchedEffect(Unit) {
        if (transaction == null) {
            delay(300)
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(Unit) {
        adViewModel.preloadTransactionCompletionAd(context)
    }

    LaunchedEffect(key1 = accounts, key2 = categoriesToShow, key3 = transaction) {
        if (transaction != null) {
            amount = transaction!!.amount.toString().takeIf { it != "0.0" } ?: ""
            notes = transaction!!.description
            selectedDate = transaction!!.dateTimestamp
            selectedAccount = accounts.find { it.accountId == transaction!!.accountId }
            selectedDestinationAccount = accounts.find { it.accountId == transaction!!.destinationAccountId }
            if (transactionType != TransactionType.TRANSFER) {
                selectedCategory = categoriesToShow.find { it.categoryId == transaction!!.categoryId }
            }
        } else {
            if (accounts.isNotEmpty() && selectedAccount == null) {
                selectedAccount = accounts.firstOrNull()
            }
            if (categoriesToShow.isNotEmpty() && selectedCategory == null) {
                selectedCategory = categoriesToShow.firstOrNull()
            }
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
                        if (transactionType == TransactionType.TRANSFER) {
                            if (isSelectingFromAccount) {
                                if (account.accountId != selectedDestinationAccount?.accountId) {
                                    selectedAccount = account
                                }
                            } else {
                                if (account.accountId != selectedAccount?.accountId) {
                                    selectedDestinationAccount = account
                                }
                            }
                        } else {
                            selectedAccount = account
                        }
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                currentSheet = SheetContent.None
                            }
                        }
                    },
                    onAddNewAccount = {
                        navController.navigate("addEditAccount")
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                currentSheet = SheetContent.None
                            }
                        }
                    },
                    currencyCode = currency
                )
                SheetContent.Category -> CategoryBottomSheet(
                    categories = categoriesToShow,
                    onCategorySelected = { category ->
                        selectedCategory = category
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                currentSheet = SheetContent.None
                            }
                        }
                    },
                    onAddNewCategory = {
                        navController.navigate("categories?showBottomSheet=true&showCategorySelection=true")
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimensions.screenPaddingHorizontal)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (transaction == null) stringResource(R.string.add_transaction) else stringResource(R.string.update_transaction),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = Dimensions.spacingLarge)
            )

            CustomAmountInput(
                value = amount,
                onValueChange = {
                    amount = it
                    amountError = InputValidation.validateAmount(it).errorMessage
                },
                currencySymbol = currencySymbol,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                showScanButton = transaction == null && transactionType == TransactionType.EXPENSE,
                onScanClick = { navController.navigate("receiptScanner") },
                isError = amountError != null,
                errorMessage = amountError
            )
            Spacer(Modifier.height(Dimensions.spacingExtraLarge))

            if (transactionType != TransactionType.TRANSFER) {
                ClickableField(
                    label = stringResource(R.string.category),
                    value = selectedCategory?.name ?: "",
                    placeholder = stringResource(R.string.select_category),
                    onClick = {
                        currentSheet = SheetContent.Category
                        scope.launch { bottomSheetState.show() }
                    }
                )
                Spacer(Modifier.height(Dimensions.spacingExtraLarge))

                ClickableField(
                    label = stringResource(R.string.account),
                    value = selectedAccount?.name ?: "",
                    placeholder = stringResource(R.string.select_account),
                    onClick = {
                        currentSheet = SheetContent.Account
                        scope.launch { bottomSheetState.show() }
                    }
                )
            } else {
                ClickableField(
                    label = stringResource(R.string.from_account),
                    value = selectedAccount?.name ?: "",
                    placeholder = stringResource(R.string.select_account),
                    onClick = {
                        isSelectingFromAccount = true
                        currentSheet = SheetContent.Account
                        scope.launch { bottomSheetState.show() }
                    }
                )
                Spacer(Modifier.height(Dimensions.spacingExtraLarge))

                ClickableField(
                    label = stringResource(R.string.to_account),
                    value = selectedDestinationAccount?.name ?: "",
                    placeholder = stringResource(R.string.select_account),
                    onClick = {
                        isSelectingFromAccount = false
                        currentSheet = SheetContent.Account
                        scope.launch { bottomSheetState.show() }
                    }
                )
            }

            Spacer(Modifier.height(Dimensions.spacingExtraLarge))

            Text(stringResource(R.string.date), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Dimensions.spacingSmall))
            DateSelector(
                date = selectedDate,
                onClick = { showDatePicker = true }
            )
            Spacer(Modifier.height(Dimensions.spacingExtraLarge))

            Text(stringResource(R.string.notes), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Dimensions.spacingSmall))
            TextField(
                value = notes,
                onValueChange = {
                    notes = it
                    notesError = InputValidation.validateNotes(it).errorMessage
                },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.add_notes_optional), style = MaterialTheme.typography.bodyLarge) },
                shape = RoundedCornerShape(Dimensions.cornerRadiusLarge),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                isError = notesError != null,
                supportingText = {
                    if (notesError != null) {
                        Text(
                            text = notesError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            Spacer(Modifier.weight(1f))

            val isSaveEnabled = amount.isNotBlank() && InputValidation.parseAmount(amount) != null && notes.length <= 200

            AnimatedSaveButton(
                state = saveButtonState,
                onClick = {
                    amountError = InputValidation.validateAmount(amount).errorMessage
                    notesError = InputValidation.validateNotes(notes).errorMessage

                    if (amountError == null && notesError == null) {
                        scope.launch {
                            saveButtonState = SaveButtonState.Loading
                            try {
                                val newOrUpdatedTransaction = when (transactionType) {
                                    TransactionType.TRANSFER -> MyFinTransaction(
                                        transactionId = transaction?.transactionId ?: 0,
                                        description = notes.trim(),
                                        amount = InputValidation.parseAmount(amount)!!,
                                        type = transactionType,
                                        dateTimestamp = selectedDate,
                                        accountId = selectedAccount!!.accountId,
                                        categoryId = null,
                                        destinationAccountId = selectedDestinationAccount!!.accountId
                                    )
                                    else -> MyFinTransaction(
                                        transactionId = transaction?.transactionId ?: 0,
                                        description = notes.trim(),
                                        amount = InputValidation.parseAmount(amount)!!,
                                        type = transactionType,
                                        dateTimestamp = selectedDate,
                                        accountId = selectedAccount!!.accountId,
                                        categoryId = selectedCategory!!.categoryId
                                    )
                                }

                                if (transaction == null) {
                                    transactionsViewModel.addTransaction(newOrUpdatedTransaction)
                                    adViewModel.showTransactionCompletionAd(context)
                                } else {
                                    transactionsViewModel.updateTransaction(newOrUpdatedTransaction)
                                }
                                Log.d("AddEditTransactionScreen", "Transaction saved successfully.")
                                saveButtonState = SaveButtonState.Success
                                delay(1000)
                                onSave()
                            } catch (e: Exception) {
                                saveButtonState = SaveButtonState.Idle
                            }
                        }
                    }
                },
                enabled = isSaveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeight),
                text = if (transaction == null) stringResource(R.string.add_transaction) else stringResource(R.string.update_transaction)
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                AnimatedPrimaryButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = it
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok), style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                AnimatedOutlinedButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DateSelector(date: Long, onClick: () -> Unit) {
    val formatter = remember { SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()) }
    val dateString = formatter.format(Date(date))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(Dimensions.screenPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.date), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(Dimensions.screenPadding))
        Text(text = dateString, style = MaterialTheme.typography.bodyLarge)
    }
}