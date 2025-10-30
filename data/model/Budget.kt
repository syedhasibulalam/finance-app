package com.achievemeaalk.freedjf.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val budgetId: Int = 0,
    val month: Int,
    val year: Int,
    val observations: String
) 