// main/java/com/achievemeaalk/freedjf/ui/components/SelectionBottomSheet.kt

package com.achievemeaalk.freedjf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.theme.bodyLargeSemiBold
import com.achievemeaalk.freedjf.ui.theme.bodyMediumSemiBold
import com.achievemeaalk.freedjf.util.IconProvider
import com.achievemeaalk.freedjf.util.formatCurrency

enum class SheetContent {
    None, Account, Category, Filter
}

@Composable
fun ClickableField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    onClick: () -> Unit
) {
    Column(modifier = modifier.padding(vertical = Dimensions.spacingSmall)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(vertical = Dimensions.screenPadding, horizontal = Dimensions.spacingMedium)
        ) {
            Text(
                text = value.ifEmpty { placeholder ?: stringResource(R.string.select_category) },
                color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AccountBottomSheet(
    accounts: List<Account>,
    onAccountSelected: (Account) -> Unit,
    onAddNewAccount: () -> Unit,
    currencyCode: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(Dimensions.screenPadding)
    ) {
        Text(stringResource(R.string.select_account), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(Dimensions.screenPadding))
        LazyColumn {
            items(accounts) { account ->
                AccountListItem(account = account, currencyCode = currencyCode) {
                    onAccountSelected(account)
                }
            }
            item {
                AddNewItem {
                    onAddNewAccount()
                }
            }
        }
    }
}

@Composable
fun AccountListItem(account: Account, currencyCode: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingExtraSmall)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.avatarSizeMedium)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = IconProvider.getIconPainter(account.icon),
                    contentDescription = account.name,
                    modifier = Modifier.size(Dimensions.iconSizeMedium),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(Dimensions.spacingLarge))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = account.accountType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatCurrency(account.balance, currencyCode),
                style = MaterialTheme.typography.bodyLargeSemiBold
            )
        }
    }
}

@Composable
fun CategoryBottomSheet(
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    onAddNewCategory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(Dimensions.screenPadding)
    ) {
        Text(stringResource(R.string.select_category), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(Dimensions.screenPadding))
        LazyColumn {
            items(categories) { category ->
                CategoryListItem(category = category) {
                    onCategorySelected(category)
                }
            }
            item {
                AddNewItem(isAccount = false) {
                    onAddNewCategory()
                }
            }
        }
    }
}

@Composable
fun CategoryListItem(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingExtraSmall)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            val backgroundColor = remember(category.color) {
                Color(android.graphics.Color.parseColor(category.color))
            }
            val iconTint = if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White

            Box(
                modifier = Modifier
                    .size(Dimensions.avatarSizeMedium)
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
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AddNewItem(isAccount: Boolean = true, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingExtraSmall)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_new),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(Dimensions.spacingLarge))
            Text(
                text = if (isAccount) stringResource(R.string.create_a_new_account) else stringResource(R.string.create_a_new_category),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMediumSemiBold
            )
        }
    }
}
