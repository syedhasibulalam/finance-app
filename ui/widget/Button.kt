package com.achievemeaalk.freedjf.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

@Composable
fun Button(
    text: String,
    onClick: Action,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .background(GlanceTheme.colors.primary)
            .cornerRadius(16.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick)
    ) {
        Text(text = text, style = TextStyle(color = GlanceTheme.colors.onPrimary))
    }
}
