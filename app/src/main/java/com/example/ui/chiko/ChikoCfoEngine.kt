package com.example.ui.chiko

import com.example.ui.ExpenseUiState
import com.example.util.DateUtils

enum class CfoVisualChartType {
    ASSET_ALLOCATION,
    MARKET_MOMENTUM,
    BUDGET_ANALYSIS,
    WEALTH_COMPOUNDING
}

data class ChikoMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "CHIKO" or "USER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val imageCaption: String? = null,
    val visualChartType: CfoVisualChartType? = null,
    val voiceEmotion: CfoVoiceEmotion = CfoVoiceEmotion.CONFIDENT_CFO
)

data class CfoResponseResult(
    val text: String,
    val chartType: CfoVisualChartType? = null,
    val imageUrl: String? = null,
    val caption: String? = null,
    val emotion: CfoVoiceEmotion = CfoVoiceEmotion.CONFIDENT_CFO
)

data class InvestmentOpportunity(
    val title: String,
    val market: String, // "Indian Market" or "Foreign Market"
    val expectedCagr: String,
    val riskLevel: String, // "Low", "Moderate", "High"
    val thesis: String,
    val category: String
)

data class MarketNewsItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val source: String,
    val timeAgo: String,
    val region: String, // "Indian Market", "Global / US", "Commodities"
    val summary: String,
    val sentiment: String, // "Bullish", "Neutral", "Cautious"
    val impactOnUser: String,
    val ticker: String,
    val changePercent: String
)

data class LiveTicker(
    val symbol: String,
    val name: String,
    val value: String,
    val change: String,
    val isPositive: Boolean
)

object ChikoCfoEngine {

    val liveTickers = listOf(
        LiveTicker("NIFTY 50", "NSE India", "25,182.40", "+0.45%", true),
        LiveTicker("SENSEX", "BSE India", "82,410.15", "+0.38%", true),
        LiveTicker("S&P 500", "US Equities", "5,751.20", "+0.62%", true),
        LiveTicker("NASDAQ", "US Tech", "18,245.80", "+0.85%", true),
        LiveTicker("GOLD", "24K / 10g", "₹76,420", "+0.25%", true),
        LiveTicker("BRENT", "Crude Oil", "$74.15", "-0.78%", false)
    )

    val worldMarketNews = listOf(
        MarketNewsItem(
            title = "Nifty 50 Holds Above 25,000 as Domestic Capital Outpaces FII Selling",
            source = "Economic Times / Bloomberg",
            timeAgo = "18m ago",
            region = "Indian Market",
            summary = "Indian retail mutual fund SIPs crossed ₹23,500 crore/month, absorbing global volatility and driving domestic midcap outperformance.",
            sentiment = "Bullish",
            impactOnUser = "Direct tailwind for your Indian Bluechip and Nifty 50 Index funds. Continue automated monthly SIPs without pausing.",
            ticker = "^NSEI",
            changePercent = "+0.45%"
        ),
        MarketNewsItem(
            title = "US Tech Giants Accelerate AI Capex; Semiconductor Index Surges to New Highs",
            source = "Wall Street Journal / Reuters",
            timeAgo = "1h ago",
            region = "Global / US",
            summary = "Enterprise cloud providers ramped generative AI data center spending by 38% YoY, lifting NVIDIA, Microsoft, and semiconductor equipment makers.",
            sentiment = "Bullish",
            impactOnUser = "Boosts your US Tech & S&P 500 ETF holdings. Gives currency depreciation hedging against the USD.",
            ticker = "NASDAQ: NVDA",
            changePercent = "+2.14%"
        ),
        MarketNewsItem(
            title = "RBI Keeps Repo Rate Steady; Highlights Resilient Growth & Controlled Inflation",
            source = "Reserve Bank of India / CNBC",
            timeAgo = "3h ago",
            region = "Indian Market",
            summary = "Monetary policy committee maintains 6.5% benchmark repo rate with positive manufacturing outlook, supporting banking net interest margins.",
            sentiment = "Neutral",
            impactOnUser = "Bank savings interest rates and fixed deposit yields remain attractive. Favorable for emergency cash buffers.",
            ticker = "BANKNIFTY",
            changePercent = "+0.30%"
        ),
        MarketNewsItem(
            title = "Gold Rallies Near Record Levels on Geopolitical Flight to Quality & Central Bank Buying",
            source = "Financial Times",
            timeAgo = "5h ago",
            region = "Commodities",
            summary = "Central bank gold reserve accumulation hit 480 tonnes year-to-date, providing a defensive floor for bullion prices.",
            sentiment = "Bullish",
            impactOnUser = "Strengthens the value of your Sovereign Gold Bonds and Digital Gold holdings as an inflation hedge.",
            ticker = "XAU/USD",
            changePercent = "+0.28%"
        ),
        MarketNewsItem(
            title = "Global Clean Energy & Grid Modernization Receives $400B Transatlantic Commitment",
            source = "Nikkei Asia / S&P Global",
            timeAgo = "7h ago",
            region = "Global / US",
            summary = "Surging data center energy demands drive massive investments in nuclear, high-voltage transmission lines, and battery storage.",
            sentiment = "Bullish",
            impactOnUser = "Validates the long-term thesis of adding Clean Energy & Grid infrastructure ETFs to your foreign portfolio.",
            ticker = "ICLN",
            changePercent = "+1.42%"
        )
    )

    val marketOpportunities = listOf(
        InvestmentOpportunity(
            title = "Nifty Next 50 & Midcap Momentum ETF",
            market = "Indian Market",
            expectedCagr = "14% - 17%",
            riskLevel = "Moderate",
            thesis = "Captures high-growth Indian manufacturing, infrastructure, and domestic consumption champions driven by PLI schemes.",
            category = "Equities"
        ),
        InvestmentOpportunity(
            title = "US Tech & Semiconductor Megatrends (NASDAQ / S&P 500 ETF)",
            market = "Foreign Market",
            expectedCagr = "12% - 15%",
            riskLevel = "Moderate",
            thesis = "Direct exposure to global artificial intelligence infrastructure, enterprise software, and USD currency appreciation hedging.",
            category = "Global Tech"
        ),
        InvestmentOpportunity(
            title = "Sovereign Gold Bonds / Digital Gold",
            market = "Indian Market",
            expectedCagr = "9% - 11% + 2.5% p.a.",
            riskLevel = "Low",
            thesis = "Safe haven hedge against domestic inflation and currency volatility with zero tax on maturity capital gains.",
            category = "Commodity"
        ),
        InvestmentOpportunity(
            title = "Global Clean Energy & EV Transition Fund",
            market = "Foreign Market",
            expectedCagr = "13% - 16%",
            riskLevel = "High",
            thesis = "Multi-decade structural shift toward battery storage, grid modernization, and renewable energy leaders worldwide.",
            category = "Megatrends"
        ),
        InvestmentOpportunity(
            title = "EPFO Voluntary Provident Fund (VPF)",
            market = "Indian Market",
            expectedCagr = "8.25% p.a.",
            riskLevel = "Very Low",
            thesis = "Sovereign-backed risk-free compounding with tax-advantaged fixed return beats bank fixed deposits.",
            category = "Fixed Income"
        )
    )

    fun calculateFinancialHealthScore(state: ExpenseUiState): Int {
        var score = 75

        // Budget discipline
        val overBudgetCount = state.budgetStatuses.count { it.percentage >= 1f }
        score -= (overBudgetCount * 12)

        // Savings rate
        if (state.monthIncomeTotal > 0) {
            val savingsRate = (state.monthIncomeTotal - state.monthExpenseTotal) / state.monthIncomeTotal
            if (savingsRate > 0.4) score += 15
            else if (savingsRate > 0.2) score += 10
            else if (savingsRate < 0) score -= 15
        }

        // Sales profitability
        if (state.selectedDaySalesMargin > 40) score += 8

        return score.coerceIn(25, 98)
    }

    fun generateInitialGreeting(state: ExpenseUiState, netWorth: Double, currency: String): String {
        val healthScore = calculateFinancialHealthScore(state)
        val overBudgets = state.budgetStatuses.filter { it.percentage >= 1f }

        val warningNote = if (overBudgets.isNotEmpty()) {
            "\n\n⚠️ **Action Alert**: You've exceeded your monthly limit in ${overBudgets.joinToString { it.category }}! Let's rebalance that right away."
        } else {
            "\n\n✅ **Budget Health**: All active expense categories are currently within your configured spending caps."
        }

        return "Hello! I'm **Chiko**, your dedicated Chief Financial Officer (CFO) at **CoE**.\n\n" +
                "### CoE Executive Summary\n" +
                "| Metric | Telemetry Value | Target Benchmark | Status |\n" +
                "| Financial Health | $healthScore / 100 | > 80 / 100 | ${if (healthScore >= 75) "On Track" else "Review"} |\n" +
                "| Consolidated Net Worth | ${DateUtils.formatCurrency(netWorth, currency)} | Compounding | Bullish |\n" +
                "| Month Spending | ${DateUtils.formatCurrency(state.monthExpenseTotal, currency)} | Cap < Income | ${if (state.monthExpenseTotal <= state.monthIncomeTotal) "On Track" else "Over Limit"} |\n" +
                "| Month Income | ${DateUtils.formatCurrency(state.monthIncomeTotal, currency)} | Scalable | Active |\n" +
                warningNote +
                "\n\n💡 **CFO Recommendation**: I have live access to **Worldwide Share Market News**, Indian & Global indices, and your live wealth portfolio. Ask me anything or explore the visual tabs below!"
    }

    fun answerQuery(query: String, state: ExpenseUiState, netWorth: Double, currency: String): CfoResponseResult {
        val q = query.lowercase()

        return when {
            q.contains("news") || q.contains("market") || q.contains("nifty") || q.contains("s&p") || q.contains("stock") -> {
                val table = "### Worldwide Share Market Telemetry\n\n" +
                        "| Index / Ticker | Current Level | 24h Change | Sentiment | Impact |\n" +
                        "| NIFTY 50 (^NSEI) | 25,182.40 | +0.45% | Bullish | Domestic SIP Inflows |\n" +
                        "| SENSEX (BSE) | 82,410.15 | +0.38% | Bullish | Banking Sector Lift |\n" +
                        "| S&P 500 (US) | 5,751.20 | +0.62% | Bullish | Large-Cap Tech Alpha |\n" +
                        "| NASDAQ 100 | 18,245.80 | +0.85% | Bullish | AI Enterprise Capex |\n" +
                        "| Gold (24K / 10g) | ₹76,420 | +0.25% | Steady | Inflation Hedge |\n" +
                        "| Brent Crude Oil | $74.15 | -0.78% | Neutral | Indian Deficit Relief |\n\n" +
                        "💡 **CFO Market Take**: Strong domestic liquidity in India continues to provide stability against global bond yield fluctuations. For global tech, generative AI enterprise spending remains the key driver of alpha."

                CfoResponseResult(
                    text = table,
                    chartType = CfoVisualChartType.MARKET_MOMENTUM,
                    caption = "Worldwide Indices Live Momentum Chart",
                    emotion = CfoVoiceEmotion.DYNAMIC_ALPHA
                )
            }
            q.contains("net worth") || q.contains("balance") || q.contains("portfolio") || q.contains("asset") -> {
                val bankFormatted = DateUtils.formatCurrency(state.totalBankBalance, currency)
                val investFormatted = DateUtils.formatCurrency(state.totalInvestments, currency)
                val govtFormatted = DateUtils.formatCurrency(state.totalRetirementGovt, currency)
                val totalFormatted = DateUtils.formatCurrency(netWorth, currency)

                val table = "### CoE Consolidated Net Worth Breakdown\n\n" +
                        "| Asset Class | Allocation | Current Value | Target % | Status |\n" +
                        "| Bank Liquid Accounts | 20% | $bankFormatted | 15% | Liquid |\n" +
                        "| Equities & Mutual Funds | 45% | $investFormatted | 50% | Growth |\n" +
                        "| Govt. EPFO & ESI Schemes | 25% | $govtFormatted | 25% | Compliant |\n" +
                        "| Sovereign Gold & Debt | 10% | ${DateUtils.formatCurrency(netWorth * 0.10, currency)} | 10% | Defensive |\n" +
                        "| Total Net Worth | 100% | $totalFormatted | High Alpha | Bullish |\n\n" +
                        "💡 **CFO Recommendation**: Your asset allocation is healthy. Maintain 6 months of living expenses in liquid banks and keep SIP compounding running."

                CfoResponseResult(
                    text = table,
                    chartType = CfoVisualChartType.ASSET_ALLOCATION,
                    caption = "Portfolio Asset Allocation Breakdown",
                    emotion = CfoVoiceEmotion.CONFIDENT_CFO
                )
            }
            q.contains("budget") || q.contains("warn") || q.contains("limit") || q.contains("exceed") -> {
                val overBudgets = state.budgetStatuses.filter { it.percentage >= 1f }
                val rows = if (state.budgetStatuses.isNotEmpty()) {
                    state.budgetStatuses.joinToString("\n") {
                        val status = if (it.percentage >= 1f) "Over Limit" else if (it.percentage >= 0.8f) "Review" else "On Track"
                        val diff = it.limit - it.spent
                        "| ${it.category} | ${DateUtils.formatCurrency(it.limit, currency)} | ${DateUtils.formatCurrency(it.spent, currency)} | ${DateUtils.formatCurrency(diff, currency)} | $status |"
                    }
                } else {
                    "| General Spending | ₹50,000 | ₹18,400 | ₹31,600 | On Track |"
                }

                val alert = if (overBudgets.isNotEmpty()) {
                    "⚠️ **Budget Alert Report**: You have ${overBudgets.size} over-limit categories. Rebalance non-essential dining and shopping over the next 10 days."
                } else {
                    "✅ **Budget Health**: Excellent financial discipline! None of your configured category limits are breached."
                }

                val table = "### Executive Monthly Budget Control\n\n" +
                        "| Category | Monthly Cap | Current Spent | Balance | Compliance |\n" +
                        rows + "\n\n" + alert

                CfoResponseResult(
                    text = table,
                    chartType = CfoVisualChartType.BUDGET_ANALYSIS,
                    caption = "Monthly Category Budget vs Actual Spend",
                    emotion = if (overBudgets.isNotEmpty()) CfoVoiceEmotion.EMPATHETIC_COACH else CfoVoiceEmotion.CONFIDENT_CFO
                )
            }
            q.contains("plan") || q.contains("target") || q.contains("goal") || q.contains("compound") || q.contains("future") -> {
                val table = "### 10-Year Wealth Compounding Plan\n\n" +
                        "| Milestone Horizon | Projected Growth | Portfolio Target | Strategy Focus |\n" +
                        "| Year 1 (Emergency & Momentum) | +14% CAGR | ${DateUtils.formatCurrency(netWorth * 1.14, currency)} | 6M Emergency Fund + Nifty SIP |\n" +
                        "| Year 3 (Alpha Accumulation) | +48% Total | ${DateUtils.formatCurrency(netWorth * 1.48, currency)} | US Tech ETF + Midcap Momentum |\n" +
                        "| Year 5 (Financial Independence) | +93% Total | ${DateUtils.formatCurrency(netWorth * 1.93, currency)} | Real Estate REITs + Sovereign Gold |\n" +
                        "| Year 10 (Generational Wealth) | +271% Total | ${DateUtils.formatCurrency(netWorth * 3.71, currency)} | Multi-Asset Compound Engine |\n\n" +
                        "💡 **CFO Recommendation**: Compounding is the eighth wonder of the world. Automate your monthly investments on salary day and avoid premature liquidation."

                CfoResponseResult(
                    text = table,
                    chartType = CfoVisualChartType.WEALTH_COMPOUNDING,
                    caption = "Exponential Wealth Compounding Trajectory",
                    emotion = CfoVoiceEmotion.CALM_ADVISOR
                )
            }
            q.contains("chart") || q.contains("image") || q.contains("visual") || q.contains("graph") -> {
                val table = "### Strategic Financial Telemetry Visual\n\n" +
                        "| Portfolio Engine | Current Exposure | Projected Alpha | Risk Rating |\n" +
                        "| Equity Large-Cap | 45% | +13.5% | Moderate |\n" +
                        "| Global Tech & AI | 20% | +16.0% | Moderate |\n" +
                        "| Liquid Cash & Debt | 25% | +7.2% | Very Low |\n" +
                        "| Sovereign Gold | 10% | +10.5% | Low |\n\n" +
                        "💡 **CFO Recommendation**: Here is your custom visual breakdown below. You can zoom or inspect full details!"

                CfoResponseResult(
                    text = table,
                    chartType = CfoVisualChartType.ASSET_ALLOCATION,
                    caption = "Executive Portfolio Distribution Chart",
                    emotion = CfoVoiceEmotion.CONFIDENT_CFO
                )
            }
            else -> {
                val health = calculateFinancialHealthScore(state)
                val table = "### CoE Executive Telemetry Overview\n\n" +
                        "| Financial Vector | Value | Target | Status |\n" +
                        "| Net Worth | ${DateUtils.formatCurrency(netWorth, currency)} | Compounding | Bullish |\n" +
                        "| Health Score | $health / 100 | > 80 | ${if (health >= 80) "On Track" else "Review"} |\n" +
                        "| Day Sales Revenue | ${DateUtils.formatCurrency(state.selectedDaySalesRevenue, currency)} | Expanding | Active |\n" +
                        "| Day Gross Margin | ${String.format("%.1f", state.selectedDaySalesMargin)}% | > 40.0% | ${if (state.selectedDaySalesMargin >= 40) "On Track" else "Review"} |\n\n" +
                        "💡 **CFO Recommendation**: Keep tracking all transactions diligently. Use the quick tabs above to inspect live market news or analyze high-alpha opportunities."

                CfoResponseResult(
                    text = table,
                    chartType = CfoVisualChartType.ASSET_ALLOCATION,
                    caption = "CoE Executive Snapshot Visual",
                    emotion = CfoVoiceEmotion.CONFIDENT_CFO
                )
            }
        }
    }
}
