package com.achievemeaalk.freedjf.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 20 to 21
 * This migration recalculates all account balances based on transaction history
 */
val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Recalculate all account balances based on transaction history
        database.execSQL("""
            UPDATE accounts 
            SET balance = (
                SELECT COALESCE(SUM(
                    CASE 
                        WHEN type = 'INCOME' THEN amount
                        WHEN type = 'EXPENSE' THEN -amount
                        ELSE 0
                    END
                ), 0)
                FROM transactions 
                WHERE transactions.accountId = accounts.accountId
            )
        """)
    }
}

