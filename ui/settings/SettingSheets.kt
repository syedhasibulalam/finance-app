package com.achievemeaalk.freedjf.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.preferences.Theme
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.util.CurrencyProvider
import com.achievemeaalk.freedjf.ui.components.ConfirmationDialog
import com.achievemeaalk.freedjf.ui.settings.homescreen.HomeScreenSettingsScreen
import com.achievemeaalk.freedjf.ui.settings.security.SecuritySettingsScreen
import kotlin.math.roundToInt

@Composable
fun ThemeSettingsSheet(viewModel: SettingsViewModel) {
    val theme by viewModel.theme.collectAsState()
    Column(
        modifier = Modifier.padding(Dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
    ) {
        Text(stringResource(R.string.theme), style = MaterialTheme.typography.headlineSmall)
        Row(
           modifier = Modifier.fillMaxWidth(),
           horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
       ) {
           Theme.entries.forEach { themeOption ->
               val isSelected = theme == themeOption
               Button(
                   onClick = { viewModel.setTheme(themeOption) },
                   modifier = Modifier.weight(1f),
                   shape = MaterialTheme.shapes.medium,
                   colors = ButtonDefaults.buttonColors(
                       containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                       contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                   )
               ) {
                   Text(text = themeOption.name.lowercase(java.util.Locale.getDefault()).replaceFirstChar { it.titlecase(java.util.Locale.getDefault()) }, style = MaterialTheme.typography.labelLarge)
               }
           }
        }
    }
}

@Composable
fun CurrencySettingsSheet(viewModel: SettingsViewModel) {
    val currency by viewModel.currency.collectAsState()
    val currencies = CurrencyProvider.getAvailableCurrencies()

    Column(
        modifier = Modifier.padding(Dimensions.screenPadding)
    ) {
        Text(
            text = stringResource(R.string.currency),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Dimensions.spacingLarge)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            items(currencies) { currencyOption ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setCurrency(currencyOption.currencyCode) },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (currency == currencyOption.currencyCode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${currencyOption.symbol} ${currencyOption.currencyCode}", style = MaterialTheme.typography.bodyLarge)
                        if (currency == currencyOption.currencyCode) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.selected),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSettingsSheet(viewModel: SettingsViewModel) {
    val language by viewModel.language.collectAsState()
    val languages = listOf(
        "en" to stringResource(R.string.language_english),
        "es" to stringResource(R.string.language_spanish),
        "fr" to stringResource(R.string.language_french),
        // Added more common languages; resource files must be added under res/values-XX
        "de" to stringResource(R.string.language_german),
        "it" to stringResource(R.string.language_italian),
        "pt-BR" to stringResource(R.string.language_portuguese_brazil),
        "ru" to stringResource(R.string.language_russian),
        "hi" to stringResource(R.string.language_hindi),
        "id" to stringResource(R.string.language_indonesian),
        "tr" to stringResource(R.string.language_turkish),
        "vi" to stringResource(R.string.language_vietnamese),
        "zh-CN" to stringResource(R.string.language_chinese_simplified),
        "ar" to stringResource(R.string.language_arabic),
        // Optional additional locales scaffolding
        "ja" to stringResource(R.string.language_japanese),
        "ko" to stringResource(R.string.language_korean),
        "nl" to stringResource(R.string.language_dutch),
        "pl" to stringResource(R.string.language_polish),
        "th" to stringResource(R.string.language_thai),
        "uk" to stringResource(R.string.language_ukrainian),
        "pt-PT" to stringResource(R.string.language_portuguese_portugal),
        "zh-TW" to stringResource(R.string.language_chinese_traditional)
    )

    Column(
        modifier = Modifier.padding(Dimensions.screenPadding)
    ) {
        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Dimensions.spacingLarge)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            items(languages) { (code, name) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setLanguage(code) },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (language == code) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                        if (language == code) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.selected),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Sheet for Notifications Settings ---
@Composable
fun NotificationsSettingsSheet(viewModel: SettingsViewModel) {
    val dueSoonDays by viewModel.dueSoonDays.collectAsState()
    Column(
        modifier = Modifier.padding(Dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
    ) {
        Text(stringResource(R.string.notifications), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.due_soon_reminders), style = MaterialTheme.typography.titleMedium)
        Slider(
            value = dueSoonDays.toFloat(),
            onValueChange = { viewModel.setDueSoonDays(it.roundToInt()) },
            valueRange = 1f..30f,
            steps = 28
        )
        Text(
            text = stringResource(R.string.due_soon_reminders_description, dueSoonDays),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// --- Sheet for Home Screen Settings ---
@Composable
fun HomeScreenSettingsSheet(viewModel: SettingsViewModel = hiltViewModel()) {
    Column(modifier = Modifier.padding(Dimensions.screenPadding)) {
        Text(stringResource(R.string.home_screen), style = MaterialTheme.typography.headlineSmall)
        HomeScreenSettingsScreen(viewModel)
    }
}

// --- Sheet for Security Settings ---
@Composable
fun SecuritySettingsSheet(navController: NavController) {
    Column(modifier = Modifier.padding(Dimensions.screenPadding)) {
        Text(stringResource(R.string.security), style = MaterialTheme.typography.headlineSmall)
        SecuritySettingsScreen(navController = navController)
    }
}

// --- Sheet for Data Management ---
@Composable
fun DataManagementSheet(viewModel: SettingsViewModel, navController: NavController) {
    val context = LocalContext.current

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { viewModel.createBackup(context, it) }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.restoreBackup(context, it) }
    }

    val items = listOf(
        stringResource(R.string.export_to_csv) to { viewModel.exportTransactionsToCsv(context) },
        stringResource(R.string.backup_data) to { backupLauncher.launch("monefy_backup.db") },
        stringResource(R.string.restore_data) to { restoreLauncher.launch("*/*") }
    )

    Column(
        modifier = Modifier.padding(Dimensions.screenPadding)
    ) {
        Text(
            text = stringResource(R.string.data_management),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Dimensions.spacingLarge)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            items(items) { (title, action) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { action() },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// --- Sheet for Delete and Reset ---
@Composable
fun DeleteAndResetSheet(viewModel: SettingsViewModel) {
    var showDeleteRecordsDialog by remember { mutableStateOf(false) }
    var showDeleteEverythingDialog by remember { mutableStateOf(false) }
    var showResetAppDialog by remember { mutableStateOf(false) }

    val items = listOf(
        Triple(stringResource(R.string.delete_all_records), { showDeleteRecordsDialog = true }, false),
        Triple(stringResource(R.string.delete_everything), { showDeleteEverythingDialog = true }, false),
        Triple(stringResource(R.string.reset_app), { showResetAppDialog = true }, true)
    )

    Column(
        modifier = Modifier.padding(Dimensions.screenPadding)
    ) {
        Text(
            text = stringResource(R.string.delete_and_reset),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Dimensions.spacingLarge)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            items(items) { (title, action, isDestructive) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { action() },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        show = showDeleteRecordsDialog,
        onDismiss = { showDeleteRecordsDialog = false },
        onConfirm = {
            viewModel.deleteAllRecords()
            showDeleteRecordsDialog = false
        },
        title = stringResource(R.string.delete_all_records),
        message = stringResource(R.string.delete_records_confirmation),
        confirmButtonText = stringResource(R.string.delete)
    )
    ConfirmationDialog(
        show = showDeleteEverythingDialog,
        onDismiss = { showDeleteEverythingDialog = false },
        onConfirm = {
            viewModel.deleteAllData()
            showDeleteEverythingDialog = false
        },
        title = stringResource(R.string.delete_everything),
        message = stringResource(R.string.delete_everything_confirmation),
        confirmButtonText = stringResource(R.string.delete)
    )
    ConfirmationDialog(
        show = showResetAppDialog,
        onDismiss = { showResetAppDialog = false },
        onConfirm = {
            viewModel.resetApplication()
            showResetAppDialog = false
        },
        title = stringResource(R.string.reset_app),
        message = stringResource(R.string.reset_app_confirmation),
        confirmButtonText = stringResource(R.string.reset)
    )
}
