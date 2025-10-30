package com.achievemeaalk.freedjf.ui.passcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achievemeaalk.freedjf.R

@Composable
fun PinIndicator(pinLength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) { index ->
            val color = if (index < pinLength)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
fun NumericKeypad(onNumberClick: (String) -> Unit, onBackspaceClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..3).forEach { i ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val number = (i - 1) * 3
                KeyButton(text = "${number + 1}") { onNumberClick("${number + 1}") }
                KeyButton(text = "${number + 2}") { onNumberClick("${number + 2}") }
                KeyButton(text = "${number + 3}") { onNumberClick("${number + 3}") }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.weight(1f))
            KeyButton(modifier = Modifier.weight(1f), text = "0") { onNumberClick("0") }
            IconButton(
                onClick = onBackspaceClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.backspace),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun KeyButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.size(72.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}
