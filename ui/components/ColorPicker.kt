package com.achievemeaalk.freedjf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.achievemeaalk.freedjf.ui.theme.CategoryColors
import com.achievemeaalk.freedjf.ui.theme.Dimensions

@Composable
fun ColorPicker(
    onColorSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = Dimensions.avatarSizeExtraLarge + Dimensions.spacingSmall),
                contentPadding = PaddingValues(Dimensions.spacingLarge),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
            ) {
                items(CategoryColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(Dimensions.avatarSizeLarge)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected("#" + Integer.toHexString(color.toArgb())) }
                    )
                }
            }
        }
    }
} 