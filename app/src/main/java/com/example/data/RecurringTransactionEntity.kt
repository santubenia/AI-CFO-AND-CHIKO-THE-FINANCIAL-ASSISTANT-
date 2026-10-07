package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a recurring automatic transaction (Daily, Weekly, or Monthly).
 * Persisted in the Room database under table 'recurring_transactions'.
 */
@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val frequency: String = "MONTHLY", // "DAILY", "WEEKLY", "MONTHLY"
    val startDate: Long = System.currentTimeMillis(),
    val nextDueDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Bank Transfer",
    val isActive: Boolean = true,
    val autoExecute: Boolean = true, // Whether to auto-log in Room transactions table
    val lastExecutedDate: Long? = null,
    val notes: String = ""
)
