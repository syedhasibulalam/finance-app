package com.achievemeaalk.freedjf.util

import com.google.mlkit.vision.text.Text
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

object ReceiptParser {

    data class ParseResult(
        val totalAmount: Double?,
        val date: Long?,
        val seller: String?
    )

    private val datePatterns = listOf(
        Pattern.compile("(\\d{2}[/-]\\d{2}[/-]\\d{2,4})"), // 01-01-2023 or 01/01/23
        Pattern.compile("(\\d{4}[/-]\\d{2}[/-]\\d{2})"),  // 2023-01-01
        Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{2,4})"),  // 01.01.2023
        Pattern.compile("(\\d{2}\\s+\\w{3}\\s+\\d{2,4})") // 01 Jan 2023
    )

    private val amountKeywords = setOf(
        "total", "amount", "subtotal", "balance", "due", "sum", "grand total",
        "final", "payable", "charged", "bill", "invoice", "receipt", "paid"
    )

    private val currencySymbols = listOf("$", "€", "£", "¥", "₹", "₽", "₦", "₨")

    fun parseText(text: Text): ParseResult {
        val allText = text.text
        val lines = allText.split("\n").map { it.trim() }.filter { it.isNotBlank() }

        val seller = findSeller(lines)
        val date = findDate(allText)
        val totalAmount = findTotalAmount(lines)

        return ParseResult(totalAmount, date, seller)
    }

    private fun findSeller(lines: List<String>): String? {
        // Look for the seller in the first few lines, excluding very short lines
        return lines.take(5)
            .firstOrNull { line ->
                line.length > 3 &&
                    !line.matches(Regex(".*\\d{2}[/-]\\d{2}[/-]\\d{2,4}.*")) && // Not a date line
                    !line.contains(Regex("\\d+\\.\\d{2}")) && // Not an amount line
                    line.any { it.isLetter() } // Contains letters
            }?.trim()
    }

    private fun findDate(text: String): Long? {
        for (pattern in datePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val dateStr = matcher.group(1)
                // Attempt to parse with different formats
                val formats = listOf(
                    SimpleDateFormat("dd/MM/yyyy", Locale.US),
                    SimpleDateFormat("dd-MM-yyyy", Locale.US),
                    SimpleDateFormat("yyyy-MM-dd", Locale.US),
                    SimpleDateFormat("dd/MM/yy", Locale.US),
                    SimpleDateFormat("dd.MM.yyyy", Locale.US),
                    SimpleDateFormat("dd MMM yyyy", Locale.US)
                )
                for (format in formats) {
                    try {
                        return format.parse(dateStr!!)?.time
                    } catch (e: Exception) {
                        // Continue to next format
                    }
                }
            }
        }
        return null
    }

    private fun findTotalAmount(lines: List<String>): Double? {
        val potentialAmounts = mutableListOf<Pair<Double, Int>>() // Amount and priority

        // Find the largest amount in the bottom 25% of the receipt
        val bottomQuarterStartIndex = (lines.size * 0.75).toInt()
        val bottomLines = lines.subList(bottomQuarterStartIndex, lines.size)
        val largestInBottom = bottomLines
            .flatMap { extractAmountsFromLine(it) }
            .maxOrNull()

        lines.forEachIndexed { index, line ->
            val cleanedLine = line.lowercase(Locale.getDefault())
            val numbers = extractAmountsFromLine(line)

            if (numbers.isNotEmpty()) {
                val isTotalLine = amountKeywords.any { keyword ->
                    cleanedLine.contains(keyword)
                }

                numbers.forEach { amount ->
                    val priority = when {
                        isTotalLine -> 4 // Highest priority for lines with total keywords
                        amount == largestInBottom && index >= bottomQuarterStartIndex -> 3 // High priority for largest amount in bottom 25%
                        index > lines.size - 5 -> 2 // Higher priority for lines near the end
                        else -> 1 // Lower priority for other lines
                    }
                    potentialAmounts.add(Pair(amount, priority))
                }
            }
        }

        // Sort by priority (highest first), then by amount (highest first)
        return potentialAmounts
            .sortedWith(compareByDescending<Pair<Double, Int>> { it.second }.thenByDescending { it.first })
            .firstOrNull()?.first
    }

    private fun extractAmountsFromLine(line: String): List<Double> {
        val amounts = mutableListOf<Double>()

        // Pattern to match currency amounts like $12.34, €15.67, 123.45, etc.
        val amountPattern = Pattern.compile("([${currencySymbols.joinToString("")}]?\\s*\\d+[.,]\\d{2})")
        val matcher = amountPattern.matcher(line)

        while (matcher.find()) {
            val amountStr = matcher.group(1)
                ?.replace(Regex("[${Pattern.quote(currencySymbols.joinToString(""))}\\s]"), "")
                ?.replace(",", ".")

            amountStr?.toDoubleOrNull()?.let { amount ->
                if (amount > 0.01) { // Filter out very small amounts that might be noise
                    amounts.add(amount)
                }
            }
        }

        return amounts
    }
}