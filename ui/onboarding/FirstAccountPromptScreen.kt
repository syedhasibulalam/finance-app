package com.achievemeaalk.freedjf.ui.onboarding

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.ui.accounts.AddEditAccountBottomSheet
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun FirstAccountPromptScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var showNamePrompt by remember { mutableStateOf(true) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = showNamePrompt,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { targetState ->
                if (targetState) {
                    NamePrompt(
                        name = name,
                        onNameChange = { name = it },
                        onContinue = {
                            viewModel.onNameSubmitted(name)
                            showNamePrompt = false
                        }
                    )
                } else {
                    AccountPrompt(
                        onShowBottomSheet = { showBottomSheet = true },
                        navController = navController
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = null
        ) {
            AddEditAccountBottomSheet(
                account = null,
                isFromOnboarding = true,
                onSave = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                            viewModel.onFirstAccountPromptCompleted()
                            Log.d("Navigation", "Navigating to $firstAccountSuccessRoute")
                            navController.navigate(firstAccountSuccessRoute) {
                                popUpTo(firstAccountPromptRoute) { inclusive = true }
                            }
                        }
                    }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun NamePrompt(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.avatarSizeExtraLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSizeExtraLarge),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
            Text(
                text = stringResource(id = R.string.what_should_we_call_you),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
            Text(
                text = stringResource(id = R.string.lets_get_to_know_each_other),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingHuge))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text(stringResource(id = R.string.enter_your_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(
            onClick = onContinue,
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.buttonHeight)
        ) {
            Text(stringResource(id = R.string.get_started))
        }
    }
}

@Composable
private fun AccountPrompt(
    onShowBottomSheet: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.avatarSizeExtraLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSizeExtraLarge),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
            Text(
                text = stringResource(id = R.string.lets_set_up_your_first_account),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingHuge))
            FeatureItem(
                icon = Icons.Default.TrackChanges,
                text = stringResource(id = R.string.track_expenses_in_real_time)
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
            FeatureItem(
                icon = Icons.Default.QueryStats,
                text = stringResource(id = R.string.smart_budget_planning)
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
            FeatureItem(
                icon = Icons.Default.Person,
                text = stringResource(id = R.string.personalized_insights)
            )
        }
        Column {
            Button(
                onClick = onShowBottomSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeight)
            ) {
                Text(stringResource(id = R.string.create_account))
            }
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
            TextButton(
                onClick = {
                    navController.navigate("dashboard") {
                        popUpTo(onboardingRoute) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeight)
            ) {
                Text(stringResource(id = R.string.skip_for_now))
            }
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, text: String) {
    Card(
        shape = RoundedCornerShape(Dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimensions.iconSizeMedium)
            )
            Spacer(modifier = Modifier.width(Dimensions.spacingMedium))
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
