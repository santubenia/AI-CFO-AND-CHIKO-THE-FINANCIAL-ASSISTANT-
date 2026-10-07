package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SaleEntity representing a daily sales record in the Room database.
 * Tracks product sales, selling price, unit cost, quantity, revenue, and gross profit margin.
 */
@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val quantity: Int = 1,
    val sellingPrice: Double,
    val costPrice: Double = 0.0,
    val totalRevenue: Double = sellingPrice * quantity,
    val totalProfit: Double = (sellingPrice - costPrice) * quantity,
    val profitMarginPercent: Double = if (sellingPrice * quantity > 0) {
        (((sellingPrice - costPrice) * quantity) / (sellingPrice * quantity)) * 100.0
    } else 0.0,
    val date: Long = System.currentTimeMillis(),
    val customerName: String = "",
    val paymentMethod: String = "Cash",
    val notes: String = ""
)
