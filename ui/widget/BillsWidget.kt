package com.achievemeaalk.freedjf.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.achievemeaalk.freedjf.MainActivity
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.ui.theme.ExpenseColor
import com.achievemeaalk.freedjf.ui.theme.IncomeColor
import dagger.hilt.EntryPoints

class BillsWidget : GlanceAppWidget() {
    private val TAG = "WIDGET_DEBUG"

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val viewModel = EntryPoints.get(context.applicationContext, WidgetViewModel.Provider::class.java).widgetViewModel()

        provideContent {
            val state = viewModel.state.collectAsState().value
            GlanceTheme(colors = AppWidgetGlanceColorScheme.colors) {
                Content(context = context, state = state, viewModel = viewModel)
            }
        }
    }

    @Composable
    private fun Content(context: Context, state: WidgetState, viewModel: WidgetViewModel) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .wrapContentHeight()
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .padding(16.dp)
                .cornerRadius(16.dp)
                .clickable(
                    if (state.isPremium) {
                        BillsWidgetReceiver.getOpenAppIntent(context)
                    } else {
                        actionStartActivity(
                            Intent(context, MainActivity::class.java).apply {
                                putExtra("destination", "paywall")
                            }
                        )
                    }
                )
        ) {
            Text(
                text = context.getString(R.string.upcoming_bills),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
            Spacer(modifier = GlanceModifier.height(12.dp))
            if (state.isPremium) {
                if (state.bills.isEmpty()) {
                    EmptyState(context)
                } else {
                    Column {
                        state.bills.forEachIndexed { index, bill ->
                            BillItem(bill = bill, context = context)
                            if (index < state.bills.size - 1) {
                                Spacer(modifier = GlanceModifier.height(8.dp))
                            }
                        }
                    }
                }
            } else {
                PremiumState(context)
            }
        }
    }

    @Composable
    private fun PremiumState(context: Context) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.subscription),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Button(
                text = context.getString(R.string.manage_in_play_store_button),
                onClick = actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("destination", "paywall")
                    }
                )
            )
        }
    }

    @Composable
    private fun BillItem(bill: Bill, context: Context) {
        val statusColor = if (bill.isOverdue) ColorProvider(ExpenseColor) else ColorProvider(IncomeColor)
        val secondaryTextColor = if (bill.isOverdue) ColorProvider(ExpenseColor) else GlanceTheme.colors.onSurfaceVariant
        val icon = if (bill.isOverdue) R.drawable.ic_warning else R.drawable.ic_calendar_today

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(12.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = bill.name,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        provider = ImageProvider(icon),
                        contentDescription = if (bill.isOverdue) context.getString(R.string.overdue) else context.getString(R.string.due_date),
                        modifier = GlanceModifier.size(14.dp),
                        colorFilter = ColorFilter.tint(secondaryTextColor)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        text = "${context.getString(R.string.due_prefix)}${bill.dueDate}",
                        style = TextStyle(color = secondaryTextColor)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = bill.amount,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Box(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(16.dp)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable(
                            actionSendBroadcast(
                                BillsWidgetReceiver.getMarkAsPaidIntent(context, bill.id)
                            )
                        )
                ) {
                    Text(context.getString(R.string.mark_paid), style = TextStyle(color = GlanceTheme.colors.onPrimary, fontWeight = FontWeight.Bold))
                }
            }
        }
    }

    @Composable
    private fun EmptyState(context: Context) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.no_upcoming_bills_widget),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
        }
    }
}
