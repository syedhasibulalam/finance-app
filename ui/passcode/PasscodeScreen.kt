package com.achievemeaalk.freedjf.ui.passcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.security.viewmodel.SecurityViewModel
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.passcode.PinIndicator
import com.achievemeaalk.freedjf.ui.passcode.NumericKeypad

import androidx.annotation.RequiresApi
import android.os.Build

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun PasscodeScreen(
    onUnlock: () -> Unit,
    securityViewModel: SecurityViewModel = hiltViewModel()
) {
    val pin by securityViewModel.pinValue.collectAsState()
    val error by securityViewModel.authenticationError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isLocked by securityViewModel.isLocked.collectAsState() 

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            securityViewModel.clearError()
        }
    }
    LaunchedEffect(isLocked) {
        if (!isLocked) {
            onUnlock()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.enter_passcode),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingExtraLarge))

            PinIndicator(pinLength = pin.length)

            Spacer(modifier = Modifier.height(Dimensions.spacingHuge * 2))

            if (securityViewModel.canAuthenticateWithBiometrics) {
                IconButton(
                    onClick = {
                        val activity = context as? FragmentActivity
                        activity?.let {
                            securityViewModel.authenticateWithBiometrics(it)
                        }
                    },
                    modifier = Modifier.size(Dimensions.iconSizeHuge * 2)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = stringResource(R.string.authenticate_with_fingerprint),
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.spacingExtraLarge))
            }

            NumericKeypad(
                onNumberClick = { number ->
                    securityViewModel.onPinValueChange(pin + number)
                },
                onBackspaceClick = {
                    if (pin.isNotEmpty()) {
                        securityViewModel.onPinValueChange(pin.dropLast(1))
                    }
                }
            )
        }
    }
}

