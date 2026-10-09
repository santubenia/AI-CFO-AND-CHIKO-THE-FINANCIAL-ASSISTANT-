package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BudgetEntity
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.data.PortfolioAssetEntity
import com.example.data.SaleEntity
import com.example.data.TransactionEntity
import com.example.model.BudgetPeriod
import com.example.model.Categories
import com.example.ui.anumati.AnumatiPfEsiResult
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimeRange(val label: String) {
    TODAY("Today"),
    WEEK("This Week"),
    MONTH("This Month"),
    YEAR("This Year"),
    ALL("All Time")
}

data class DailySpendPoint(
    val dayLabel: String,
    val dateMillis: Long,
    val amount: Double,
    val isToday: Boolean
)

data class SalesTrendPoint(
    val dayLabel: String,
    val dateMillis: Long,
    val revenue: Double,
    val profit: Double,
    val cost: Double,
    val marginPercent: Double
)

data class ProductPerformance(
    val productName: String,
    val totalQuantity: Int,
    val totalRevenue: Double,
    val totalProfit: Double,
    val marginPercent: Double
)

data class CategorySpend(
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class BudgetStatus(
    val category: String,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val limit: Double,
    val spent: Double,
    val percentage: Float,
    val remaining: Double,
    val alertThresholdPercent: Double = 80.0,
    val isExceeded: Boolean = spent > limit && limit > 0,
    val isNearLimit: Boolean = limit > 0 && spent <= limit && (spent / limit) >= (alertThresholdPercent / 100.0)
)

data class ExpenseUiState(
    val allExpenses: List<ExpenseEntity> = emptyList(),
    val allBudgets: List<BudgetEntity> = emptyList(),
    val allSales: List<SaleEntity> = emptyList(),
    val allPortfolioAssets: List<PortfolioAssetEntity> = emptyList(),
    val selectedDate: Long = DateUtils.getStartOfDay(),
    val currencySymbol: String = "₹",
    val selectedTimeRange: TimeRange = TimeRange.MONTH,
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null,
    val selectedTypeFilter: String? = null,

    // Multi-Period Budgets & Spending Plan
    val selectedBudgetPeriod: BudgetPeriod = BudgetPeriod.MONTHLY,
    val periodExpenseTotal: Double = 0.0,
    val periodDaysLeft: Int = 30,
    val periodFormattedLabel: String = "Monthly Plan",
    val budgetStatuses: List<BudgetStatus> = emptyList(),
    val allPeriodBudgetStatuses: List<BudgetStatus> = emptyList(),
    val exceededBudgetWarnings: List<BudgetStatus> = emptyList(),
    val nearBudgetWarnings: List<BudgetStatus> = emptyList(),

    // Net Worth & Portfolio Totals
    val totalNetWorth: Double = 0.0,
    val totalBankBalance: Double = 0.0,
    val totalInvestments: Double = 0.0,
    val totalRetirementGovt: Double = 0.0,
    val totalLiabilities: Double = 0.0,

    // Computed for Selected Day
    val selectedDayExpenses: List<ExpenseEntity> = emptyList(),
    val selectedDayExpenseTotal: Double = 0.0,
    val selectedDayIncomeTotal: Double = 0.0,

    // Computed for Selected Day Sales
    val selectedDaySales: List<SaleEntity> = emptyList(),
    val selectedDaySalesRevenue: Double = 0.0,
    val selectedDaySalesCost: Double = 0.0,
    val selectedDaySalesProfit: Double = 0.0,
    val selectedDaySalesMargin: Double = 0.0,
    val selectedDayUnitsSold: Int = 0,

    // Computed for Current Month & Budgets
    val monthExpenseTotal: Double = 0.0,
    val monthIncomeTotal: Double = 0.0,
    val overallBudget: Double = 55000.0,

    // Computed for Analytics
    val analyticsExpenses: List<ExpenseEntity> = emptyList(),
    val analyticsExpenseTotal: Double = 0.0,
    val analyticsIncomeTotal: Double = 0.0,
    val categorySpends: List<CategorySpend> = emptyList(),
    val weeklyTrend: List<DailySpendPoint> = emptyList(),

    // Sales Analytics
    val analyticsSales: List<SaleEntity> = emptyList(),
    val analyticsSalesRevenue: Double = 0.0,
    val analyticsSalesCost: Double = 0.0,
    val analyticsSalesProfit: Double = 0.0,
    val analyticsSalesMargin: Double = 0.0,
    val salesAndProfitTrend: List<SalesTrendPoint> = emptyList(),
    val topSellingProducts: List<ProductPerformance> = emptyList(),

    // Filtered list for History
    val historyFilteredExpenses: List<ExpenseEntity> = emptyList(),

    // Shake to Log Feature (Battery-optimized: default off in background)
    val isShakeToLogEnabled: Boolean = false,
    val shakeSensitivity: String = "Soft (Battery Saver)",
    val shakeLaunchMode: String = "FIRST_TIME_ONLY",

    // Activity & Cloud Sync Settings
    val isGoogleFitEnabled: Boolean = false,
    val googleFitSteps: Int = 8740,
    val googleFitCalories: Int = 580,
    val isCloudBackupEnabled: Boolean = false,
    val selectedTheme: String = "SYSTEM",
    val userEmail: String = "investor@coe.app",
    val userName: String = "CoE Portfolio Member",

    // Recurring Automatic Transactions
    val recurringTransactions: List<com.example.data.RecurringTransactionEntity> = emptyList(),
    val defaultRecurringCategory: String = "Housing",
    val defaultRecurringFrequency: String = "MONTHLY",
    val defaultRecurringPaymentMethod: String = "Bank Transfer",

    // Net Worth Security Passcode
    val netWorthPasscode: String = "1234"
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("coe_app_settings", Context.MODE_PRIVATE)
    private val repository: ExpenseRepository

    private val _selectedDate = MutableStateFlow(DateUtils.getStartOfDay())
    private val _currencySymbol = MutableStateFlow(prefs.getString("DEFAULT_CURRENCY", "₹") ?: "₹")
    private val _selectedTimeRange = MutableStateFlow(TimeRange.MONTH)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    private val _selectedTypeFilter = MutableStateFlow<String?>("ALL")
    private val _selectedBudgetPeriod = MutableStateFlow(BudgetPeriod.MONTHLY)
    private val _isShakeToLogEnabled = MutableStateFlow(prefs.getBoolean("SHAKE_ENABLED", false))
    private val _shakeSensitivity = MutableStateFlow(prefs.getString("SHAKE_SENSITIVITY", "Fast (Instant 1.8G)") ?: "Fast (Instant 1.8G)")
    private val _shakeLaunchMode = MutableStateFlow(prefs.getString("SHAKE_LAUNCH_MODE", "EVERY_SHAKE") ?: "EVERY_SHAKE")

    private val _isGoogleFitEnabled = MutableStateFlow(prefs.getBoolean("GOOGLE_FIT_ENABLED", false))
    private val _isCloudBackupEnabled = MutableStateFlow(prefs.getBoolean("CLOUD_BACKUP_ENABLED", false))
    private val _selectedTheme = MutableStateFlow(prefs.getString("DEFAULT_THEME", "SYSTEM") ?: "SYSTEM")
    private val _netWorthPasscode = MutableStateFlow(prefs.getString("NET_WORTH_PASSCODE", "1234") ?: "1234")

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(
            db.transactionDao(),
            db.budgetDao(),
            db.saleDao(),
            db.portfolioDao(),
            db.recurringTransactionDao()
        )
        viewModelScope.launch {
            repository.checkAndExecuteDueRecurringTransactions()
        }
    }

    private val syncManager = com.example.data.FirestoreSyncManager(application)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _lastSyncTime = MutableStateFlow(prefs.getLong("LAST_CLOUD_SYNC_MILLIS", 0L))
    val lastSyncTime: StateFlow<Long> = _lastSyncTime

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage

    fun syncWithCloud(userId: String, onComplete: ((Boolean, String) -> Unit)? = null) {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Syncing with Firebase Firestore..."
            val allLocal = repository.allExpenses.first()
            val result = syncManager.performFullCloudSync(userId, allLocal, repository)
            _isSyncing.value = false
            if (result.isSuccess) {
                val (uploaded, downloaded) = result.getOrNull() ?: Pair(0, 0)
                val now = System.currentTimeMillis()
                _lastSyncTime.value = now
                prefs.edit().putLong("LAST_CLOUD_SYNC_MILLIS", now).apply()
                val msg = "Sync Complete: $uploaded uploaded, $downloaded downloaded"
                _syncMessage.value = msg
                onComplete?.invoke(true, msg)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Sync failed"
                _syncMessage.value = "Error: $err"
                onComplete?.invoke(false, err)
            }
        }
    }

    fun exportTransactionsCsv(context: Context) {
        viewModelScope.launch {
            val transactions = repository.allExpenses.first()
            syncManager.shareCsvBackup(context, transactions)
        }
    }

    fun importTransactionsCsv(csvText: String, onComplete: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            val result = syncManager.importTransactionsFromCsv(csvText, repository)
            onComplete(result)
        }
    }

    val uiState: StateFlow<ExpenseUiState> = combine(
        combine(
            repository.allExpenses,
            repository.allBudgets,
            repository.allSales,
            repository.allPortfolioAssets,
            repository.allRecurringTransactions
        ) { expenses, budgets, sales, assets, recurring ->
            Quint(expenses, budgets, sales, assets, recurring)
        },
        _selectedDate,
        _currencySymbol,
        combine(
            _selectedTimeRange,
            _searchQuery,
            _selectedCategoryFilter,
            _selectedTypeFilter,
            _isShakeToLogEnabled,
            _shakeSensitivity,
            _shakeLaunchMode,
            _selectedBudgetPeriod
        ) { args: Array<Any?> ->
            FilterParams(
                timeRange = args[0] as TimeRange,
                searchQuery = args[1] as String,
                selectedCategoryFilter = args[2] as? String,
                selectedTypeFilter = args[3] as? String,
                isShakeToLogEnabled = args[4] as Boolean,
                shakeSensitivity = args[5] as String,
                shakeLaunchMode = args[6] as String,
                selectedBudgetPeriod = args[7] as BudgetPeriod
            )
        },
        combine(
            _isGoogleFitEnabled,
            _isCloudBackupEnabled,
            _selectedTheme,
            _netWorthPasscode
        ) { fit, cloud, theme, passcode -> Quad(fit, cloud, theme, passcode) }
    ) { dataQuint, selectedDate, currency, filters, integrations ->
        calculateUiState(
            expenses = dataQuint.first,
            budgets = dataQuint.second,
            sales = dataQuint.third,
            assets = dataQuint.fourth,
            recurring = dataQuint.fifth,
            selectedDate = selectedDate,
            currency = currency,
            filters = filters,
            isGoogleFit = integrations.first,
            isCloudBackup = integrations.second,
            selectedTheme = integrations.third,
            netWorthPasscode = integrations.fourth
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseUiState()
    )

    private fun calculateUiState(
        expenses: List<ExpenseEntity>,
        budgets: List<BudgetEntity>,
        sales: List<SaleEntity>,
        assets: List<PortfolioAssetEntity>,
        recurring: List<com.example.data.RecurringTransactionEntity>,
        selectedDate: Long,
        currency: String,
        filters: FilterParams,
        isGoogleFit: Boolean,
        isCloudBackup: Boolean,
        selectedTheme: String,
        netWorthPasscode: String
    ): ExpenseUiState {
        val startOfSelectedDay = DateUtils.getStartOfDay(selectedDate)
        val endOfSelectedDay = DateUtils.getEndOfDay(selectedDate)

        // Net Worth Calculations
        val totalAssets = assets.filter { !it.isLiability }.sumOf { it.balanceOrValue }
        val totalLiabilities = assets.filter { it.isLiability }.sumOf { it.balanceOrValue }
        val netWorth = totalAssets - totalLiabilities

        val bankBalance = assets.filter { it.category == "BANK" }.sumOf { it.balanceOrValue }
        val investments = assets.filter { it.category in listOf("MUTUAL_FUND", "STOCK_INDIAN", "STOCK_FOREIGN") }.sumOf { it.balanceOrValue }
        val retirement = assets.filter { it.category in listOf("EPFO", "ESI") }.sumOf { it.balanceOrValue }

        // Selected Day Expenses
        val selectedDayExpenses = expenses.filter {
            it.timestamp in startOfSelectedDay..endOfSelectedDay
        }
        val selectedDayExpenseTotal = selectedDayExpenses
            .filter { it.type == "EXPENSE" }
            .sumOf { it.amount }
        val selectedDayIncomeTotal = selectedDayExpenses
            .filter { it.type == "INCOME" }
            .sumOf { it.amount }

        // Selected Day Sales
        val selectedDaySales = sales.filter {
            it.date in startOfSelectedDay..endOfSelectedDay
        }
        val selectedDaySalesRevenue = selectedDaySales.sumOf { it.totalRevenue }
        val selectedDaySalesCost = selectedDaySales.sumOf { it.costPrice * it.quantity }
        val selectedDaySalesProfit = selectedDaySales.sumOf { it.totalProfit }
        val selectedDaySalesMargin = if (selectedDaySalesRevenue > 0) {
            (selectedDaySalesProfit / selectedDaySalesRevenue) * 100.0
        } else 0.0
        val selectedDayUnitsSold = selectedDaySales.sumOf { it.quantity }

        // Current Month Spending
        val now = System.currentTimeMillis()
        val startOfMonth = DateUtils.getStartOfMonth(now)
        val endOfMonth = DateUtils.getEndOfMonth(now)

        val monthExpenses = expenses.filter { it.timestamp in startOfMonth..endOfMonth }
        val monthExpenseTotal = monthExpenses
            .filter { it.type == "EXPENSE" }
            .sumOf { it.amount }
        val monthIncomeTotal = monthExpenses
            .filter { it.type == "INCOME" }
            .sumOf { it.amount }

        // Multi-Period Budgets & Spending Plan (Monthly, Quarterly, Half-Yearly, Yearly)
        val allPeriodStatuses = mutableListOf<BudgetStatus>()
        var selectedPeriodStatuses = listOf<BudgetStatus>()
        var selectedPeriodExpenseTotal = 0.0
        var selectedPeriodOverallLimit = 55000.0

        for (period in BudgetPeriod.entries) {
            val (pStart, pEnd) = period.getDateRange(now)
            val periodExpenses = expenses.filter { it.timestamp in pStart..pEnd && it.type == "EXPENSE" }
            val periodSpent = periodExpenses.sumOf { it.amount }

            val defaultOverall = when (period) {
                BudgetPeriod.MONTHLY -> 55000.0
                BudgetPeriod.QUARTERLY -> 165000.0
                BudgetPeriod.HALF_YEARLY -> 330000.0
                BudgetPeriod.YEARLY -> 660000.0
            }

            val overallEntity = budgets.find {
                it.category == "OVERALL" && (it.period == period.id || (period == BudgetPeriod.MONTHLY && it.period.isEmpty()))
            }
            val overallLimit = overallEntity?.limitAmount ?: defaultOverall

            val catStatuses = Categories.expenseCategories.map { category ->
                val entity = budgets.find {
                    it.category == category.name && (it.period == period.id || (period == BudgetPeriod.MONTHLY && it.period.isEmpty()))
                }
                val limit = entity?.limitAmount ?: 0.0
                val alertThreshold = entity?.alertThresholdPercent ?: 80.0
                val spent = periodExpenses
                    .filter { it.category == category.name }
                    .sumOf { it.amount }
                val pct = if (limit > 0) (spent / limit).toFloat() else if (spent > 0) 1f else 0f
                BudgetStatus(
                    category = category.name,
                    period = period,
                    limit = limit,
                    spent = spent,
                    percentage = pct,
                    remaining = (limit - spent).coerceAtLeast(0.0),
                    alertThresholdPercent = alertThreshold
                )
            }.toMutableList()

            if (overallLimit > 0) {
                val overallPct = if (overallLimit > 0) (periodSpent / overallLimit).toFloat() else if (periodSpent > 0) 1f else 0f
                catStatuses.add(
                    0,
                    BudgetStatus(
                        category = "Overall Budget",
                        period = period,
                        limit = overallLimit,
                        spent = periodSpent,
                        percentage = overallPct,
                        remaining = (overallLimit - periodSpent).coerceAtLeast(0.0),
                        alertThresholdPercent = overallEntity?.alertThresholdPercent ?: 80.0
                    )
                )
            }

            allPeriodStatuses.addAll(catStatuses)

            if (period == filters.selectedBudgetPeriod) {
                selectedPeriodStatuses = catStatuses
                selectedPeriodExpenseTotal = periodSpent
                selectedPeriodOverallLimit = overallLimit
            }
        }

        val exceededWarnings = allPeriodStatuses.filter { it.isExceeded }
        val nearWarnings = allPeriodStatuses.filter { it.isNearLimit }

        val periodDaysLeft = filters.selectedBudgetPeriod.getDaysLeft()
        val periodFormattedLabel = filters.selectedBudgetPeriod.getFormattedPeriodLabel(now)

        // Analytics range filtering
        val (analyticsStart, analyticsEnd) = when (filters.timeRange) {
            TimeRange.TODAY -> DateUtils.getStartOfDay(now) to DateUtils.getEndOfDay(now)
            TimeRange.WEEK -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -6)
                DateUtils.getStartOfDay(cal.timeInMillis) to DateUtils.getEndOfDay(now)
            }
            TimeRange.MONTH -> startOfMonth to endOfMonth
            TimeRange.YEAR -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_YEAR, 1)
                DateUtils.getStartOfDay(cal.timeInMillis) to DateUtils.getEndOfDay(now)
            }
            TimeRange.ALL -> 0L to Long.MAX_VALUE
        }

        val analyticsExpenses = expenses.filter { it.timestamp in analyticsStart..analyticsEnd }
        val analyticsExpenseTotal = analyticsExpenses
            .filter { it.type == "EXPENSE" }
            .sumOf { it.amount }
        val analyticsIncomeTotal = analyticsExpenses
            .filter { it.type == "INCOME" }
            .sumOf { it.amount }

        val categorySpends = analyticsExpenses
            .filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                val pct = if (analyticsExpenseTotal > 0) (sum / analyticsExpenseTotal).toFloat() else 0f
                CategorySpend(cat, sum, pct)
            }
            .sortedByDescending { it.amount }

        // Weekly expense trend
        val trendPoints = mutableListOf<DailySpendPoint>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = DateUtils.getStartOfDay(cal.timeInMillis)
            val dayEnd = DateUtils.getEndOfDay(cal.timeInMillis)
            val dayAmount = expenses
                .filter { it.type == "EXPENSE" && it.timestamp in dayStart..dayEnd }
                .sumOf { it.amount }
            val label = if (i == 0) "Today" else DateUtils.formatShortDay(dayStart)
            trendPoints.add(
                DailySpendPoint(
                    dayLabel = label,
                    dateMillis = dayStart,
                    amount = dayAmount,
                    isToday = i == 0
                )
            )
        }

        // Analytics Sales calculations
        val analyticsSales = sales.filter { it.date in analyticsStart..analyticsEnd }
        val analyticsSalesRevenue = analyticsSales.sumOf { it.totalRevenue }
        val analyticsSalesCost = analyticsSales.sumOf { it.costPrice * it.quantity }
        val analyticsSalesProfit = analyticsSales.sumOf { it.totalProfit }
        val analyticsSalesMargin = if (analyticsSalesRevenue > 0) {
            (analyticsSalesProfit / analyticsSalesRevenue) * 100.0
        } else 0.0

        val salesTrendPoints = mutableListOf<SalesTrendPoint>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = DateUtils.getStartOfDay(cal.timeInMillis)
            val dayEnd = DateUtils.getEndOfDay(cal.timeInMillis)
            val daySales = sales.filter { it.date in dayStart..dayEnd }
            val dayRevenue = daySales.sumOf { it.totalRevenue }
            val dayCost = daySales.sumOf { it.costPrice * it.quantity }
            val dayProfit = daySales.sumOf { it.totalProfit }
            val dayMargin = if (dayRevenue > 0) (dayProfit / dayRevenue) * 100.0 else 0.0
            val label = if (i == 0) "Today" else DateUtils.formatShortDay(dayStart)
            salesTrendPoints.add(
                SalesTrendPoint(
                    dayLabel = label,
                    dateMillis = dayStart,
                    revenue = dayRevenue,
                    profit = dayProfit,
                    cost = dayCost,
                    marginPercent = dayMargin
                )
            )
        }

        val topProducts = analyticsSales
            .groupBy { it.productName }
            .map { (prod, list) ->
                val qty = list.sumOf { it.quantity }
                val rev = list.sumOf { it.totalRevenue }
                val prof = list.sumOf { it.totalProfit }
                val margin = if (rev > 0) (prof / rev) * 100.0 else 0.0
                ProductPerformance(prod, qty, rev, prof, margin)
            }
            .sortedByDescending { it.totalRevenue }

        // History filtering
        val historyFilteredExpenses = expenses.filter { item ->
            val matchesQuery = filters.searchQuery.isBlank() ||
                    item.title.contains(filters.searchQuery, ignoreCase = true) ||
                    item.category.contains(filters.searchQuery, ignoreCase = true) ||
                    item.note.contains(filters.searchQuery, ignoreCase = true) ||
                    item.paymentMethod.contains(filters.searchQuery, ignoreCase = true)

            val matchesCategory = filters.selectedCategoryFilter == null ||
                    item.category == filters.selectedCategoryFilter

            val matchesType = when (filters.selectedTypeFilter) {
                "EXPENSE" -> item.type == "EXPENSE"
                "INCOME" -> item.type == "INCOME"
                else -> true
            }

            matchesQuery && matchesCategory && matchesType
        }

        return ExpenseUiState(
            allExpenses = expenses,
            allBudgets = budgets,
            allSales = sales,
            allPortfolioAssets = assets,
            selectedDate = selectedDate,
            currencySymbol = currency,
            selectedTimeRange = filters.timeRange,
            searchQuery = filters.searchQuery,
            selectedCategoryFilter = filters.selectedCategoryFilter,
            selectedTypeFilter = filters.selectedTypeFilter,
            totalNetWorth = netWorth,
            totalBankBalance = bankBalance,
            totalInvestments = investments,
            totalRetirementGovt = retirement,
            totalLiabilities = totalLiabilities,
            selectedDayExpenses = selectedDayExpenses,
            selectedDayExpenseTotal = selectedDayExpenseTotal,
            selectedDayIncomeTotal = selectedDayIncomeTotal,
            selectedDaySales = selectedDaySales,
            selectedDaySalesRevenue = selectedDaySalesRevenue,
            selectedDaySalesCost = selectedDaySalesCost,
            selectedDaySalesProfit = selectedDaySalesProfit,
            selectedDaySalesMargin = selectedDaySalesMargin,
            selectedDayUnitsSold = selectedDayUnitsSold,
            selectedBudgetPeriod = filters.selectedBudgetPeriod,
            periodExpenseTotal = selectedPeriodExpenseTotal,
            periodDaysLeft = periodDaysLeft,
            periodFormattedLabel = periodFormattedLabel,
            monthExpenseTotal = monthExpenseTotal,
            monthIncomeTotal = monthIncomeTotal,
            overallBudget = selectedPeriodOverallLimit,
            budgetStatuses = selectedPeriodStatuses,
            allPeriodBudgetStatuses = allPeriodStatuses,
            exceededBudgetWarnings = exceededWarnings,
            nearBudgetWarnings = nearWarnings,
            analyticsExpenses = analyticsExpenses,
            analyticsExpenseTotal = analyticsExpenseTotal,
            analyticsIncomeTotal = analyticsIncomeTotal,
            categorySpends = categorySpends,
            weeklyTrend = trendPoints,
            analyticsSales = analyticsSales,
            analyticsSalesRevenue = analyticsSalesRevenue,
            analyticsSalesCost = analyticsSalesCost,
            analyticsSalesProfit = analyticsSalesProfit,
            analyticsSalesMargin = analyticsSalesMargin,
            salesAndProfitTrend = salesTrendPoints,
            topSellingProducts = topProducts,
            historyFilteredExpenses = historyFilteredExpenses,
            isShakeToLogEnabled = filters.isShakeToLogEnabled,
            shakeSensitivity = filters.shakeSensitivity,
            shakeLaunchMode = filters.shakeLaunchMode,
            isGoogleFitEnabled = isGoogleFit,
            isCloudBackupEnabled = isCloudBackup,
            selectedTheme = selectedTheme,
            recurringTransactions = recurring,
            defaultRecurringCategory = prefs.getString("DEFAULT_RECURRING_CATEGORY", "Housing") ?: "Housing",
            defaultRecurringFrequency = prefs.getString("DEFAULT_RECURRING_FREQUENCY", "MONTHLY") ?: "MONTHLY",
            defaultRecurringPaymentMethod = prefs.getString("DEFAULT_RECURRING_PAYMENT_METHOD", "Bank Transfer") ?: "Bank Transfer",
            netWorthPasscode = netWorthPasscode
        )
    }

    fun setNetWorthPasscode(passcode: String) {
        _netWorthPasscode.value = passcode
        prefs.edit().putString("NET_WORTH_PASSCODE", passcode).apply()
    }

    fun setTheme(theme: String) {
        _selectedTheme.value = theme
        prefs.edit().putString("DEFAULT_THEME", theme).apply()
    }

    fun toggleShakeToLog(enabled: Boolean) {
        _isShakeToLogEnabled.value = enabled
        prefs.edit().putBoolean("SHAKE_ENABLED", enabled).apply()
    }

    fun setShakeSensitivity(sensitivity: String) {
        _shakeSensitivity.value = sensitivity
        prefs.edit().putString("SHAKE_SENSITIVITY", sensitivity).apply()
    }

    fun setShakeLaunchMode(mode: String) {
        _shakeLaunchMode.value = mode
        prefs.edit().putString("SHAKE_LAUNCH_MODE", mode).apply()
    }

    fun setSelectedDate(timestamp: Long) {
        _selectedDate.value = DateUtils.getStartOfDay(timestamp)
    }

    fun changeDay(offsetDays: Int) {
        _selectedDate.value = DateUtils.addDays(_selectedDate.value, offsetDays)
    }

    fun setCurrency(symbol: String) {
        _currencySymbol.value = symbol
        prefs.edit().putString("DEFAULT_CURRENCY", symbol).apply()
    }

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setTypeFilter(type: String?) {
        _selectedTypeFilter.value = type
    }

    fun toggleGoogleFit(enabled: Boolean) {
        _isGoogleFitEnabled.value = enabled
        prefs.edit().putBoolean("GOOGLE_FIT_ENABLED", enabled).apply()
    }

    fun toggleCloudBackup(enabled: Boolean) {
        _isCloudBackupEnabled.value = enabled
        prefs.edit().putBoolean("CLOUD_BACKUP_ENABLED", enabled).apply()
    }

    // Recurring Transactions Operations
    fun addRecurringTransaction(
        title: String,
        amount: Double,
        category: String,
        frequency: String,
        paymentMethod: String,
        notes: String = "",
        autoExecute: Boolean = true
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            when (frequency.uppercase()) {
                "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                else -> cal.add(Calendar.MONTH, 1)
            }
            val entity = com.example.data.RecurringTransactionEntity(
                title = title.trim(),
                amount = amount,
                category = category,
                type = "EXPENSE",
                frequency = frequency,
                startDate = now,
                nextDueDate = cal.timeInMillis,
                paymentMethod = paymentMethod,
                isActive = true,
                autoExecute = autoExecute,
                notes = notes.trim()
            )
            repository.insertRecurring(entity)
            saveDefaultRecurringSettings(category, frequency, paymentMethod)
        }
    }

    fun toggleRecurringActive(item: com.example.data.RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.updateRecurring(item.copy(isActive = !item.isActive))
        }
    }

    fun deleteRecurringTransaction(item: com.example.data.RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.deleteRecurring(item)
        }
    }

    fun executeRecurringNow(item: com.example.data.RecurringTransactionEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val transaction = TransactionEntity(
                amount = item.amount,
                category = item.category,
                date = now,
                description = "[Manual Trigger] ${item.title}",
                type = item.type,
                paymentMethod = item.paymentMethod,
                note = "Executed manually from Budget Plan"
            )
            repository.insertTransaction(transaction)

            val cal = Calendar.getInstance()
            cal.timeInMillis = item.nextDueDate
            when (item.frequency.uppercase()) {
                "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                else -> cal.add(Calendar.MONTH, 1)
            }
            repository.updateRecurring(
                item.copy(
                    nextDueDate = cal.timeInMillis,
                    lastExecutedDate = now
                )
            )
        }
    }

    fun saveDefaultRecurringSettings(category: String, frequency: String, paymentMethod: String) {
        prefs.edit()
            .putString("DEFAULT_RECURRING_CATEGORY", category)
            .putString("DEFAULT_RECURRING_FREQUENCY", frequency)
            .putString("DEFAULT_RECURRING_PAYMENT_METHOD", paymentMethod)
            .apply()
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        type: String = "EXPENSE",
        timestamp: Long = System.currentTimeMillis(),
        paymentMethod: String = "Cash",
        note: String = "",
        tag: String = ""
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    category = category,
                    date = timestamp,
                    description = title,
                    title = title,
                    type = type,
                    timestamp = timestamp,
                    paymentMethod = paymentMethod,
                    note = note,
                    tag = tag
                )
            )
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addSale(
        productName: String,
        quantity: Int,
        sellingPrice: Double,
        costPrice: Double = 0.0,
        date: Long = System.currentTimeMillis(),
        customerName: String = "",
        paymentMethod: String = "Cash",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertSale(
                SaleEntity(
                    productName = productName,
                    quantity = quantity,
                    sellingPrice = sellingPrice,
                    costPrice = costPrice,
                    date = date,
                    customerName = customerName,
                    paymentMethod = paymentMethod,
                    notes = notes
                )
            )
        }
    }

    fun updateSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.updateSale(sale)
        }
    }

    fun deleteSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.deleteSale(sale)
        }
    }

    fun setSelectedBudgetPeriod(period: BudgetPeriod) {
        _selectedBudgetPeriod.value = period
    }

    fun setBudget(
        category: String,
        limit: Double,
        period: BudgetPeriod = _selectedBudgetPeriod.value,
        alertThreshold: Double = 80.0
    ) {
        viewModelScope.launch {
            if (limit <= 0) {
                repository.deleteBudgetByCategoryAndPeriod(category, period.id)
            } else {
                val id = BudgetEntity.createId(category, period)
                repository.insertOrUpdateBudget(
                    BudgetEntity(
                        id = id,
                        category = category,
                        period = period.id,
                        limitAmount = limit,
                        alertThresholdPercent = alertThreshold
                    )
                )
            }
        }
    }

    fun deleteBudget(category: String, period: BudgetPeriod = _selectedBudgetPeriod.value) {
        viewModelScope.launch {
            repository.deleteBudgetByCategoryAndPeriod(category, period.id)
        }
    }

    // Portfolio Asset Operations
    fun addPortfolioAsset(
        name: String,
        category: String,
        balanceOrValue: Double,
        investedAmount: Double = balanceOrValue,
        institution: String = "",
        accountNumberMasked: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertPortfolioAsset(
                PortfolioAssetEntity(
                    name = name,
                    category = category,
                    balanceOrValue = balanceOrValue,
                    investedAmount = investedAmount,
                    institution = institution,
                    accountNumberMasked = accountNumberMasked,
                    notes = notes
                )
            )
        }
    }

    fun updatePortfolioAsset(asset: PortfolioAssetEntity) {
        viewModelScope.launch {
            repository.updatePortfolioAsset(asset)
        }
    }

    fun deletePortfolioAsset(asset: PortfolioAssetEntity) {
        viewModelScope.launch {
            repository.deletePortfolioAsset(asset)
        }
    }

    fun updateAnumatiPfEsi(res: AnumatiPfEsiResult) {
        viewModelScope.launch {
            val existingAssets = repository.allPortfolioAssets.first()
            val epfoAsset = existingAssets.find { it.category == "EPFO" }
            if (epfoAsset != null) {
                repository.updatePortfolioAsset(
                    epfoAsset.copy(
                        balanceOrValue = res.totalPfBalance,
                        institution = "EPFO (Anumati AA Sync)",
                        accountNumberMasked = "UAN ${res.uan}",
                        notes = "Verified via Anumati AA (Govt. of India scheme). Employee: ₹${res.pfEmployeeShare.toLong()}, Employer: ₹${res.pfEmployerShare.toLong()}, Interest: ₹${res.pfInterestAccrued.toLong()}"
                    )
                )
            } else {
                repository.insertPortfolioAsset(
                    PortfolioAssetEntity(
                        name = "EPFO Employees Provident Fund",
                        category = "EPFO",
                        balanceOrValue = res.totalPfBalance,
                        institution = "EPFO (Anumati AA Sync)",
                        accountNumberMasked = "UAN ${res.uan}",
                        notes = "Verified via Anumati AA (Govt. of India scheme)"
                    )
                )
            }

            val esiAsset = existingAssets.find { it.category == "ESI" }
            if (esiAsset != null) {
                repository.updatePortfolioAsset(
                    esiAsset.copy(
                        balanceOrValue = res.esiReserveBalance,
                        institution = "ESIC (Anumati AA Sync)",
                        accountNumberMasked = "IP ${res.esiNumber}",
                        notes = "Verified via Anumati AA (Govt. of India scheme)"
                    )
                )
            } else {
                repository.insertPortfolioAsset(
                    PortfolioAssetEntity(
                        name = "ESI Medical & Health Security Reserve",
                        category = "ESI",
                        balanceOrValue = res.esiReserveBalance,
                        institution = "ESIC (Anumati AA Sync)",
                        accountNumberMasked = "IP ${res.esiNumber}",
                        notes = "Verified via Anumati AA (Govt. of India scheme)"
                    )
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

private data class FilterParams(
    val timeRange: TimeRange,
    val searchQuery: String,
    val selectedCategoryFilter: String?,
    val selectedTypeFilter: String?,
    val isShakeToLogEnabled: Boolean,
    val shakeSensitivity: String,
    val shakeLaunchMode: String,
    val selectedBudgetPeriod: BudgetPeriod
)

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
