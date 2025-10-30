package com.achievemeaalk.freedjf.domain.repository

import com.achievemeaalk.freedjf.data.db.budgets.BudgetCategoryWithCategory
import com.achievemeaalk.freedjf.data.db.budgets.BudgetDao
import com.achievemeaalk.freedjf.data.model.Budget
import com.achievemeaalk.freedjf.data.model.BudgetCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BudgetsRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()

    fun getBudget(id: Int): Flow<Budget?> = budgetDao.getBudget(id)

    fun getBudgetByMonthAndYear(month: Int, year: Int): Flow<Budget?> = budgetDao.getBudgetByMonthAndYear(month, year)

    suspend fun insertBudget(budget: Budget): Long = budgetDao.insertBudget(budget)

    suspend fun updateBudget(budget: Budget) = budgetDao.updateBudget(budget)

    suspend fun deleteBudget(budget: Budget) = budgetDao.deleteBudget(budget)

    fun getBudgetCategoriesWithCategory(budgetId: Int): Flow<List<BudgetCategoryWithCategory>> =
        budgetDao.getBudgetCategoriesWithCategory(budgetId)

    suspend fun insertBudgetCategory(budgetCategory: BudgetCategory) =
        budgetDao.insertBudgetCategory(budgetCategory)

    suspend fun updateBudgetCategory(budgetCategory: BudgetCategory) =
        budgetDao.updateBudgetCategory(budgetCategory)

    suspend fun deleteBudgetCategory(budgetCategory: BudgetCategory) =
        budgetDao.deleteBudgetCategory(budgetCategory)
} 