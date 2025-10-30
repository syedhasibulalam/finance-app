package com.achievemeaalk.freedjf.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["accountId"],
            childColumns = ["accountId"],
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
data class MyFinTransaction(
    @PrimaryKey(autoGenerate = true)
    val transactionId: Int = 0,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val accountId: Int,
    val categoryId: Int?,
    val destinationAccountId: Int? = null
)

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
} 