package com.achievemeaalk.freedjf.domain.repository

import com.achievemeaalk.freedjf.data.db.transactions.TransactionDao
import com.achievemeaalk.freedjf.data.model.MonthlyCashFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReportsRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getCashFlow(): Flow<List<MonthlyCashFlow>> {
        return transactionDao.getMonthlyCashFlow()
    }
}
