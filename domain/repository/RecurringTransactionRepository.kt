package com.achievemeaalk.freedjf.domain.repository

import com.achievemeaalk.freedjf.data.db.recurring.RecurringTransactionDao
import com.achievemeaalk.freedjf.data.db.transactions.TransactionDao
import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import com.achievemeaalk.freedjf.data.model.RecurringTransaction
import com.achievemeaalk.freedjf.data.model.RecurrenceFrequency
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.domain.service.BalanceCalculationService
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepository @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao, // Inject TransactionDao for creating transactions
    private val balanceCalculationService: BalanceCalculationService
) {
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getAllRecurringTransactions()
    }

    fun getActiveRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getActiveRecurringTransactions()
    }

    fun getSubscriptions(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getSubscriptions()
    }

    fun getActiveSubscriptions(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getActiveSubscriptions()
    }

    fun getBills(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getBills()
    }


    fun getActiveBills(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getActiveBills()
    }



    fun getRecurringTransactionsDueBefore(timestamp: Long): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getRecurringTransactionsDueBefore(timestamp)
    }

    fun getActiveRecurringTransactionsDueBefore(timestamp: Long): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getActiveRecurringTransactionsDueBefore(timestamp)
    }

    fun getActiveRecurringTransactionsDueBetween(startTime: Long, endTime: Long): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getActiveRecurringTransactionsDueBetween(startTime, endTime)
    }

    suspend fun getRecurringTransactionById(id: Int): RecurringTransaction? {
        return recurringTransactionDao.getRecurringTransactionById(id)
    }

    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long {
        return recurringTransactionDao.insertRecurringTransaction(recurringTransaction)
    }

    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringTransactionDao.updateRecurringTransaction(recurringTransaction)
    }

    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringTransactionDao.deleteRecurringTransaction(recurringTransaction)
    }

    suspend fun deleteRecurringTransactionById(id: Int) {
        recurringTransactionDao.deleteRecurringTransactionById(id)
    }

    suspend fun updateNextDueDate(id: Int, nextDueDate: Long) {
        recurringTransactionDao.updateNextDueDate(id, nextDueDate)
    }

    /**
     * Calculate the next due date based on frequency
     */
    fun calculateNextDueDate(currentDueDate: Long, frequency: RecurrenceFrequency): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDueDate

        when (frequency) {
            RecurrenceFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurrenceFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RecurrenceFrequency.QUARTERLY -> calendar.add(Calendar.MONTH, 3)
            RecurrenceFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }

        return calendar.timeInMillis
    }

    /**
     * Mark a recurring transaction as processed, create a new transaction record,
     * and update its next due date.
     */
    suspend fun markAsProcessed(recurringTransaction: RecurringTransaction) {
        // 1. Create a new transaction from the recurring one
        val newTransaction = MyFinTransaction(
            description = recurringTransaction.name,
            amount = recurringTransaction.amount,
            type = recurringTransaction.transactionType,
            dateTimestamp = recurringTransaction.nextDueDate,
            accountId = recurringTransaction.accountId,
            categoryId = recurringTransaction.categoryId
        )
        transactionDao.insertTransaction(newTransaction)
        
        // 2. Update account balance for the new transaction
        balanceCalculationService.updateBalanceForNewTransaction(newTransaction)

        // 3. Calculate and update the next due date for the recurring transaction
        val nextDueDate = calculateNextDueDate(recurringTransaction.nextDueDate, recurringTransaction.frequency)
        updateNextDueDate(recurringTransaction.id, nextDueDate)
    }
}