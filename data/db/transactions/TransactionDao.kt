package com.achievemeaalk.freedjf.data.db.transactions

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.achievemeaalk.freedjf.data.model.MonthlyCashFlow
import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query(
        """
        SELECT 
            strftime('%Y-%m', datetime(dateTimestamp / 1000, 'unixepoch')) as month,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as totalIncome,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as totalExpense
        FROM transactions
        GROUP BY month
        ORDER BY month ASC
    """
    )
    fun getMonthlyCashFlow(): Flow<List<MonthlyCashFlow>>

    @Query("SELECT * FROM transactions WHERE description LIKE '%' || :searchQuery || '%' ORDER BY dateTimestamp DESC")
    fun getTransactions(searchQuery: String): Flow<List<MyFinTransaction>>

    @Query("SELECT * FROM transactions WHERE transactionId = :id")
    fun getTransactionById(id: Int): Flow<MyFinTransaction?>

    @Query("SELECT * FROM transactions WHERE dateTimestamp BETWEEN :startDate AND :endDate")
    fun getTransactionsForDateRange(startDate: Long, endDate: Long): Flow<List<MyFinTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MyFinTransaction)

    @Update
    suspend fun updateTransaction(transaction: MyFinTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: MyFinTransaction)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}