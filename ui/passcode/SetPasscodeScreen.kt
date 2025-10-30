package com.achievemeaalk.freedjf.ui.passcode

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.achievemeaalk.freedjf.data.security.viewmodel.SecurityViewModel
import com.achievemeaalk.freedjf.R

private enum class SetPasscodeStep {
    CREATE, CONFIRM
}

@Composable
fun SetPasscodeScreen(
    onPasscodeSet: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    var step by remember { mutableStateOf(SetPasscodeStep.CREATE) }
    var newPin by remember { mutableStateOf("") }
    var pinToConfirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val currentPin = if (step == SetPasscodeStep.CREATE) newPin else pinToConfirm
    val title = if (step == SetPasscodeStep.CREATE) stringResource(R.string.create_new_pin) else stringResource(R.string.confirm_your_pin)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            PinIndicator(pinLength = currentPin.length)

            Spacer(modifier = Modifier.height(48.dp))

            NumericKeypad(
                onNumberClick = { number ->
                    if (currentPin.length < 4) {
                        if (step == SetPasscodeStep.CREATE) {
                            newPin += number
                        } else {
                            pinToConfirm += number
                        }
                    }
                },
                onBackspaceClick = {
                    if (step == SetPasscodeStep.CREATE) {
                        newPin = newPin.dropLast(1)
                    } else {
                        pinToConfirm = pinToConfirm.dropLast(1)
                    }
                    error = null
                }
            )

            val pinMismatchError = stringResource(R.string.pins_do_not_match)
            LaunchedEffect(newPin) {
                if (newPin.length == 4) {
                    step = SetPasscodeStep.CONFIRM
                }
            }

            LaunchedEffect(pinToConfirm) {
                if (pinToConfirm.length == 4) {
                    if (newPin == pinToConfirm) {
                        viewModel.setPin(newPin)
                        viewModel.setPasscodeEnabled(true)
                        onPasscodeSet()
                    } else {
                        error = pinMismatchError
                        newPin = ""
                        pinToConfirm = ""
                        step = SetPasscodeStep.CREATE
                    }
                }
            }
        }
    }
}
