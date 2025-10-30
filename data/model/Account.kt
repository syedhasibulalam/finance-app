package com.achievemeaalk.freedjf.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val accountId: Int = 0,
    val name: String,
    val balance: Double,
    val icon: String,
    val accountType: String,
    val creditLimit: Double? = null
)