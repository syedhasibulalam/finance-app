package com.achievemeaalk.freedjf.util

import android.content.Context
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportTransactions(context: Context, transactions: List<MyFinTransaction>, accounts: List<Account>): String {
        val headerText = context.getString(R.string.csv_header)
        val naText = context.getString(R.string.not_applicable)
        val rows = transactions.map { transaction ->
            val accountName = accounts.find { it.accountId == transaction.accountId }?.name ?: naText
            val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(transaction.dateTimestamp))
            "${formattedDate},${accountName},\"${transaction.description}\",${transaction.amount},${transaction.type}"
        }
        return headerText + "\n" + rows.joinToString("\n")
    }
}