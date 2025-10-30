// src/main/java/com/achievemeaalk/freedjf/ui/settings/SettingsScreen.kt
package com.achievemeaalk.freedjf.ui.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.BuildConfig
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.ui.theme.Dimensions

private enum class SettingsSheetContent {
    None, ChangeName, Theme, Currency, Language, Notifications, HomeScreen, Security, Data, Delete, Privacy, Terms
}

private data class SettingsItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val sheetContent: SettingsSheetContent
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  rememberCoroutineScope()
    var currentSheet by remember { mutableStateOf(SettingsSheetContent.None) }
  LocalContext.current

    val subscriptionTitle = stringResource(R.string.subscriptions)
    val settingsItems = listOf(
        SettingsItem(stringResource(R.string.settings_change_name_title), stringResource(R.string.settings_change_name_subtitle), Icons.Default.Person, SettingsSheetContent.ChangeName),
        SettingsItem(stringResource(R.string.settings_theme_title), stringResource(R.string.settings_theme_subtitle), Icons.Default.Palette, SettingsSheetContent.Theme),
        SettingsItem(stringResource(R.string.settings_currency_title), stringResource(R.string.settings_currency_subtitle), Icons.Default.AttachMoney, SettingsSheetContent.Currency),
        SettingsItem(stringResource(R.string.settings_language_title), stringResource(R.string.settings_language_subtitle), Icons.Default.Language, SettingsSheetContent.Language),
        SettingsItem(stringResource(R.string.notifications), stringResource(R.string.settings_notifications_subtitle), Icons.Default.Notifications, SettingsSheetContent.Notifications),
        SettingsItem(stringResource(R.string.home_screen), stringResource(R.string.settings_home_screen_subtitle), Icons.Default.Home, SettingsSheetContent.HomeScreen),
        SettingsItem(stringResource(R.string.security), stringResource(R.string.settings_security_subtitle), Icons.Default.Security, SettingsSheetContent.Security),
        SettingsItem(stringResource(R.string.data_management), stringResource(R.string.settings_data_subtitle), Icons.Default.Storage, SettingsSheetContent.Data),
        SettingsItem(stringResource(R.string.delete_and_reset), stringResource(R.string.settings_delete_subtitle), Icons.Default.DeleteForever, SettingsSheetContent.Delete),
        SettingsItem(stringResource(R.string.subscriptions), stringResource(R.string.manage_label), Icons.Default.Star, SettingsSheetContent.None),
        SettingsItem(stringResource(R.string.privacy_policy), stringResource(R.string.view_details), Icons.Default.PrivacyTip, SettingsSheetContent.Privacy),
        SettingsItem(stringResource(R.string.terms_of_service), stringResource(R.string.view_details), Icons.Default.Gavel, SettingsSheetContent.Terms)
    )

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
           contentPadding = PaddingValues(Dimensions.screenPadding),
           verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.settings_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = Dimensions.spacingLarge)
                )
            }
            items(settingsItems) { item ->
                SettingsListItem(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    onClick = {
                        if (item.title == subscriptionTitle) {
                            navController.navigate("subscriptionSettings")
                        } else {
                            currentSheet = item.sheetContent
                        }
                    }
                )
            }
            // Removed debug test purchase controls
        }
    }

    if (currentSheet != SettingsSheetContent.None) {
        ModalBottomSheet(
            onDismissRequest = { currentSheet = SettingsSheetContent.None },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.background,

            ) {
            when (currentSheet) {
                SettingsSheetContent.ChangeName -> ChangeNameSheet(viewModel)
                SettingsSheetContent.Theme -> ThemeSettingsSheet(viewModel)
                SettingsSheetContent.Currency -> CurrencySettingsSheet(viewModel)
                SettingsSheetContent.Language -> LanguageSettingsSheet(viewModel)
                SettingsSheetContent.Notifications -> NotificationsSettingsSheet(viewModel)
                SettingsSheetContent.HomeScreen -> HomeScreenSettingsSheet(viewModel)
                SettingsSheetContent.Security -> SecuritySettingsSheet(navController)
                SettingsSheetContent.Data -> DataManagementSheet(viewModel, navController)
                SettingsSheetContent.Delete -> DeleteAndResetSheet(viewModel)
                SettingsSheetContent.Privacy -> PrivacySheet()
                SettingsSheetContent.Terms -> TermsSheet()
                else -> {}
            }
        }
    }
}

@Composable
private fun ChangeNameSheet(viewModel: SettingsViewModel) {
    val userName by viewModel.userName.collectAsState()
    var newName by remember { mutableStateOf(userName) }

    Column(
        modifier = Modifier
            .padding(Dimensions.screenPadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.settings_change_name_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text(stringResource(R.string.settings_change_name_title)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
        Button(
            onClick = { viewModel.updateUserName(newName) },
            enabled = newName.isNotBlank() && newName != userName
        ) {
            Text(stringResource(R.string.save))
        }
    }
}

@Composable
private fun PrivacySheet() {
    Column(
        modifier = Modifier
            .padding(Dimensions.screenPadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.privacy_policy), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
        Text(stringResource(R.string.view_details), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TermsSheet() {
    Column(
        modifier = Modifier
            .padding(Dimensions.screenPadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.terms_of_service), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
        Text(stringResource(R.string.view_details), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SettingsListItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
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
        shape = MaterialTheme.shapes.large,
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
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimensions.iconSizeLarge)
            )
            Spacer(modifier = Modifier.width(Dimensions.spacingLarge))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
