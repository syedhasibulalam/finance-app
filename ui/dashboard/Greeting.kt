package com.achievemeaalk.freedjf.ui.dashboard

import com.achievemeaalk.freedjf.R
import java.util.Calendar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingResId = when (hour) {
        in 0..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }
    return stringResource(id = greetingResId)
}
