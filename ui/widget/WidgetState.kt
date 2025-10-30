package com.achievemeaalk.freedjf.ui.widget

data class WidgetState(
    val bills: List<Bill>,
    val isPremium: Boolean = false
)

data class Bill(
    val id: Int, // Add this field
    val name: String,
    val amount: String,
    val dueDate: String,
    val isOverdue: Boolean
)
