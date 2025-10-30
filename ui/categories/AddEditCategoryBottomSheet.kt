package com.achievemeaalk.freedjf.ui.categories

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.data.model.CategoryType
import com.achievemeaalk.freedjf.ui.components.AnimatedOutlinedButton
import com.achievemeaalk.freedjf.ui.components.AnimatedPrimaryButton
import com.achievemeaalk.freedjf.ui.components.ColorPicker
import com.achievemeaalk.freedjf.ui.components.IconPicker
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.util.IconProvider
import com.achievemeaalk.freedjf.ui.components.localizedName
import com.achievemeaalk.freedjf.ui.components.localizedDescription

@Composable
fun AddEditCategoryBottomSheet(
    category: Category?,
    onSave: (String) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val localizedName = category?.localizedName() ?: ""
    val localizedDescription = category?.localizedDescription() ?: ""

    var name by remember(localizedName) { mutableStateOf(localizedName) }
    var description by remember(localizedDescription) { mutableStateOf(localizedDescription) }
    var type by remember(category) { mutableStateOf(category?.type ?: CategoryType.EXPENSE) }
    var icon by remember(category) {
        mutableStateOf(
            category?.icon ?: IconProvider.allIcons.keys.random()
        )
    }
    var color by remember(category) {
        mutableStateOf(
            category?.color ?: listOf(
                "#F44336",
                "#E91E63",
                "#9C27B0",
                "#673AB7",
                "#3F51B5",
                "#2196F3",
                "#03A9F4",
                "#00BCD4",
                "#009688",
                "#4CAF50",
                "#8BC34A",
                "#CDDC39",
                "#FFEB3B",
                "#FFC107",
                "#FF9800",
                "#FF5722",
                "#795548",
                "#9E9E9E",
                "#607D8B"
            ).random()
        )
    }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (category == null) {
            focusRequester.requestFocus()
        }
    }

    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges by remember(name, description, type, icon, color, category) {
        derivedStateOf {
            if (category == null) {
                name.isNotEmpty() || description.isNotEmpty()
            } else {
                name != localizedName ||
                        description != localizedDescription ||
                        type != category.type ||
                        icon != category.icon ||
                        color != category.color
            }
        }
    }

    if (showIconPicker) {
        IconPicker(
            onIconSelected = {
                icon = it
                showIconPicker = false
            },
            onDismissRequest = { showIconPicker = false }
        )
    }

    if (showColorPicker) {
        ColorPicker(
            onColorSelected = {
                color = it
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false }
        )
    }

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                AnimatedPrimaryButton(onClick = {
                    showUnsavedChangesDialog = false
                    onSave("")
                }) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                AnimatedOutlinedButton(onClick = { showUnsavedChangesDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedChangesDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .background(MaterialTheme.colorScheme.surface)
            .padding(Dimensions.screenPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (category == null) stringResource(R.string.add_category) else stringResource(R.string.edit_category),
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
            IconButton(onClick = {
                if (hasUnsavedChanges) {
                    showUnsavedChangesDialog = true
                } else {
                    onSave("")
                }
            },
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                interactionSource = interactionSource
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        }
        Spacer(Modifier.height(Dimensions.spacingLarge))

        // Category Name
        Text(stringResource(R.string.category_name), style = MaterialTheme.typography.titleMedium)
        TextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text(stringResource(R.string.category_name)) },
            singleLine = true,
            shape = RoundedCornerShape(Dimensions.cornerRadiusLarge),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background
            )
        )
        Spacer(Modifier.height(Dimensions.spacingMedium))

        // Description
        Text(stringResource(R.string.description), style = MaterialTheme.typography.titleMedium)
        TextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = { Text(stringResource(R.string.description)) },
            shape = RoundedCornerShape(Dimensions.cornerRadiusLarge),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background
            )
        )
        Spacer(Modifier.height(Dimensions.spacingMedium))

        // Category Type
        Text(stringResource(R.string.category), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)) {
            TypeChip(
                text = stringResource(R.string.transaction_type_expense),
                isSelected = type == CategoryType.EXPENSE,
                onClick = { type = CategoryType.EXPENSE }
            )
            TypeChip(
                text = stringResource(R.string.transaction_type_income),
                isSelected = type == CategoryType.INCOME,
                onClick = { type = CategoryType.INCOME }
            )
        }
        Spacer(Modifier.height(Dimensions.spacingLarge))

        // Icon & Color Pickers
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.select_icon), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Dimensions.spacingSmall))
                PickerSelector(
                    text = if (icon.isNotBlank()) stringResource(R.string.selected) else stringResource(
                        R.string.tap_to_select
                    ),
                    onClick = { showIconPicker = true },
                    leadingIcon = {
                        Icon(
                            painter = IconProvider.getIconPainter(icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimensions.iconSizeLarge)
                        )
                    }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.select_color), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Dimensions.spacingSmall))
                PickerSelector(
                    text = if (color.isNotBlank()) stringResource(R.string.selected) else stringResource(
                        R.string.tap_to_select
                    ),
                    onClick = { showColorPicker = true },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(Dimensions.iconSizeLarge)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                        )
                    }
                )
            }
        }

        Spacer(Modifier.weight(1f)) // Pushes button to the bottom

        // Save Button
        AnimatedPrimaryButton(
            onClick = {
                val newOrUpdatedCategory = category?.copy(
                    name = name,
                    description = description,
                    type = type,
                    icon = icon,
                    color = color
                ) ?: Category(
                    name = name,
                    description = description,
                    type = type,
                    icon = icon,
                    color = color,
                    nameResId = null,
                    descriptionResId = null
                )

                if (category == null) {
                    viewModel.addCategory(newOrUpdatedCategory, context) { newCategoryId ->
                        onSave(newCategoryId)
                    }
                } else {
                    viewModel.updateCategory(newOrUpdatedCategory) { updatedCategoryId ->
                        onSave(updatedCategoryId)
                    }
                }
            },
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.buttonHeight),
            shape = RoundedCornerShape(Dimensions.cornerRadiusExtraLarge)
        ) {
            Text(
                text = stringResource(R.string.save),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun TypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(Dimensions.cornerRadiusLarge))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = Dimensions.spacingExtraLarge,
                vertical = Dimensions.spacingMedium
            )
    ) {
        Text(text = text, color = contentColor, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PickerSelector(
    text: String,
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(Dimensions.cornerRadiusLarge),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimensions.elevationSmall
        )
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = Dimensions.spacingLarge,
                    vertical = Dimensions.spacingLarge
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            leadingIcon()
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                softWrap = false,
                // **THIS IS THE FIX**
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}