package com.achievemeaalk.freedjf.util

import android.content.Context
import java.util.*

object CurrencyUtils {

    fun getCurrencyCodeForLocale(context: Context): String {
        return try {
            val locale = context.resources.configuration.locales[0]
            Currency.getInstance(locale).currencyCode
        } catch (e: Exception) {
            "USD" // Fallback to USD if detection fails
        }
    }
}
