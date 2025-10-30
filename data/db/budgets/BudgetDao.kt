package com.achievemeaalk.freedjf.data.db.budgets

import androidx.room.*
import com.achievemeaalk.freedjf.data.model.Budget
import com.achievemeaalk.freedjf.data.model.BudgetCategory
import com.achievemeaalk.freedjf.data.model.Category
import kotlinx.coroutines.flow.Flow

// New data class to hold joined data
data class BudgetCategoryWithCategory(
    @Embedded val budgetCategory: BudgetCategory,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val category: Category
)

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE budgetId = :id")
    fun getBudget(id: Int): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    fun getBudgetByMonthAndYear(month: Int, year: Int): Flow<Budget?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategory(budgetCategory: BudgetCategory)

    @Update
    suspend fun updateBudgetCategory(budgetCategory: BudgetCategory)

    @Delete
    suspend fun deleteBudgetCategory(budgetCategory: BudgetCategory)


    @Transaction
    @Query("SELECT * FROM budget_categories WHERE budgetId = :budgetId")
    fun getBudgetCategoriesWithCategory(budgetId: Int): Flow<List<BudgetCategoryWithCategory>>

    @Query("DELETE FROM budgets")
    suspend fun clearAllBudgets()
}
