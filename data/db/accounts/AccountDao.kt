package com.achievemeaalk.freedjf.data.db.accounts

import androidx.room.*
import com.achievemeaalk.freedjf.data.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE accountId = :id")
    fun getAccount(id: Int): Flow<Account?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("DELETE FROM accounts")
    suspend fun clearAllAccounts()

    @Query("UPDATE accounts SET balance = :newBalance WHERE accountId = :accountId")
    suspend fun updateAccountBalance(accountId: Int, newBalance: Double)

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN type = 'INCOME' THEN amount
                WHEN type = 'EXPENSE' THEN -amount
                ELSE 0
            END
        ), 0) as calculatedBalance
        FROM transactions 
        WHERE accountId = :accountId
    """)
    suspend fun calculateAccountBalanceFromTransactions(accountId: Int): Double
}
