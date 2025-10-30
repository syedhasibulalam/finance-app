package com.achievemeaalk.freedjf.util

import java.util.Currency

object CurrencyProvider {

    // A curated list of major world currencies to simplify the dropdown.
    private val majorCurrencyCodes = listOf(
        "USD", // United States Dollar
        "EUR", // Euro
        "JPY", // Japanese Yen
        "GBP", // British Pound Sterling
        "AUD", // Australian Dollar
        "CAD", // Canadian Dollar
        "CHF", // Swiss Franc
        "CNY", // Chinese Yuan
        "INR", // Indian Rupee
        "BDT", // Bangladeshi Taka
        "BRL", // Brazilian Real
        "RUB", // Russian Ruble
        "SGD", // Singapore Dollar
        "AED"  // UAE Dirham
    )

    fun getAvailableCurrencies(): List<Currency> {
        return majorCurrencyCodes.mapNotNull { code ->
            try {
                Currency.getInstance(code)
            } catch (e: Exception) {
                null // In case a currency code is not supported on a specific device
            }
        }
    }
} 