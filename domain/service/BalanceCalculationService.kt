package com.achievemeaalk.freedjf.domain.service

import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import com.achievemeaalk.freedjf.data.model.TransactionType
import com.achievemeaalk.freedjf.domain.repository.AccountsRepository
import com.achievemeaalk.freedjf.domain.repository.TransactionsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceCalculationService @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val transactionsRepository: TransactionsRepository
) {
    
    /**
     * Recalculates and updates the balance for a specific account based on all its transactions
     */
    suspend fun recalculateAccountBalance(accountId: Int) {
        val calculatedBalance = accountsRepository.calculateAccountBalanceFromTransactions(accountId)
        accountsRepository.updateAccountBalance(accountId, calculatedBalance)
    }
    
    /**
     * Recalculates and updates balances for all accounts
     */
    suspend fun recalculateAllAccountBalances() {
        val accounts = accountsRepository.getAllAccounts().first()
        accounts.forEach { account ->
            recalculateAccountBalance(account.accountId)
        }
    }
    
    /**
     * Updates account balance when a transaction is added
     */
    suspend fun updateBalanceForNewTransaction(transaction: MyFinTransaction) {
        val currentBalance = accountsRepository.calculateAccountBalanceFromTransactions(transaction.accountId)
        accountsRepository.updateAccountBalance(transaction.accountId, currentBalance)
        
        // Handle transfer transactions - update destination account too
        if (transaction.type == TransactionType.TRANSFER && transaction.destinationAccountId != null) {
            val destinationBalance = accountsRepository.calculateAccountBalanceFromTransactions(transaction.destinationAccountId!!)
            accountsRepository.updateAccountBalance(transaction.destinationAccountId!!, destinationBalance)
        }
    }
    
    /**
     * Updates account balance when a transaction is modified
     */
    suspend fun updateBalanceForModifiedTransaction(oldTransaction: MyFinTransaction, newTransaction: MyFinTransaction) {
        // Recalculate balance for the original account
        val originalAccountBalance = accountsRepository.calculateAccountBalanceFromTransactions(oldTransaction.accountId)
        accountsRepository.updateAccountBalance(oldTransaction.accountId, originalAccountBalance)
        
        // Recalculate balance for the new account (if changed)
        if (oldTransaction.accountId != newTransaction.accountId) {
            val newAccountBalance = accountsRepository.calculateAccountBalanceFromTransactions(newTransaction.accountId)
            accountsRepository.updateAccountBalance(newTransaction.accountId, newAccountBalance)
        }
        
        // Handle transfer transactions
        if (oldTransaction.type == TransactionType.TRANSFER && oldTransaction.destinationAccountId != null) {
            val oldDestinationBalance = accountsRepository.calculateAccountBalanceFromTransactions(oldTransaction.destinationAccountId!!)
            accountsRepository.updateAccountBalance(oldTransaction.destinationAccountId!!, oldDestinationBalance)
        }
        
        if (newTransaction.type == TransactionType.TRANSFER && newTransaction.destinationAccountId != null) {
            val newDestinationBalance = accountsRepository.calculateAccountBalanceFromTransactions(newTransaction.destinationAccountId!!)
            accountsRepository.updateAccountBalance(newTransaction.destinationAccountId!!, newDestinationBalance)
        }
    }
    
    /**
     * Updates account balance when a transaction is deleted
     */
    suspend fun updateBalanceForDeletedTransaction(transaction: MyFinTransaction) {
        val accountBalance = accountsRepository.calculateAccountBalanceFromTransactions(transaction.accountId)
        accountsRepository.updateAccountBalance(transaction.accountId, accountBalance)
        
        // Handle transfer transactions
        if (transaction.type == TransactionType.TRANSFER && transaction.destinationAccountId != null) {
            val destinationBalance = accountsRepository.calculateAccountBalanceFromTransactions(transaction.destinationAccountId!!)
            accountsRepository.updateAccountBalance(transaction.destinationAccountId!!, destinationBalance)
        }
    }
}

