package com.example.model

import com.example.util.DateUtils

enum class BudgetPeriod(
    val id: String,
    val title: String,
    val shortLabel: String,
    val description: String
) {
    MONTHLY(
        id = "MONTHLY",
        title = "Monthly",
        shortLabel = "Month",
        description = "Current calendar month"
    ),
    QUARTERLY(
        id = "QUARTERLY",
        title = "Quarterly",
        shortLabel = "Quarter",
        description = "Current quarter (3 months)"
    ),
    HALF_YEARLY(
        id = "HALF_YEARLY",
        title = "Half-Yearly",
        shortLabel = "6 Months",
        description = "Current half-year (6 months)"
    ),
    YEARLY(
        id = "YEARLY",
        title = "Yearly",
        shortLabel = "Year",
        description = "Full calendar year"
    );

    fun getDateRange(timestamp: Long = System.currentTimeMillis()): Pair<Long, Long> {
        return when (this) {
            MONTHLY -> DateUtils.getStartOfMonth(timestamp) to DateUtils.getEndOfMonth(timestamp)
            QUARTERLY -> DateUtils.getStartOfQuarter(timestamp) to DateUtils.getEndOfQuarter(timestamp)
            HALF_YEARLY -> DateUtils.getStartOfHalfYear(timestamp) to DateUtils.getEndOfHalfYear(timestamp)
            YEARLY -> DateUtils.getStartOfYear(timestamp) to DateUtils.getEndOfYear(timestamp)
        }
    }

    fun getFormattedPeriodLabel(timestamp: Long = System.currentTimeMillis()): String {
        return when (this) {
            MONTHLY -> DateUtils.formatMonthYear(timestamp)
            QUARTERLY -> DateUtils.formatQuarterYear(timestamp)
            HALF_YEARLY -> DateUtils.formatHalfYear(timestamp)
            YEARLY -> DateUtils.formatYear(timestamp)
        }
    }

    fun getDaysLeft(): Int {
        return when (this) {
            MONTHLY -> DateUtils.getDaysLeftInCurrentMonth()
            QUARTERLY -> DateUtils.getDaysLeftInCurrentQuarter()
            HALF_YEARLY -> DateUtils.getDaysLeftInCurrentHalfYear()
            YEARLY -> DateUtils.getDaysLeftInCurrentYear()
        }
    }

    companion object {
        fun fromId(id: String?): BudgetPeriod {
            return entries.find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) } ?: MONTHLY
        }
    }
}
