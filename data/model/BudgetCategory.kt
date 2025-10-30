package com.achievemeaalk.freedjf.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_categories",
    foreignKeys = [
        ForeignKey(
        entity = Budget::class,
        parentColumns = ["budgetId"],
        childColumns = ["budgetId"],
        onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BudgetCategory(
    @PrimaryKey(autoGenerate = true)
    val budgetCategoryId: Int = 0,
    val budgetId: Int,
    val categoryId: Int,
    val plannedAmount: Double,
    val reminderEnabled: Boolean = false
) 