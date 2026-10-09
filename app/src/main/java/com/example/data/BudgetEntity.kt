package com.example.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.model.BudgetPeriod

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String, // format: "${category}_${period}"
    val category: String, // "OVERALL" or specific category name
    val period: String = BudgetPeriod.MONTHLY.id, // "MONTHLY", "QUARTERLY", "HALF_YEARLY", "YEARLY"
    val limitAmount: Double = 0.0,
    val alertThresholdPercent: Double = 80.0 // alerts when nearing limit e.g. at 80%
) {
    @Ignore
    constructor(category: String, monthlyLimit: Double) : this(
        id = "${category}_${BudgetPeriod.MONTHLY.id}",
        category = category,
        period = BudgetPeriod.MONTHLY.id,
        limitAmount = monthlyLimit,
        alertThresholdPercent = 80.0
    )

    val monthlyLimit: Double get() = limitAmount
    val budgetPeriod: BudgetPeriod get() = BudgetPeriod.fromId(period)

    companion object {
        fun createId(category: String, period: BudgetPeriod): String {
            return "${category}_${period.id}"
        }
    }
}
