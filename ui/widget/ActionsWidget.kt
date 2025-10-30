package com.achievemeaalk.freedjf.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf // <-- Add this import
import androidx.glance.appwidget.GlanceAppWidget
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
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.ui.theme.ExpenseColor
import com.achievemeaalk.freedjf.ui.theme.IncomeColor
import com.achievemeaalk.freedjf.ui.theme.TransferColor

class ActionsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(colors = AppWidgetGlanceColorScheme.colors) {
                ActionWidgetContent(
                    expenseText = context.getString(R.string.transaction_type_expense),
                    incomeText = context.getString(R.string.transaction_type_income),
                    transferText = context.getString(R.string.transaction_type_transfer)
                )
            }
        }
    }

    @Composable
    private fun ActionWidgetContent(
        expenseText: String,
        incomeText: String,
        transferText: String
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(24.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionButton(
                text = expenseText,
                iconRes = R.drawable.ic_arrow_downward,
                color = ExpenseColor,
                transactionType = TransactionType.EXPENSE,
                modifier = GlanceModifier.defaultWeight()
            )
            ActionButton(
                text = incomeText,
                iconRes = R.drawable.ic_arrow_upward,
                color = IncomeColor,
                transactionType = TransactionType.INCOME,
                modifier = GlanceModifier.defaultWeight()
            )
            ActionButton(
                text = transferText,
                iconRes = R.drawable.ic_sync,
                color = TransferColor,
                transactionType = TransactionType.TRANSFER,
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }

    @Composable
    private fun ActionButton(
        text: String,
        iconRes: Int,
        color: androidx.compose.ui.graphics.Color,
        transactionType: TransactionType,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Column(
            modifier = modifier.clickable(
                actionStartActivity(
                    activity = MainActivity::class.java,
                    parameters = actionParametersOf(
                        ActionParameters.Key<String>(MainActivity.EXTRA_TRANSACTION_TYPE) to transactionType.name
                    )
                )
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f))
                    .cornerRadius(20.dp), // Use cornerRadius to make it a circle
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = text,
                    colorFilter = ColorFilter.tint(ColorProvider(color)),
                    modifier = GlanceModifier.size(24.dp)
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = text,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
