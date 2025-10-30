// com/achievemeaalk/freedjf/ui/onboarding/FirstAccountSuccessScreen.kt
package com.achievemeaalk.freedjf.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.achievemeaalk.freedjf.ui.accounts.AccountsViewModel
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.theme.TransferColor
import com.achievemeaalk.freedjf.ui.theme.bodyLargeBold
import com.achievemeaalk.freedjf.ui.theme.headlineMediumMedium
import com.achievemeaalk.freedjf.util.formatCurrency
import androidx.compose.runtime.setValue
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.components.CelebrationAnimation
import com.achievemeaalk.freedjf.ui.components.AnimatedPrimaryButton

@Composable
fun FirstAccountSuccessScreen(
    navController: NavController,
    viewModel: AccountsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val lastAccount = accounts.lastOrNull()
    val currency by settingsViewModel.currency.collectAsState()


    var showAnimation by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(Dimensions.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.account_created),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                    Text(
                        text = stringResource(id = R.string.account_created_message),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimensions.spacingHuge))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(Dimensions.cornerRadiusLarge))
                            .background(TransferColor)
                    ) {
                        Text(
                            text =lastAccount?.accountType ?: "Visa",
                            style = MaterialTheme.typography.headlineMediumMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(Dimensions.cardPadding)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimensions.spacingHuge))
                    if (lastAccount != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            shape = RoundedCornerShape(Dimensions.cornerRadiusLarge),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(Dimensions.cardPadding)) {
                                AccountInfoRow(stringResource(id = R.string.account_type_label), lastAccount.accountType)
                                AccountInfoRow(stringResource(id = R.string.account_name_label), lastAccount.name)
                                AccountInfoRow(stringResource(id = R.string.current_balance_label), formatCurrency(lastAccount.balance, currency))
                            }
                        }
                    }
                }
                AnimatedPrimaryButton(
                    onClick = {
                        navController.navigate(paywallRoute) {
                            popUpTo(firstAccountSuccessRoute) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.buttonHeightLarge),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.continue_button), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            if (showAnimation) {
                CelebrationAnimation(onFinished = { showAnimation = false })
            }
        }
    }
}

@Composable
private fun AccountInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLargeBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
