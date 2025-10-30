package com.achievemeaalk.freedjf.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.achievemeaalk.freedjf.ui.theme.Dimensions

@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf(
        "😀", "💳", "📄", "💼", "S", "🧾", "🪙", "🐷", "🏦", "🏛️",
        "🏠", "🏢", "🏭", "🏰", "🏟️", "🏞️", "🏜️", "🏝️", "🏕️", "🛖",
        "🚗", "🚕", "✈️", "🚀", "⛵", "🛳️", "⚓", "⛽", "🚧", "🚦",
        "🍔", "🍕", "🍟", "🍿", "🥐", "🍩", "🎂", "☕", "🥂", "🍻",
        "⚽", "🏀", "🏈", "⚾", "🎾", "🏆", "🎁", "🎉", "💻", "📱",
        "⌚", "📷", "💡", "💰", "🛒", "💊", "🔨", "📈", "📉", "❤️"
    )
    val emojiRows = emojis.chunked(20)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
    ) {
        emojiRows.forEach { row ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                contentPadding = PaddingValues(horizontal = Dimensions.spacingSmall)
            ) {
                items(row) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(Dimensions.iconSizeHuge)
                            .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
        }
    }
} 