package com.achievemeaalk.freedjf.util

import java.util.Currency
import java.util.Locale

object CurrencySymbolProvider {

    private val symbolMap = mapOf(
        "BDT" to "৳",
        "INR" to "₹",
    )

    fun getSymbol(currencyCode: String): String {
        if (symbolMap.containsKey(currencyCode)) {
            return symbolMap[currencyCode]!!
        }

        return try {
            val currency = Currency.getInstance(currencyCode)


            when (currencyCode) {
                "USD" -> return currency.getSymbol(Locale.US)
                "GBP" -> return currency.getSymbol(Locale.UK)
                "JPY" -> return currency.getSymbol(Locale.JAPAN)
                "CNY" -> return currency.getSymbol(Locale.CHINA)
                "EUR" -> return currency.getSymbol(Locale.FRANCE)
            }

            currency.symbol
        } catch (e: Exception) {
            currencyCode
        }
    }
}