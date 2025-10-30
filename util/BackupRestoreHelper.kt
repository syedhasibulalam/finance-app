
package com.achievemeaalk.freedjf.util

import android.content.Context
import android.net.Uri
import androidx.room.RoomDatabase
import java.io.FileOutputStream

object BackupRestoreHelper {

  private const val BACKUP_FILE_NAME = "monefy_backup.db"

  fun createBackup(context: Context, database: RoomDatabase, outputUri: Uri) {
    val dbFile = context.getDatabasePath(database.openHelper.databaseName)
    context.contentResolver.openFileDescriptor(outputUri, "w")?.use {
      FileOutputStream(it.fileDescriptor).use { outputStream ->
        dbFile.inputStream().use { inputStream ->
          inputStream.copyTo(outputStream)
        }
      }
    }
  }

  fun restoreBackup(context: Context, database: RoomDatabase, inputUri: Uri) {
    val dbFile = context.getDatabasePath(database.openHelper.databaseName)

    // Close the database to prevent corruption
    database.close()

    context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
      FileOutputStream(dbFile).use { outputStream ->
        inputStream.copyTo(outputStream)
      }
    }
  }
}