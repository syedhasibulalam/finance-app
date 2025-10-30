package com.achievemeaalk.freedjf.ui.budgets

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.BuildConfig
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.ui.components.AnimatedOutlinedButton
import com.achievemeaalk.freedjf.ui.components.AnimatedPrimaryButton
import com.achievemeaalk.freedjf.ui.components.PremiumAwareBannerAd
import com.achievemeaalk.freedjf.ui.onboarding.screens.CoachMark
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.theme.AppTheme
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.theme.headlineSmallBold
import com.achievemeaalk.freedjf.ui.theme.isColorDark
import com.achievemeaalk.freedjf.ui.theme.titleLargeSemiBold
import com.achievemeaalk.freedjf.util.IconProvider
import com.achievemeaalk.freedjf.util.formatCurrency
import com.canopas.lib.showcase.IntroShowcaseScope
import com.canopas.lib.showcase.component.ShowcaseShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.canopas.lib.showcase.component.ShowcaseStyle
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun IntroShowcaseScope.BudgetsScreen(
    navController: NavController,
    viewModel: BudgetsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.budgetState.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    val isPremium = true
    val recurringTotal by viewModel.recurringTotalForCategory.collectAsState()
    var showSetBudgetDialog by remember { mutableStateOf<Category?>(null) }
    var showEditBudgetDialog by remember { mutableStateOf<BudgetedCategory?>(null) }
    var showRemoveConfirmationDialog by remember { mutableStateOf<BudgetedCategory?>(null) }


    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CategoryBudgetsTab(
                state = state,
                currency = currency,
                onSetBudgetClick = { category ->
                    viewModel.calculateRecurringTotalForCategory(category.categoryId)
                    showSetBudgetDialog = category
                },
                onEditBudgetClick = { showEditBudgetDialog = it },
                onRemoveBudgetClick = { showRemoveConfirmationDialog = it },
                onPreviousMonth = { viewModel.changeMonth(-1) },
                onNextMonth = { viewModel.changeMonth(1) },
                showWalkthrough = false // Assuming walkthrough is not needed here
            )

            PremiumAwareBannerAd(adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID, isPremium = isPremium)
        }
    }

    showSetBudgetDialog?.let { category ->
        SetBudgetDialog(
            categoryName = category.name,
            initialAmount = null,
            onDismiss = {
                showSetBudgetDialog = null
                viewModel.clearRecurringTotal()
            },
            onSave = { amount, reminderEnabled ->
                viewModel.setBudgetForCategory(category.categoryId, amount, reminderEnabled)
                showSetBudgetDialog = null
                viewModel.clearRecurringTotal()
            },
            currencyCode = currency,
            recurringTotal = recurringTotal
        )
    }

    showEditBudgetDialog?.let { budgetedCategory ->
        SetBudgetDialog(
            categoryName = budgetedCategory.category.name,
            initialAmount = budgetedCategory.budgetCategory.plannedAmount,
            initialReminderEnabled = budgetedCategory.budgetCategory.reminderEnabled,
            onDismiss = { showEditBudgetDialog = null },
            onSave = { amount, reminderEnabled ->
                viewModel.updateBudgetForCategory(budgetedCategory.budgetCategory, amount, reminderEnabled)
                showEditBudgetDialog = null
            },
            currencyCode = currency,
            recurringTotal = 0.0 // Don't show suggestions when editing
        )
    }

    showRemoveConfirmationDialog?.let { budgetedCategory ->
        AlertDialog(
            onDismissRequest = { showRemoveConfirmationDialog = null },
            title = { Text(stringResource(R.string.remove_budget_limit_title)) },
            text = { Text(stringResource(R.string.remove_budget_limit_message, budgetedCategory.category.name)) },
            confirmButton = {
                AnimatedPrimaryButton(
                    onClick = {
                        viewModel.removeBudgetForCategory(budgetedCategory.budgetCategory)
                        showRemoveConfirmationDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete), style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                AnimatedOutlinedButton(onClick = { showRemoveConfirmationDialog = null }) {
                    Text(stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}

@Composable
fun IntroShowcaseScope.MonthNavigator(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM, yyyy", Locale.getDefault())
    val rectangularShowcaseStyle = ShowcaseStyle.Default.copy(
        showcaseShape = ShowcaseShape.RECTANGLE(roundCorner = Dimensions.cornerRadiusLarge),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .introShowCaseTarget(
                index = 7,
                style = rectangularShowcaseStyle,
                content = {
                    Column {
                        Text(
                            text = stringResource(R.string.month_navigator_tooltip_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text = stringResource(R.string.month_navigator_tooltip_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = stringResource(R.string.previous_month))
        }
        Text(
            text = currentMonth.format(formatter),
            style = MaterialTheme.typography.titleLargeSemiBold,
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ArrowForwardIos, contentDescription = stringResource(R.string.next_month))
        }
    }
}

@Composable
fun SuggestionCard(
    total: Double,
    currencyCode: String,
    onInclude: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onInclude
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.recurring_bills_found),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(R.string.total_prefix) + formatCurrency(total, currencyCode),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AnimatedPrimaryButton(
                onClick = onInclude,
                contentPadding = PaddingValues(horizontal = Dimensions.spacingMedium)
            ) {
                Text(stringResource(R.string.include))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetDialog(
    categoryName: String,
    initialAmount: Double? = null,
    initialReminderEnabled: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (Double, Boolean) -> Unit,
    currencyCode: String,
    recurringTotal: Double
) {
    var amount by remember { mutableStateOf(initialAmount?.let { 
        // Keep raw number formatting to avoid locale issues in input
        if (it == 0.0) "" else it.toString()
    } ?: "") }
    var reminderEnabled by remember { mutableStateOf(initialReminderEnabled) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                reminderEnabled = true
            }
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.spacingExtraLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
            ) {
                Text(
                    text = stringResource(R.string.set_budget_for_category, categoryName),
                    style = MaterialTheme.typography.headlineSmall
                )

                if (recurringTotal > 0.0) {
                    SuggestionCard(
                        total = recurringTotal,
                        currencyCode = currencyCode,
                        onInclude = {
                            amount = recurringTotal.toString()
                        }
                    )
                }

                Text(
                    text = stringResource(R.string.planned_amount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    prefix = { Text(text = "$currencyCode ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(Dimensions.cornerRadiusMedium),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    )
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.enable_budget_reminder),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(R.string.budget_reminder_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(Dimensions.spacingMedium))

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = {
                            if (it) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    reminderEnabled = true
                                }
                            } else {
                                reminderEnabled = false
                            }
                        }
                    )
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
                            onSave(amount.toDoubleOrNull()?: 0.0, reminderEnabled)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = amount.toDoubleOrNull()!= null
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
@Composable
fun BudgetedCategoryItemNew(
    item: BudgetedCategory,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit,
    currencyCode: String
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val isExceeded = item.spentAmount > item.budgetCategory.plannedAmount
    val progress = (item.spentAmount / item.budgetCategory.plannedAmount).toFloat().coerceAtMost(1f)
    val fallbackColor = MaterialTheme.colorScheme.primary
    val backgroundColor = remember(item.category.color, fallbackColor) {
        try {
            Color(android.graphics.Color.parseColor(item.category.color))
        } catch (e: Exception) {
            fallbackColor
        }
    }
    val iconTint = if (backgroundColor.isColorDark())  MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface


    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = Dimensions.spacingSmall)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(Dimensions.avatarSizeLarge)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = IconProvider.getIconPainter(item.category.icon),
                    contentDescription = item.category.name,
                    tint = iconTint,
                    modifier = Modifier.size(Dimensions.iconSizeMedium)
                )
            }
            Spacer(Modifier.width(Dimensions.spacingLarge))

            Text(
                text = item.category.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.edit_budget))
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit_limit)) },
                        onClick = {
                            onEditClick()
                            menuExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_limit)) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.remove_limit)) },
                        onClick = {
                            onRemoveClick()
                            menuExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_limit)) }
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimensions.spacingMedium))

        Row(
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.limit_prefix) + formatCurrency(item.budgetCategory.plannedAmount, currencyCode), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.spent_prefix) + formatCurrency(item.spentAmount, currencyCode), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.remaining_prefix) + formatCurrency(item.remainingAmount, currencyCode),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val formatter = DateTimeFormatter.ofPattern("MMM, yyyy", Locale.getDefault())
                Text(stringResource(R.string.monthly_recap_format, YearMonth.now().format(formatter)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Dimensions.spacingExtraSmall))
                LimitTag(amount = item.budgetCategory.plannedAmount, currencyCode = currencyCode)
            }
        }

        Spacer(Modifier.height(Dimensions.spacingMedium))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.progressBarHeight)
                .clip(CircleShape)
                .background(if (isExceeded) MaterialTheme.colorScheme.error else AppTheme.colors.neutral)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = if(isExceeded) 1f else progress)
                    .height(Dimensions.progressBarHeight)
                    .clip(CircleShape)
                    .background(if (isExceeded) MaterialTheme.colorScheme.error else AppTheme.colors.success)
            )
        }


        if (isExceeded) {
            Spacer(Modifier.height(Dimensions.spacingExtraSmall))
            Text(
                stringResource(R.string.limit_exceeded),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun LimitTag(amount: Double, currencyCode: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(
                topStart = Dimensions.cornerRadiusMedium,
                bottomStart = Dimensions.cornerRadiusMedium,
                bottomEnd = Dimensions.cornerRadiusMedium
            ))
            .padding(
                horizontal = Dimensions.spacingMedium,
                vertical = Dimensions.tagVerticalPadding
            )
    ) {
        Text(
            text = formatCurrency(amount, currencyCode),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}



@Composable
fun IntroShowcaseScope.UnbudgetedCategoryItemNew(
    category: Category,
    onSetBudgetClick: () -> Unit,
    showWalkthrough: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
            val backgroundColor = remember(category.color) {
                try {
                    Color(android.graphics.Color.parseColor(category.color))
                } catch (e: Exception) {
                    surfaceColor
                }
            }
            val iconTint = if (backgroundColor.luminance() > 0.5) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onPrimary
            }

            Box(
                modifier = Modifier
                    .size(Dimensions.avatarSizeLarge)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = IconProvider.getIconPainter(category.icon),
                    contentDescription = category.name,
                    tint = iconTint,
                    modifier = Modifier.size(Dimensions.iconSizeMedium)
                )
            }
            Spacer(Modifier.width(Dimensions.spacingLarge))
            Text(category.name, style = MaterialTheme.typography.titleLarge)
        }
        if (showWalkthrough) {
            CoachMark(stringResource(R.string.set_budget_coach_mark))
        }
        AnimatedPrimaryButton(
            onClick = onSetBudgetClick,
            modifier = Modifier.introShowCaseTarget(
                index = 6,
                content = {
                    Column {
                        Text(
                            text = stringResource(R.string.set_budget_tooltip_title),
                            style = MaterialTheme.typography.headlineSmallBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                        Text(
                            text = stringResource(R.string.set_budget_tooltip_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        ) {
            Text(stringResource(R.string.set_budget))
        }
    }
}

@Composable
fun IntroShowcaseScope.CategoryBudgetsTab(
    state: BudgetScreenState,
    currency: String,
    onSetBudgetClick: (Category) -> Unit,
    onEditBudgetClick: (BudgetedCategory) -> Unit,
    onRemoveBudgetClick: (BudgetedCategory) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    showWalkthrough: Boolean
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            item {
                MonthNavigator(
                    currentMonth = state.selectedDate,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth
                )
                Spacer(Modifier.height(Dimensions.spacingLarge))
            }

            item {
                val formatter = DateTimeFormatter.ofPattern("MMM, yyyy", Locale.getDefault())
                Text(
                    text = stringResource(R.string.budgeted_categories_title, state.selectedDate.format(formatter)),
                    style = MaterialTheme.typography.titleMedium,
                )
                HorizontalDivider(modifier = Modifier.padding(top = Dimensions.spacingSmall))
            }

            items(state.budgetedCategories) { budgetedCategory ->
                BudgetedCategoryItemNew(
                    item = budgetedCategory,
                    onEditClick = { onEditBudgetClick(budgetedCategory) },
                    onRemoveClick = { onRemoveBudgetClick(budgetedCategory) },
                    currencyCode = currency
                )
            }

            if (state.unbudgetedCategories.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(Dimensions.spacingLarge))
                    Text(
                        stringResource(R.string.not_budgeted_this_month),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = Dimensions.spacingSmall))
                }
            }

            items(state.unbudgetedCategories) { category ->
                UnbudgetedCategoryItemNew(
                    category = category,
                    onSetBudgetClick = { onSetBudgetClick(category) },
                    showWalkthrough = showWalkthrough
                )
            }
        }
    }
}

