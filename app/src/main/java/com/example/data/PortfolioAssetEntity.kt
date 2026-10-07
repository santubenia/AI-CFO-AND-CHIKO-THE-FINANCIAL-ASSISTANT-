package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Portfolio and Net Worth asset entity for CoE.
 * Tracks bank accounts, credit cards, mutual funds, Indian & foreign stocks,
 * EPFO, ESI, real estate, and digital assets.
 */
@Entity(tableName = "portfolio_assets")
data class PortfolioAssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "BANK", "CREDIT_CARD", "MUTUAL_FUND", "STOCK_INDIAN", "STOCK_FOREIGN", "EPFO", "ESI", "OTHER_ASSET"
    val balanceOrValue: Double,
    val investedAmount: Double = balanceOrValue,
    val institution: String = "",
    val accountNumberMasked: String = "",
    val returnsPercentage: Double = if (investedAmount > 0) ((balanceOrValue - investedAmount) / investedAmount) * 100.0 else 0.0,
    val isLiability: Boolean = (category == "CREDIT_CARD"),
    val lastUpdated: Long = System.currentTimeMillis(),
    val notes: String = ""
)
