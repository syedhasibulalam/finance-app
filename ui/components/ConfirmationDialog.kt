package com.achievemeaalk.freedjf.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.achievemeaalk.freedjf.R

@Composable
fun ConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = { Text(text = message) },
            dismissButton = {
                Button(
                    onClick = onDismiss
                ) {
                    Text(text = stringResource(R.string.cancel_button_text))
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm
                ) {
                    Text(text = confirmButtonText, color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            icon = {
                painterResource(id = R.drawable.ic_warning)
            }
        )
    }
}
