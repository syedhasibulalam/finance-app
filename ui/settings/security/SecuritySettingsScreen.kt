package com.achievemeaalk.freedjf.ui.settings.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.data.security.viewmodel.SecurityViewModel
import com.achievemeaalk.freedjf.R


@Composable
fun SecuritySettingsScreen(
    navController: NavController,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val isPasscodeEnabled by viewModel.isPasscodeEnabled.collectAsState()
    val currentBackStackEntry = navController.currentBackStackEntry

    LaunchedEffect(currentBackStackEntry) {
        val passcodeUpdated =
            currentBackStackEntry?.savedStateHandle?.get<Boolean>("passcode_updated")
        if (passcodeUpdated == true) {
            viewModel.refreshPasscodeState()
            currentBackStackEntry.savedStateHandle.remove<Boolean>("passcode_updated")
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.enable_passcode),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isPasscodeEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            navController.navigate("setPasscode")
                        } else {
                            viewModel.setPin("")
                            viewModel.setPasscodeEnabled(false)
                        }
                    }
                )
            }
        }

    }
}
