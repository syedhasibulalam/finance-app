// main/java/com/achievemeaalk/freedjf/ui/accounts/AddEditAccountBottomSheet.kt
package com.achievemeaalk.freedjf.ui.accounts

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.util.CurrencySymbolProvider
import com.achievemeaalk.freedjf.util.IconProvider
import com.achievemeaalk.freedjf.util.InputValidation
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import com.achievemeaalk.freedjf.ui.components.AnimatedOutlinedButton
import com.achievemeaalk.freedjf.ui.components.AnimatedPrimaryButton

@Composable
fun AddEditAccountBottomSheet(
    account: Account?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    isFromOnboarding: Boolean = false,
    viewModel: AccountsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var name by remember(account) { mutableStateOf(account?.name ?: "") }
    var balance by remember(account) { mutableStateOf(account?.balance?.toString()?.takeIf { it != "0.0" } ?: "") }
    val accountTypes = stringArrayResource(R.array.account_types)
    var selectedType by remember(account) { mutableStateOf(account?.accountType ?: accountTypes.first()) }
    var icon by remember(account) { mutableStateOf(account?.icon ?: "account_balance_wallet") }
    var creditLimit by remember(account) { mutableStateOf(account?.creditLimit?.toString() ?: "") }
    val focusRequester = remember { FocusRequester() }
    val adViewModel = viewModel.adViewModel

    LaunchedEffect(Unit) {
        if (account == null) {
            focusRequester.requestFocus()
        }
    }


    val currency by settingsViewModel.currency.collectAsState()
    val currencySymbol = CurrencySymbolProvider.getSymbol(currency)
    val context = LocalContext.current

    var validationError by remember { mutableStateOf<String?>(null) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges by remember(name, balance, icon, selectedType, account) {
        derivedStateOf {
            if (account == null) {
                name.isNotEmpty() || balance.isNotEmpty()
            } else {
                name != account.name ||
                    (balance.toDoubleOrNull() ?: 0.0) != account.balance ||
                    icon != account.icon ||
                    selectedType != account.accountType
            }
        }
    }

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                AnimatedPrimaryButton(onClick = {
                    showUnsavedChangesDialog = false
                    onDismiss()
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

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        adViewModel.preloadTransactionCompletionAd(context)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (account == null) stringResource(R.string.add_account) else stringResource(R.string.edit_account),
                style = MaterialTheme.typography.headlineSmall
            )
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

            IconButton(onClick = {
                if (hasUnsavedChanges) {
                    showUnsavedChangesDialog = true
                } else {
                    onDismiss()
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
        Spacer(Modifier.height(Dimensions.spacingSmall))

        Text(stringResource(R.string.account_name), style = MaterialTheme.typography.titleMedium)
        TextField(
            value = name,
            onValueChange = {
                name = it
                validationError = null
            },
            placeholder = { Text(stringResource(R.string.enter_account_name)) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
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

        Text(stringResource(R.string.account_type), style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            accountTypes.forEach { type ->
                val isSelected = selectedType == type
                AnimatedPrimaryButton(
                    onClick = { selectedType = type },
                    shape = RoundedCornerShape(Dimensions.cornerRadiusLarge),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(type, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Text(stringResource(R.string.initial_balance), style = MaterialTheme.typography.titleMedium)
        TextField(
            value = balance,
            onValueChange = {
                balance = it
                validationError = null
            },
            placeholder = { Text(stringResource(R.string.empty_balance_placeholder)) },
            leadingIcon = { Text(currencySymbol, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
        if (selectedType == stringArrayResource(R.array.account_types)[2]) {
            Spacer(Modifier.height(Dimensions.spacingMedium))
            Text(stringResource(R.string.credit_limit), style = MaterialTheme.typography.titleMedium)
            TextField(
                value = creditLimit,
                onValueChange = {
                    creditLimit = it
                    validationError = null
                },
                placeholder = { Text(stringResource(R.string.credit_limit_placeholder)) },
                leadingIcon = {
                    Text(
                        currencySymbol,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
        }

        Text(stringResource(R.string.choose_icon), style = MaterialTheme.typography.titleMedium)
        val icons = IconProvider.allIcons.keys.toList()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 60.dp),
            modifier = Modifier.heightIn(max = 200.dp), 
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            items(icons) { iconName ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(Dimensions.cornerRadiusLarge))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                            icon = iconName
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        })
                        .padding(Dimensions.spacingSmall)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconProvider.getIcon(iconName),
                        contentDescription = iconName,
                        tint = if (icon == iconName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Dimensions.iconSizeLarge)
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f)) 

        val nameValidation = InputValidation.validateAccountName(name)
        val balanceValidation = if (balance.isNotEmpty()) InputValidation.validateAmount(balance) else InputValidation.ValidationResult(true)
        val isSaveEnabled = nameValidation.isValid && balanceValidation.isValid

        AnimatedPrimaryButton(
            onClick = {
                val finalNameValidation = InputValidation.validateAccountName(name)
                if (!finalNameValidation.isValid) {
                    validationError = finalNameValidation.errorMessage
                    return@AnimatedPrimaryButton
                }
                val finalBalanceValidation = if (balance.isNotEmpty()) InputValidation.validateAmount(balance) else InputValidation.ValidationResult(true)
                if (!finalBalanceValidation.isValid) {
                    validationError = finalBalanceValidation.errorMessage
                    return@AnimatedPrimaryButton
                }

                val validBalance = if (balance.isNotEmpty()) InputValidation.parseAmount(balance) ?: 0.0 else 0.0
                val validCreditLimit = if (creditLimit.isNotEmpty()) InputValidation.parseAmount(creditLimit) else null


                val newOrUpdatedAccount = account?.copy(
                    name = name.trim(),
                    balance = validBalance,
                    icon = icon,
                    accountType = selectedType,
                    creditLimit = validCreditLimit
                ) ?: Account(
                    name = name.trim(),
                    balance = validBalance,
                    icon = icon,
                    accountType = selectedType,
                    creditLimit = validCreditLimit
                )

                if (account == null) {
                    viewModel.addAccount(newOrUpdatedAccount, isFromOnboarding) { newAccountId ->
                        onSave(newAccountId)
                    }
                    if (!isFromOnboarding) {
                        adViewModel.showTransactionCompletionAd(context)
                    }
                } else {
                    viewModel.updateAccount(newOrUpdatedAccount) { updatedAccountId ->
                        onSave(updatedAccountId)
                    }
                }
            },
            enabled = isSaveEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.buttonHeight),
            shape = RoundedCornerShape(Dimensions.cornerRadiusExtraLarge)
        ) {
            Text(
                stringResource(if (account == null) R.string.create_account else R.string.save),
                style = MaterialTheme.typography.titleMedium
            )
        }

        val errorMessage = validationError ?: if (!isSaveEnabled) {
            nameValidation.errorMessage ?: balanceValidation.errorMessage
        } else null

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.spacingSmall)
            )
        }
    }
}
