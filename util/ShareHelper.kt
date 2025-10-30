package com.achievemeaalk.freedjf.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    fun shareCsv(context: Context, csvData: String) {
        val csvFile = File(context.cacheDir, "transactions.csv")
        FileOutputStream(csvFile).use {
            it.write(csvData.toByteArray())
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", csvFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Transactions"))
    }
} 