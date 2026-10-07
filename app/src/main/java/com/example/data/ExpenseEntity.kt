package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * TransactionEntity representing financial transaction records in the Room database.
 * Stores core transaction attributes: amount, category, date, and description.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val paymentMethod: String = "Cash",
    val note: String = "",
    val tag: String = "",
    // Aliases to seamlessly support UI title and timestamp bindings
    val title: String = description.ifEmpty { category },
    val timestamp: Long = date
)

typealias ExpenseEntity = TransactionEntity
