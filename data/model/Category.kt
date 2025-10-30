package com.achievemeaalk.freedjf.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int = 0,
    val name: String,
    val nameResId: String?,
    val type: CategoryType,
    val icon: String,
    val description: String?,
    val descriptionResId: String?,
    val color: String
) 