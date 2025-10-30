package com.achievemeaalk.freedjf.domain.repository

import com.achievemeaalk.freedjf.data.db.transactions.TransactionDao
import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionsRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getTransactions(searchQuery: String): Flow<List<MyFinTransaction>> {
        return transactionDao.getTransactions(searchQuery)
    }

    fun getTransactionById(id: Int): Flow<MyFinTransaction?> {
        return transactionDao.getTransactionById(id)
    }

    fun getTransactionsForDateRange(startDate: Long, endDate: Long): Flow<List<MyFinTransaction>> {
        return transactionDao.getTransactionsForDateRange(startDate, endDate)
    }

    fun getAllTransactions(): Flow<List<MyFinTransaction>> {
        return transactionDao.getTransactions("")
    }

    suspend fun insertTransaction(transaction: MyFinTransaction) = transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: MyFinTransaction) = transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: MyFinTransaction) = transactionDao.deleteTransaction(transaction)
} 