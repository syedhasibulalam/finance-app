package com.achievemeaalk.freedjf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.achievemeaalk.freedjf.ui.theme.Dimensions

@Composable
fun SegmentedControl(
    tabs: List<String>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingSmall),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        TabRow(
            selectedTabIndex = selectedIndex,
            indicator = { },
            divider = { },
            modifier = Modifier.padding(Dimensions.spacingExtraSmall)
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedIndex == index
                Tab(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                    selected = selected,
                    onClick = { onTabClick(index) },
                    text = {
                        Text(
                            text = title,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = Dimensions.spacingSmall, vertical = Dimensions.spacingMedium)
                        )
                    }
                )
            }
        }
    }
}
