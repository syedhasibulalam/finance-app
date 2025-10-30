package com.achievemeaalk.freedjf.ui.dashboard

import android.app.Activity
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.BuildConfig
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.ui.components.AnimatedOutlinedButton
import com.achievemeaalk.freedjf.ui.components.AnimatedPrimaryButton
import com.achievemeaalk.freedjf.ui.components.PremiumAwareBannerAd
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.theme.*
import com.achievemeaalk.freedjf.util.IconProvider
import com.achievemeaalk.freedjf.util.formatCurrency
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.canopas.lib.showcase.IntroShowcaseScope
import com.canopas.lib.showcase.component.ShowcaseShape
import com.canopas.lib.showcase.component.ShowcaseStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.roundToInt


@Composable
fun IntroShowcaseScope.DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel,
    language: String
) {
    val state by viewModel.dashboardState.collectAsState()
  val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currency by settingsViewModel.currency.collectAsState()
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var isGreetingAnimated by remember { mutableStateOf(false) }
    var isBalanceCardAnimated by remember { mutableStateOf(false) }
    var isIncomeSpentAnimated by remember { mutableStateOf(false) }
    var isSpendingChartAnimated by remember { mutableStateOf(false) }
    var isUpcomingBillsAnimated by remember { mutableStateOf(false) }
    var isRecentTransactionsAnimated by remember { mutableStateOf(false) }

    var showDetailDialog by remember { mutableStateOf<DashboardViewModel.TransactionDetail?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<MyFinTransaction?>(null) }
    var showInsightsPopup by remember { mutableStateOf(false) }
    var insightsIconCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val showInsightsBadge by viewModel.showInsightsBadge.collectAsState()



    val isBalanceVisible by viewModel.isBalanceVisible.collectAsState()
    val isIncomeSpentVisible by viewModel.isIncomeSpentVisible.collectAsState()
    val isSpendingChartVisible by viewModel.isSpendingChartVisible.collectAsState()
    val isUpcomingBillsVisible by viewModel.isUpcomingBillsVisible.collectAsState()
    val isRecentTransactionsVisible by viewModel.isRecentTransactionsVisible.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(state.recentTransactions) {
        delay(200)
        isGreetingAnimated = true
        delay(100)
        isBalanceCardAnimated = true
        delay(100)
        isIncomeSpentAnimated = true
        delay(100)
        isSpendingChartAnimated = true
        delay(100)
        isUpcomingBillsAnimated = true
        delay(100)
        isRecentTransactionsAnimated = true
    }

    LaunchedEffect(state.financialInsights.isNotEmpty()) {
        if (state.financialInsights.isEmpty()) {
            showInsightsPopup = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkAndTriggerInAppReview(context as Activity)
    }

    LaunchedEffect(language) {
        // This will trigger a recomposition when the language changes
    }

    val rectangularShowcaseStyle = ShowcaseStyle.Default.copy(
        showcaseShape = ShowcaseShape.RECTANGLE(roundCorner = Dimensions.cornerRadiusLarge),
    )

    val verticalMargin = with(LocalDensity.current) { Dimensions.spacingSmall.toPx() }.roundToInt()

    val popupPositionProvider = remember(insightsIconCoordinates) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val iconBounds = insightsIconCoordinates?.boundsInWindow() ?: return IntOffset(0, 0)
                val x = iconBounds.left.roundToInt()
                val y = iconBounds.bottom.roundToInt() + verticalMargin
                return IntOffset(x, y)
            }
        }
    }

    AnimatedVisibility(
        visible = showInsightsPopup && insightsIconCoordinates != null,
        enter = fadeIn(animationSpec = Motion.Animation.ElementEntrance) + slideInVertically(animationSpec = tween(300))
    ) {
        Popup(
            popupPositionProvider = popupPositionProvider,
            onDismissRequest = {
                showInsightsPopup = false
                viewModel.acknowledgeInsights()
            },
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .clip(RoundedCornerShape(Dimensions.cornerRadiusLarge))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .padding(Dimensions.cardPadding)
            ) {
                if (state.financialInsights.isNotEmpty()) {
                    state.financialInsights.forEach { insight ->
                        FinancialInsightCard(
                            insight = insight,
                            containerColor = Color.Transparent,
                            titleColor = MaterialTheme.colorScheme.onSurface,
                            messageColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            iconColor = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_new_insights),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(Dimensions.spacingMedium)
                    )
                }
            }
        }
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
            title = { Text(stringResource(R.string.delete_transaction_title)) },
            text = { Text(stringResource(R.string.delete_transaction_message)) },
            confirmButton = {
                AnimatedPrimaryButton(onClick = {
                    viewModel.deleteTransaction(transactionToDelete)
                    showDeleteConfirmation = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                AnimatedOutlinedButton(onClick = { showDeleteConfirmation = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(horizontal = Dimensions.screenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            AnimatedVisibility(
                visible = isGreetingAnimated,
                enter = slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { -it } + fadeIn()
            ) {
                GreetingSection(
                    name = userName,
                    onSettingsClick = { navController.navigate("settings") },
                    insights = state.financialInsights,
                    showInsightsBadge = showInsightsBadge,
                    onInsightsClick = { showInsightsPopup = true },
                    onInsightsIconPositioned = { coordinates -> insightsIconCoordinates = coordinates }
                )
            }

            if (isBalanceVisible) {
                AnimatedVisibility(
                    visible = isBalanceCardAnimated,
                    enter = slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it / 2 } + fadeIn()
                ) {
                    TotalBalanceCard(
                        totalBalance = state.totalBalance,
                        currencyCode = currency,
                        modifier = Modifier.introShowCaseTarget(
                            index = 1,
                            style = rectangularShowcaseStyle,
                            content = {
                                Column {
                                    Text(stringResource(R.string.total_balance),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface)
                                    Text(stringResource(R.string.total_balance_tooltip),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        )
                    )
                }
            }

            if (isIncomeSpentVisible) {
                AnimatedVisibility(
                    visible = isIncomeSpentAnimated,
                    enter = slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it / 2 } + fadeIn()
                ) {
                    IncomeAndSpentSection(
                        totalIncome = state.totalIncome,
                        totalSpent = state.totalSpent,
                        currencyCode = currency
                    )
                }
            }

            if (isSpendingChartVisible) {
                AnimatedVisibility(
                    visible = isSpendingChartAnimated,
                    enter = slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it / 2 } + fadeIn()
                ) {
                    SpendingByCategoryCard(
                        spendingByCategory = state.spendingByCategory,
                        currencyCode = currency,
                        selectedTimeFilter = selectedTimeFilter,
                        onTimeFilterSelected = { viewModel.selectTimeFilter(it) },
                        modifier = Modifier.introShowCaseTarget(
                            index = 3,
                            style = rectangularShowcaseStyle,
                            content = {
                                Column {
                                    Text(stringResource(R.string.spending_by_category),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface)
                                    Text(stringResource(R.string.spending_by_category_tooltip),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        ),
                        navController = navController
                    )
                }
            }

            if (isUpcomingBillsVisible) {
                AnimatedVisibility(
                    visible = isUpcomingBillsAnimated,
                    enter = slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it / 2 } + fadeIn()
                ) {
                    UpcomingBillsCard(
                        upcomingBillsSummary = state.upcomingBillsSummary,
                        currency = currency,
                        onViewAllClick = { navController.navigate("recurringBills") }
                    )
                }
            }


            if (isRecentTransactionsVisible) {
                AnimatedVisibility(
                    visible = isRecentTransactionsAnimated,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessVeryLow))
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.recent_transactions), style = MaterialTheme.typography.titleLarge)
                            AnimatedOutlinedButton(
                                onClick = { navController.navigate("allTransactions") },
                                shape = RoundedCornerShape(Dimensions.cornerRadiusFull),
                                border = BorderStroke(Dimensions.borderWidth, DisabledBorder)
                            ) {
                                Text(stringResource(R.string.view_all), style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        if (state.recentTransactions.isEmpty()) {
                            EmptyStateAnimation(
                                title = stringResource(R.string.no_transactions_title),
                                lottieResourceId = R.raw.transactions
                            )
                        } else {
                            state.recentTransactions.forEach { transactionDetail ->
                                RecentTransactionItem(
                                    transactionDetail = transactionDetail,
                                    onClick = { showDetailDialog = transactionDetail },
                                    currency = currency,
                                    allAccounts = state.accounts
                                )
                                Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                            }
                        }
                    }
                }
            }

            // Add the banner ad here (premium-aware)
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
            PremiumAwareBannerAd(adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID, isPremium = true)
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
        }
    }
}

@Composable
fun EmptyStateAnimation(
    title: String,
    subtitle: String? = null,
    @RawRes lottieResourceId: Int
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResourceId))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingHuge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(Dimensions.emptyStateImageSize)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimensions.screenPadding)
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------------

@Composable
fun IntroShowcaseScope.GreetingSection(
    name: String,
    onSettingsClick: () -> Unit,
    insights: List<FinancialInsight>,
    showInsightsBadge: Boolean,
    onInsightsClick: () -> Unit,
    onInsightsIconPositioned: (LayoutCoordinates) -> Unit
) {
    val greeting = getGreeting()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimensions.spacingLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onInsightsClick
                    )
                    .onGloballyPositioned { coordinates -> onInsightsIconPositioned(coordinates) }
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.notification))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(Dimensions.iconSizeHuge)
                )
                if (showInsightsBadge) {
                    Badge(modifier = Modifier.align(Alignment.TopEnd)) {
                        Text(text = insights.size.toString())
                    }
                }
            }
            Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
            Column(verticalArrangement = Arrangement.Center) {
                Text(text = greeting, style = MaterialTheme.typography.titleLarge)
                Text(text = name, style = MaterialTheme.typography.bodyLarge)
            }
        }
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(Dimensions.iconSizeHuge)
                .introShowCaseTarget(
                    index = 2,
                    content = {
                        Column {
                            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
                            Text(stringResource(R.string.settings_tooltip), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                )
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            interactionSource = interactionSource
        ) {
            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_content_description))
        }
    }
}

// ------------------------------------------------------------------------------------------------------

@Composable
fun AnimatedCounterText(
    targetValue: Double,
    currencyCode: String,
    style: androidx.compose.ui.text.TextStyle,
    prefix: String = "",
    color: Color = Color.Unspecified
) {
    val animatedValue by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "counterAnimation"
    )

    Text(
        text = prefix + formatCurrency(animatedValue.toDouble(), currencyCode),
        style = style,
        color = color
    )
}

@Composable
fun TotalBalanceCard(totalBalance: Double, currencyCode: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimensions.spacingLarge)
    ) {
        Text(
            text = stringResource(R.string.total_balance),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnimatedCounterText(
            targetValue = totalBalance,
            currencyCode = currencyCode,
            style = MaterialTheme.typography.displayMediumBold
        )
    }
}

// ------------------------------------------------------------------------------------------------------

@Composable
fun IncomeAndSpentSection(
    totalIncome: Double,
    totalSpent: Double,
    currencyCode: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
    ) {
        InfoCard(
            title = stringResource(R.string.total_income),
            amount = totalIncome,
            color = IncomeColor,
            icon = Icons.Default.ArrowUpward,
            modifier = Modifier
                .weight(1f),
            currencyCode = currencyCode
        )
        InfoCard(
            title = stringResource(R.string.total_spent),
            amount = totalSpent,
            color = ExpenseColor,
            icon = Icons.Default.ArrowDownward,
            modifier = Modifier.weight(1f),
            currencyCode = currencyCode,
            isExpense = true
        )
    }
}

// ------------------------------------------------------------------------------------------------------

@Composable
fun SpendingByCategoryCard(
    spendingByCategory: List<CategorySpending>,
    currencyCode: String,
    selectedTimeFilter: TimeFilter,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val spendingText = stringResource(R.string.spending_by_category)
    Log.d("DashboardScreen", "Spending by category text: $spendingText")
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                Text(
                    text = stringResource(R.string.spending_by_category),
                    style = MaterialTheme.typography.titleMedium
                )
                TimeFilterDropdown(
                    selectedTimeFilter = selectedTimeFilter,
                    onTimeFilterSelected = onTimeFilterSelected
                )
            }
            Spacer(Modifier.height(Dimensions.spacingLarge))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.chartSize),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    spendingByCategory = spendingByCategory,
                    onCategoryClick = { category ->
                        navController.navigate("allTransactions?categoryId=${category.categoryId}")
                    }
                )
            }

            Spacer(Modifier.height(Dimensions.spacingLarge))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                spendingByCategory.take(4).forEach { spending ->
                    LegendItem(
                        color = Color(android.graphics.Color.parseColor(spending.categoryColor)),
                        text = spending.categoryName,
                        amount = spending.amount,
                        currencyCode = currencyCode
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterDropdown(
    selectedTimeFilter: TimeFilter,
    onTimeFilterSelected: (TimeFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(),
            shape = RoundedCornerShape(Dimensions.cornerRadiusFull), // Matches Manage Bills
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(Dimensions.borderWidth, DisabledBorder)
        ) {
            Text(
                text = selectedTimeFilter.name.replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.labelLarge
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.iconSizeSmall),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TimeFilter.values().forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.name.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.labelLarge) },
                    onClick = {
                        onTimeFilterSelected(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}


// ------------------------------------------------------------------------------------------------------

@Composable
fun InfoCard(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    currencyCode: String,
    isExpense: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(Dimensions.iconSizeSmall)
                )
                Spacer(Modifier.width(Dimensions.spacingExtraSmall))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedCounterText(
                targetValue = amount,
                currencyCode = currencyCode,
                style = MaterialTheme.typography.titleLargeBold,
                prefix = if (isExpense) "-" else "+",
                color = if (isExpense) OnExpenseColor else OnIncomeColor
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------------

@Composable
fun DonutChart(
    spendingByCategory: List<CategorySpending>,
    onCategoryClick: (CategorySpending) -> Unit
) {
    val neutralColor = AppTheme.colors.neutral
    val animatables = remember(spendingByCategory) {
        spendingByCategory.map { Animatable(0f) }
    }

    LaunchedEffect(spendingByCategory) {
        animatables.forEachIndexed { index, animatable ->
            launch {
                delay(index * 150L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 700)
                )
            }
        }
    }
    Canvas(
        modifier = Modifier
            .size(Dimensions.chartSize)
            .pointerInput(spendingByCategory) {
                detectTapGestures { offset ->
                    val chartSize = size.width
                    val centerX = chartSize / 2f
                    val centerY = chartSize / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    val angle = (atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()
                    val normalizedAngle = (angle + 450) % 360

                    val total = spendingByCategory.sumOf { it.amount }
                    var currentAngle = 0f
                    for (spending in spendingByCategory) {
                        val sweepAngle = (spending.amount / total * 360).toFloat()
                        val endAngle = currentAngle + sweepAngle
                        if (normalizedAngle in currentAngle..endAngle) {
                            onCategoryClick(spending)
                            break
                        }
                        currentAngle = endAngle
                    }
                }
            }
    ) {
        val total = spendingByCategory.sumOf { it.amount }
        if (total == 0.0) {
            drawArc(
                color = neutralColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = Dimensions.donutChartStrokeWidth.toPx(), cap = StrokeCap.Round)
            )
        } else {
            var startAngle = -90f
            spendingByCategory.forEachIndexed { index, spending ->
                val sweepAngle = (spending.amount / total * 360).toFloat()
                val color = Color(android.graphics.Color.parseColor(spending.categoryColor))
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * animatables[index].value,
                    useCenter = false,
                    style = Stroke(width = Dimensions.donutChartStrokeWidth.toPx(), cap = StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------------

@Composable
fun LegendItem(color: Color, text: String, amount: Double, currencyCode: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(Dimensions.spacingMedium)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(Dimensions.spacingSmall))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatCurrency(amount, currencyCode),
            style = MaterialTheme.typography.bodyMediumSemiBold
        )
    }
}

// ------------------------------------------------------------------------------------------------------

@Composable
fun RecentTransactionItem(
    transactionDetail: DashboardViewModel.TransactionDetail,
    onClick: () -> Unit,
    currency: String,
    allAccounts: List<Account>
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

    val transaction = transactionDetail.transaction
    val category = transactionDetail.category
    val account = transactionDetail.account
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val isTransfer = transaction.type == TransactionType.TRANSFER

    val destinationAccount = if (isTransfer) {
        allAccounts.find { it.accountId == transaction.destinationAccountId }
    } else {
        null
    }

    val fallbackColor = if (isTransfer) TransferColor else MaterialTheme.colorScheme.secondaryContainer
    val backgroundColor = remember(category?.color, fallbackColor, isTransfer) {
        try {
            if (isTransfer) {
                TransferColor
            } else {
                category?.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: fallbackColor
            }
        } catch (e: Exception) {
            fallbackColor
        }
    }
    val iconTint = if (backgroundColor.luminance() > 0.5f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.avatarSizeLarge)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (isTransfer) rememberVectorPainter(Icons.Default.Sync) else IconProvider.getIconPainter(category?.icon ?: ""),
                    contentDescription = if (isTransfer) stringResource(R.string.transaction_type_transfer) else category?.name,
                    tint = iconTint,
                    modifier = Modifier.size(Dimensions.iconSizeMedium)
                )
            }
            Spacer(Modifier.width(Dimensions.spacingLarge))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isTransfer) stringResource(R.string.transaction_type_transfer) else category?.name ?: stringResource(R.string.uncategorized),
                    style = MaterialTheme.typography.titleMediumBold
                )
                Spacer(Modifier.height(Dimensions.spacingExtraSmall))
                Text(
                    if (isTransfer) {
                        "${account?.name ?: stringResource(R.string.unknown_account)} → ${destinationAccount?.name ?: stringResource(R.string.unknown_account)}"
                    } else {
                        account?.name ?: stringResource(R.string.unknown_account)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val (amountColor, sign) = when (transaction.type) {
                    TransactionType.INCOME -> Pair(OnIncomeColor, "+")
                    TransactionType.EXPENSE -> Pair(OnExpenseColor, "-")
                    TransactionType.TRANSFER -> Pair(MaterialTheme.colorScheme.onSurface, "")
                }
                Text(
                    text = "$sign${formatCurrency(transaction.amount, currency)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = amountColor
                )
                Spacer(Modifier.height(Dimensions.spacingExtraSmall))
                Text(
                    text = formatter.format(Date(transaction.dateTimestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------------

@Composable
fun UpcomingBillsCard(
    upcomingBillsSummary: UpcomingBillsSummary,
    currency: String,
    onViewAllClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                Text(
                    text = stringResource(R.string.upcoming_bills),
                    style = MaterialTheme.typography.titleMedium
                )

                if (upcomingBillsSummary.overdueCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = stringResource(R.string.overdue),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(Dimensions.iconSizeSmall)
                        )
                        Text(
                            text = stringResource(R.string.overdue_count, upcomingBillsSummary.overdueCount),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(Modifier.height(Dimensions.spacingLarge))
            if (upcomingBillsSummary.upcomingBills.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.spacingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.no_upcoming_bills),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.no_upcoming_bills_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                upcomingBillsSummary.upcomingBills.forEach { bill ->
                    UpcomingBillItem(
                        bill = bill,
                        currency = currency
                    )
                    if (bill != upcomingBillsSummary.upcomingBills.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Dimensions.spacingSmall),
                            color = DisabledBorder
                        )
                    }
                }
                Spacer(Modifier.height(Dimensions.spacingLarge))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.total_prefix),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(upcomingBillsSummary.totalMonthlyAmount, currency),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.height(Dimensions.spacingLarge))
            AnimatedOutlinedButton(
                onClick = onViewAllClick,
                shape = RoundedCornerShape(Dimensions.cornerRadiusFull),
                border = BorderStroke(Dimensions.borderWidth, DisabledBorder),

                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.manage_bills), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(Dimensions.spacingSmall))
                Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.manage_bills))
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------------

@Composable
fun UpcomingBillItem(
    bill: UpcomingBill,
    currency: String
) {
    val formatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    val primaryColor = MaterialTheme.colorScheme.primary

    val backgroundColor = remember(bill.categoryColor, primaryColor) {
        try {
            Color(android.graphics.Color.parseColor(bill.categoryColor))
        } catch (e: Exception) {
            primaryColor
        }
    }
    val iconTint = if (backgroundColor.luminance() > 0.5f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimensions.iconSizeExtraLarge)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = IconProvider.getIconPainter(bill.categoryIcon),
                contentDescription = bill.categoryName,
                tint = iconTint,
                modifier = Modifier.size(Dimensions.iconSizeMedium)
            )
        }
        Spacer(Modifier.width(Dimensions.spacingMedium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bill.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = bill.categoryName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(bill.amount, currency),
                style = MaterialTheme.typography.bodyLarge,
                color = if (bill.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when {
                    bill.isOverdue -> stringResource(R.string.overdue)
                    bill.daysUntilDue == 0 -> stringResource(R.string.due_today)
                    bill.daysUntilDue == 1 -> stringResource(R.string.due_tomorrow)
                    else -> formatter.format(Date(bill.dueDate))
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    bill.isOverdue -> MaterialTheme.colorScheme.error
                    bill.daysUntilDue <= 1 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
