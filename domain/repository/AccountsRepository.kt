package com.achievemeaalk.freedjf.domain.repository

import com.achievemeaalk.freedjf.data.db.accounts.AccountDao
import com.achievemeaalk.freedjf.data.model.Account
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountsRepository @Inject constructor(
    private val accountDao: AccountDao
) {
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()

    fun getAccount(id: Int): Flow<Account?> = accountDao.getAccount(id)

    suspend fun insertAccount(account: Account): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = accountDao.deleteAccount(account)
    
    suspend fun updateAccountBalance(accountId: Int, newBalance: Double) = accountDao.updateAccountBalance(accountId, newBalance)
    suspend fun calculateAccountBalanceFromTransactions(accountId: Int): Double = accountDao.calculateAccountBalanceFromTransactions(accountId)
} 