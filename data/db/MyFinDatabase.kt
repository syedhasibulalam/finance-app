package com.achievemeaalk.freedjf.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.achievemeaalk.freedjf.data.db.accounts.AccountDao
import com.achievemeaalk.freedjf.data.db.budgets.BudgetDao
import com.achievemeaalk.freedjf.data.db.categories.CategoryDao
import com.achievemeaalk.freedjf.data.db.recurring.RecurringTransactionDao
import com.achievemeaalk.freedjf.data.db.transactions.TransactionDao
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.data.model.Budget
import com.achievemeaalk.freedjf.data.model.BudgetCategory
import com.achievemeaalk.freedjf.data.model.Category
import com.achievemeaalk.freedjf.data.model.MyFinTransaction
import com.achievemeaalk.freedjf.data.model.RecurringTransaction


@Database(
    entities = [
        MyFinTransaction::class,
        Account::class,
        Budget::class,
        BudgetCategory::class,
        Category::class,
        RecurringTransaction::class
    ],
    version = 21,
    exportSchema = true
)
abstract class MyFinDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun databaseDao(): DatabaseDao
}
