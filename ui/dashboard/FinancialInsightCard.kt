package com.achievemeaalk.freedjf.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.theme.titleMediumBold

@Composable
fun FinancialInsightCard(
    insight: FinancialInsight,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    messageColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = insight.icon,
                contentDescription = null,
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(Dimensions.spacingLarge))
            Column {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMediumBold,
                    color = titleColor
                )
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = messageColor
                )
            }
        }
    }
}