package com.achievemeaalk.freedjf.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for MyFinDatabase
 * Each migration should handle schema changes safely without data loss
 */
object DatabaseMigrations {
    
    // Example migration from version 12 to 13
    // Replace this with your actual migration logic based on what changed
    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add any schema changes that were made in version 13
            // This is a placeholder - you should implement actual migration logic
            // based on what changes were made to your database schema
            
            // Example of common migration operations:
            // database.execSQL("ALTER TABLE transactions ADD COLUMN new_column TEXT")
            // database.execSQL("CREATE INDEX index_name ON table_name(column_name)")
            
            // Since we don't know the exact changes made in version 13,
            // this migration is left empty but provides the structure
        }
    }
    
    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add reminderEnabled column to budget_categories table
            database.execSQL("ALTER TABLE budget_categories ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0")
        }
    }
    
    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `recurring_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` REAL NOT NULL, `categoryId` INTEGER NOT NULL, `nextDueDate` INTEGER NOT NULL, `frequency` TEXT NOT NULL, `isSubscription` INTEGER NOT NULL)"
            )
        }
    }
    
    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add accountId column to recurring_transactions table
            database.execSQL("ALTER TABLE recurring_transactions ADD COLUMN accountId INTEGER NOT NULL DEFAULT 1")
        }
    }
    
    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add isActive column to recurring_transactions table
            database.execSQL("ALTER TABLE recurring_transactions ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            
            // Note: The frequency field change from String to Enum is handled automatically
            // by Room since both are stored as TEXT in SQLite. The enum will be converted
            // to/from string automatically by Room's type converters.
        }
    }

    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Recreate transactions table with foreign keys
            database.execSQL("""
                CREATE TABLE new_transactions (
                    transactionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    description TEXT NOT NULL,
                    amount REAL NOT NULL,
                    type TEXT NOT NULL,
                    dateTimestamp INTEGER NOT NULL,
                    accountId INTEGER NOT NULL,
                    categoryId INTEGER,
                    destinationAccountId INTEGER,
                    FOREIGN KEY(accountId) REFERENCES accounts(accountId) ON DELETE CASCADE,
                    FOREIGN KEY(categoryId) REFERENCES categories(categoryId) ON DELETE CASCADE
                )
            """)
            database.execSQL("INSERT INTO new_transactions (transactionId, description, amount, type, dateTimestamp, accountId, categoryId, destinationAccountId) SELECT transactionId, description, amount, type, dateTimestamp, accountId, categoryId, destinationAccountId FROM transactions")
            database.execSQL("DROP TABLE transactions")
            database.execSQL("ALTER TABLE new_transactions RENAME TO transactions")

            // Recreate recurring_transactions table with foreign keys
            database.execSQL("""
                CREATE TABLE new_recurring_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    amount REAL NOT NULL,
                    accountId INTEGER NOT NULL,
                    categoryId INTEGER NOT NULL,
                    nextDueDate INTEGER NOT NULL,
                    frequency TEXT NOT NULL,
                    isSubscription INTEGER NOT NULL,
                    isActive INTEGER NOT NULL,
                    FOREIGN KEY(accountId) REFERENCES accounts(accountId) ON DELETE CASCADE,
                    FOREIGN KEY(categoryId) REFERENCES categories(categoryId) ON DELETE CASCADE
                )
            """)
            database.execSQL("INSERT INTO new_recurring_transactions (id, name, amount, accountId, categoryId, nextDueDate, frequency, isSubscription, isActive) SELECT id, name, amount, accountId, categoryId, nextDueDate, frequency, isSubscription, isActive FROM recurring_transactions")
            database.execSQL("DROP TABLE recurring_transactions")
            database.execSQL("ALTER TABLE new_recurring_transactions RENAME TO recurring_transactions")
        }
    }

    val MIGRATION_19_20 = object : Migration(19, 20) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add transactionType column to recurring_transactions with default EXPENSE
            database.execSQL("ALTER TABLE recurring_transactions ADD COLUMN transactionType TEXT NOT NULL DEFAULT 'EXPENSE'")
        }
    }
    
    /**
     * Get all available migrations
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_19_20
        )
    }
} 