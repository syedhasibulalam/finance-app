package com.achievemeaalk.freedjf.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.achievemeaalk.freedjf.data.model.TransactionType

enum class RecurrenceFrequency {
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

@Entity(
    tableName = "recurring_transactions",
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
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Double,
    val accountId: Int, // Added field for linking to account
    val categoryId: Int,
    val nextDueDate: Long, // Timestamp of next due date
    val frequency: RecurrenceFrequency, // Using Enum for type safety
    val isSubscription: Boolean = false, // true for subscriptions, false for bills
    val isActive: Boolean = true, // Added for pausing/resuming
    val transactionType: TransactionType = TransactionType.EXPENSE // Support recurring income; default preserved
) 