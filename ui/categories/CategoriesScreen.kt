package com.achievemeaalk.freedjf.ui.categories

import com.achievemeaalk.freedjf.BuildConfig
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.ui.components.PremiumAwareBannerAd
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.util.IconProvider
import com.canopas.lib.showcase.IntroShowcaseScope
import com.canopas.lib.showcase.component.ShowcaseShape
import com.canopas.lib.showcase.component.ShowcaseStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroShowcaseScope.CategoriesScreen(
  viewModel: CategoriesViewModel = hiltViewModel(),
  showAddSheetInitially: Boolean = false,
  isPicker: Boolean = false,
  onCategorySelected: (String) -> Unit = {}
) {
  val categoriesState by viewModel.categoriesState.collectAsState()
  var showDeleteDialog by remember { mutableStateOf<Category?>(null) }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  var showBottomSheet by remember { mutableStateOf(showAddSheetInitially) }
  var selectedCategory by remember { mutableStateOf<Category?>(null) }

  LaunchedEffect(showAddSheetInitially) {
      if (showAddSheetInitially) {
          selectedCategory = null 
          showBottomSheet = true
      }
  }

  val isPremium = true

  Scaffold(
    floatingActionButton = {
      if (!isPicker) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          FloatingActionButton(
            onClick = {
              selectedCategory = null
              showBottomSheet = true
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.introShowCaseTarget(
              index = 8,
              style = ShowcaseStyle.Default.copy(
                showcaseShape = ShowcaseShape.CIRCLE
              ),
              content = {
                Column {
                  Text(
                    text = stringResource(R.string.add_category_tooltip_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                  )
                  Text(
                    text = stringResource(R.string.add_category_tooltip_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                  )
                }
              }
            )
          ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_category))
          }
        }
      }
    }
  ) { padding ->
    Column {
      LazyColumn(
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(Dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
      ) {
        item {
          Text(
            stringResource(R.string.expense_categories),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(
              top = Dimensions.spacingLarge,
              bottom = Dimensions.spacingSmall
            )
          )
        }
        items(categoriesState.expenseCategories) { category ->
          CategoryItem(
            category = category,
            onEdit = {
              if (isPicker) {
                onCategorySelected(category.categoryId.toString())
              } else {
                selectedCategory = category
                showBottomSheet = true
              }
            },
            onDelete = { showDeleteDialog = category }
          )
        }

        item {
          Text(
            stringResource(R.string.income_categories),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(
              top = Dimensions.spacingLarge,
              bottom = Dimensions.spacingSmall
            )
          )
        }
        items(categoriesState.incomeCategories) { category ->
          CategoryItem(
            category = category,
            onEdit = {
              if (isPicker) {
                onCategorySelected(category.categoryId.toString())
              } else {
                selectedCategory = category
                showBottomSheet = true
              }
            },
            onDelete = { showDeleteDialog = category }
          )
        }
      }
      PremiumAwareBannerAd(adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID, isPremium = isPremium)
    }
  }

  if (showBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = { showBottomSheet = false },
      sheetState = sheetState,
      containerColor = MaterialTheme.colorScheme.background,
      dragHandle = null
    ) {
      AddEditCategoryBottomSheet(
        category = selectedCategory,
        onSave = { newCategory ->
          if (isPicker) {
            onCategorySelected(newCategory)
          }
          scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
              showBottomSheet = false
            }
          }
        },
        viewModel = viewModel
      )
    }
  }

  showDeleteDialog?.let { categoryToDelete ->
    AlertDialog(
      onDismissRequest = { showDeleteDialog = null },
      title = { Text(stringResource(R.string.delete_category_title)) },
      text = { Text(stringResource(R.string.delete_category_message, categoryToDelete.name)) },
      confirmButton = {
        Button(onClick = {
          viewModel.deleteCategory(categoryToDelete)
          showDeleteDialog = null
        }) {
          Text(stringResource(R.string.delete))
        }
      },
      dismissButton = {
        Button(onClick = { showDeleteDialog = null }) {
          Text(stringResource(R.string.cancel))
        }
      }
    )
  }
}

@Composable
fun CategoryItem(category: Category, onEdit: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onEdit
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Use surface color
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            val backgroundColor = remember(category.color) {
                Color(android.graphics.Color.parseColor(category.color))
            }
            val iconTint =
                if (backgroundColor.luminance() > 0.5f) Color.Black else MaterialTheme.colorScheme.onPrimary

            Box(
                modifier = Modifier
                    .size(Dimensions.avatarSizeLarge)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = IconProvider.getIconPainter(category.icon),
                    contentDescription = category.name,
                    tint = iconTint,
                    modifier = Modifier.size(Dimensions.iconSizeMedium)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(text = category.description ?: "", style = MaterialTheme.typography.bodyMedium)
            }
            val deleteInteractionSource = remember { MutableInteractionSource() }
            val isDeletePressed by deleteInteractionSource.collectIsPressedAsState()
            val deleteScale by animateFloatAsState(
                targetValue = if (isDeletePressed) 0.95f else 1f,
                label = "deleteScale"
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = deleteScale
                        scaleY = deleteScale
                    },
                interactionSource = deleteInteractionSource
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_category_title),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun getIconResource(context: Context, iconName: String): Int {
  val resourceId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
  return if (resourceId == 0) R.drawable.ic_launcher else resourceId
}