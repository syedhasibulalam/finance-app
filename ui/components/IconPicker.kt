package com.achievemeaalk.freedjf.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.util.IconProvider

@Composable
fun IconPicker(
    onIconSelected: (String) -> Unit,
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
                items(IconProvider.allIcons.keys.toList()) { name ->
                    Icon(
                        painter = IconProvider.getIconPainter(name),
                        contentDescription = null,
                        modifier = Modifier
                            .size(Dimensions.avatarSizeLarge)
                            .clickable { onIconSelected(name) }
                    )
                }
            }
        }
    }
} 