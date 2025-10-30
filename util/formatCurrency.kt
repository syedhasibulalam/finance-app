package com.achievemeaalk.freedjf.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun formatCurrency(amount: Double, currencyCode: String, locale: Locale = Locale.getDefault()): String {
    val format = NumberFormat.getCurrencyInstance(locale)
    try {
        format.currency = Currency.getInstance(currencyCode)
    } catch (_: Exception) {
        // Fallback: leave locale currency if provided code is invalid
    }
    return format.format(amount)
}