package com.achievemeaalk.freedjf.data.db.recurring

import androidx.room.*
import com.achievemeaalk.freedjf.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    
    @Query("SELECT * FROM recurring_transactions ORDER BY nextDueDate ASC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>
    
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveRecurringTransactions(): Flow<List<RecurringTransaction>>
    
    @Query("SELECT * FROM recurring_transactions WHERE isSubscription = 1 ORDER BY nextDueDate ASC")
    fun getSubscriptions(): Flow<List<RecurringTransaction>>
    
    @Query("SELECT * FROM recurring_transactions WHERE isSubscription = 1 AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveSubscriptions(): Flow<List<RecurringTransaction>>
    
    @Query("SELECT * FROM recurring_transactions WHERE isSubscription = 0 ORDER BY nextDueDate ASC")
    fun getBills(): Flow<List<RecurringTransaction>>
    
    @Query("SELECT * FROM recurring_transactions WHERE isSubscription = 0 AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveBills(): Flow<List<RecurringTransaction>>
    
    @Query("SELECT * FROM recurring_transactions WHERE nextDueDate <= :timestamp ORDER BY nextDueDate ASC")
    fun getRecurringTransactionsDueBefore(timestamp: Long): Flow<List<RecurringTransaction>>
    
    @Query("SELECT * FROM recurring_transactions WHERE nextDueDate <= :timestamp AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveRecurringTransactionsDueBefore(timestamp: Long): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE nextDueDate BETWEEN :startTime AND :endTime AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveRecurringTransactionsDueBetween(startTime: Long, endTime: Long): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringTransactionById(id: Int): RecurringTransaction?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long
    
    @Update
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)
    
    @Delete
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)
    
    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteRecurringTransactionById(id: Int)
    
    @Query("UPDATE recurring_transactions SET nextDueDate = :nextDueDate WHERE id = :id")
    suspend fun updateNextDueDate(id: Int, nextDueDate: Long)

    @Query("DELETE FROM recurring_transactions")
    suspend fun clearAllRecurringTransactions()
}
