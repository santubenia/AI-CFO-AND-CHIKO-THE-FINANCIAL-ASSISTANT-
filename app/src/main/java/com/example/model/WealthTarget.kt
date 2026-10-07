package com.example.model

data class WealthTarget(
    val category: String, // "BANK", "MUTUAL_FUND", "STOCK_INDIAN", "STOCK_FOREIGN", "EPFO", "TOTAL_NET_WORTH"
    val title: String,
    val targetAmount: Double,
    val targetDateMonthYear: String = "Dec 2027",
    val monthlyContributionNeeded: Double = 12500.0,
    val notes: String = "Strategic wealth building milestone"
)

object DefaultWealthTargets {
    fun getDefaultTargets(): List<WealthTarget> {
        return listOf(
            WealthTarget(
                category = "BANK",
                title = "Emergency Liquid Bank Buffer",
                targetAmount = 350000.0,
                targetDateMonthYear = "Dec 2025",
                monthlyContributionNeeded = 8500.0,
                notes = "6 months of living expenses in high-yield liquid accounts"
            ),
            WealthTarget(
                category = "MUTUAL_FUND",
                title = "Mutual Fund Wealth Corpus",
                targetAmount = 1000000.0,
                targetDateMonthYear = "Dec 2026",
                monthlyContributionNeeded = 22000.0,
                notes = "Automated SIP compounding in Nifty 50 & Flexi-cap"
            ),
            WealthTarget(
                category = "STOCK_INDIAN",
                title = "Indian Bluechip Direct Equities",
                targetAmount = 750000.0,
                targetDateMonthYear = "Jun 2027",
                monthlyContributionNeeded = 15000.0,
                notes = "Long term holdings in India's top infrastructure and banking champions"
            ),
            WealthTarget(
                category = "STOCK_FOREIGN",
                title = "US Tech & S&P 500 Global Growth",
                targetAmount = 500000.0,
                targetDateMonthYear = "Dec 2027",
                monthlyContributionNeeded = 10000.0,
                notes = "Global artificial intelligence infrastructure & currency hedge"
            ),
            WealthTarget(
                category = "EPFO",
                title = "EPFO & Pension Retirement Goal",
                targetAmount = 1500000.0,
                targetDateMonthYear = "Dec 2028",
                monthlyContributionNeeded = 18000.0,
                notes = "Compounding with sovereign 8.25% tax-free interest"
            ),
            WealthTarget(
                category = "TOTAL_NET_WORTH",
                title = "Milestone Consolidated Net Worth",
                targetAmount = 4500000.0,
                targetDateMonthYear = "Dec 2028",
                monthlyContributionNeeded = 65000.0,
                notes = "Combined wealth corpus across all assets"
            )
        )
    }
}
