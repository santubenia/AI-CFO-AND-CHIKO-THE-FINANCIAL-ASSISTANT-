package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BudgetPeriod
import com.example.model.Categories
import com.example.model.DefaultWealthTargets
import com.example.model.WealthTarget
import com.example.ui.BudgetStatus
import com.example.ui.ExpenseUiState
import com.example.ui.components.AddEditWealthTargetDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.DateUtils

@Composable
fun BudgetsScreen(
    uiState: ExpenseUiState,
    onPeriodChange: (BudgetPeriod) -> Unit = {},
    onEditBudget: (category: String, currentLimit: Double, period: BudgetPeriod, threshold: Double) -> Unit,
    onResetSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    onOpenChiko: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Budgets & Warnings, 1: Wealth Plan & Targets, 2: AI Recommendations
    var editingTarget by remember { mutableStateOf<WealthTarget?>(null) }
    val wealthTargets = remember { mutableStateListOf<WealthTarget>().apply { addAll(DefaultWealthTargets.getDefaultTargets()) } }

    var animTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animTrigger = true }

    // Coin fill animation
    val coinFillAnim by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "CoinFillAnim"
    )

    val overallBudgetStatus = uiState.budgetStatuses.find { it.category == "Overall Budget" || it.category == "OVERALL" }
    val overallLimit = overallBudgetStatus?.limit ?: uiState.overallBudget
    val overallSpent = uiState.periodExpenseTotal
    val overallRemaining = (overallLimit - overallSpent).coerceAtLeast(0.0)
    val overallPct = if (overallLimit > 0) (overallSpent / overallLimit).toFloat() else 0f
    val isOverBudget = overallSpent > overallLimit
    val isNearBudget = overallBudgetStatus?.isNearLimit ?: (overallPct >= 0.8f && !isOverBudget)

    val daysLeft = uiState.periodDaysLeft
    val dailyAllowance = if (daysLeft > 0) overallRemaining / daysLeft else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("budgets_screen")
    ) {
        // Sweep / Sub-tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Category Budgets", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Wealth Plan & Targets", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("AI Recommendations", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        when (selectedTab) {
            0 -> {
                // Category Budgets & Active Warning Alerts
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Multi-Period Horizon Switcher Bar
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Plan Horizon / Period",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "${uiState.periodDaysLeft} days left in ${uiState.selectedBudgetPeriod.shortLabel}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BudgetPeriod.entries.forEach { period ->
                                        FilterChip(
                                            selected = uiState.selectedBudgetPeriod == period,
                                            onClick = { onPeriodChange(period) },
                                            label = { Text(period.title, fontWeight = FontWeight.SemiBold) },
                                            modifier = Modifier.testTag("tab_period_${period.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Active Warnings Banner when limit is approaching / near threshold (e.g. >= 80%)
                    if (uiState.nearBudgetWarnings.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("budget_near_warning_card")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "⚠️ Nearing Spending Limits (${uiState.nearBudgetWarnings.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Spending has reached or exceeded 80% of defined limits. Pace yourself to prevent overages:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    uiState.nearBudgetWarnings.forEach { ws ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clickable {
                                                    onEditBudget(ws.category, ws.limit, ws.period, ws.alertThresholdPercent)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "• ${ws.category}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${ws.period.title} Plan • ${DateUtils.formatCurrency(ws.remaining, uiState.currencySymbol)} left",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "${(ws.percentage * 100).toInt()}% Used",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFD97706),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Active Warnings Banner when limit is exceeded
                    if (uiState.exceededBudgetWarnings.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("budget_exceeded_warning_card")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "🚨 Limit Exceeded Warning! (${uiState.exceededBudgetWarnings.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "The following spending limits have been exceeded:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    uiState.exceededBudgetWarnings.forEach { ws ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("• ${ws.category} (${ws.period.title})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = "Over by +${DateUtils.formatCurrency(ws.spent - ws.limit, uiState.currencySymbol)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = ExpenseRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 1. Overall Period Budget Card with Coin Filling Animation
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().testTag("overall_budget_card")
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .scale(coinFillAnim)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEAB308).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.MonetizationOn,
                                                contentDescription = null,
                                                tint = Color(0xFFCA8A04),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("${uiState.selectedBudgetPeriod.title} Budget Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(uiState.periodFormattedLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                                            onEditBudget("OVERALL", overallLimit, uiState.selectedBudgetPeriod, overallBudgetStatus?.alertThresholdPercent ?: 80.0)
                                        }
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Plan", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text("Spent so far in ${uiState.selectedBudgetPeriod.shortLabel.lowercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = DateUtils.formatCurrency(overallSpent, uiState.currencySymbol),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isOverBudget) ExpenseRed else if (isNearBudget) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${uiState.selectedBudgetPeriod.title} Limit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = DateUtils.formatCurrency(overallLimit, uiState.currencySymbol),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                LinearProgressIndicator(
                                    progress = { (overallPct * coinFillAnim).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                    color = if (isOverBudget) ExpenseRed else if (overallPct >= (overallBudgetStatus?.alertThresholdPercent ?: 80.0) / 100.0) Color(0xFFF59E0B) else IncomeGreen,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isOverBudget)
                                            "Exceeded by ${DateUtils.formatCurrency(overallSpent - overallLimit, uiState.currencySymbol)}"
                                        else if (isNearBudget)
                                            "Nearing limit: ${DateUtils.formatCurrency(overallRemaining, uiState.currencySymbol)} left"
                                        else
                                            "${DateUtils.formatCurrency(overallRemaining, uiState.currencySymbol)} remaining",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isOverBudget) ExpenseRed else if (isNearBudget) Color(0xFFD97706) else IncomeGreen
                                    )

                                    Text(
                                        text = "${DateUtils.formatCurrency(dailyAllowance, uiState.currencySymbol)}/day safe pace ($daysLeft days)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category Spending Limits (${uiState.selectedBudgetPeriod.title})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Button(
                                onClick = {
                                    onEditBudget("", 0.0, uiState.selectedBudgetPeriod, 80.0)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Set Limit", fontSize = 12.sp)
                            }
                        }
                    }

                    items(uiState.budgetStatuses.filter { it.category != "Overall Budget" && it.category != "OVERALL" }) { status ->
                        CategoryBudgetCard(
                            status = status,
                            currencySymbol = uiState.currencySymbol,
                            onEdit = { onEditBudget(status.category, status.limit, status.period, status.alertThresholdPercent) }
                        )
                    }
                }
            }

            1 -> {
                // Wealth Plan & Targets Tab (Customizable milestones for Bank, MF, Stocks, EPFO, etc.)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Wealth Plan & Profile Targets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Customize and track targets for each profile asset class. Monitor exact progress and required monthly contributions.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(wealthTargets) { target ->
                        val currentVal = when (target.category) {
                            "BANK" -> uiState.totalBankBalance
                            "MUTUAL_FUND" -> uiState.allPortfolioAssets.filter { it.category == "MUTUAL_FUND" }.sumOf { it.balanceOrValue }
                            "STOCK_INDIAN" -> uiState.allPortfolioAssets.filter { it.category == "STOCK_INDIAN" }.sumOf { it.balanceOrValue }
                            "STOCK_FOREIGN" -> uiState.allPortfolioAssets.filter { it.category == "STOCK_FOREIGN" }.sumOf { it.balanceOrValue }
                            "EPFO" -> uiState.totalRetirementGovt
                            else -> uiState.totalNetWorth
                        }
                        val progress = if (target.targetAmount > 0) (currentVal / target.targetAmount).toFloat().coerceIn(0f, 1f) else 0f

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (target.category) {
                                                        "BANK" -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                                        "MUTUAL_FUND", "STOCK_INDIAN" -> IncomeGreen.copy(alpha = 0.15f)
                                                        "STOCK_FOREIGN" -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                                        "EPFO" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                when (target.category) {
                                                    "BANK" -> Icons.Default.AccountBalance
                                                    "MUTUAL_FUND", "STOCK_INDIAN" -> Icons.Default.ShowChart
                                                    "STOCK_FOREIGN" -> Icons.Default.TrendingUp
                                                    "EPFO" -> Icons.Default.Security
                                                    else -> Icons.Default.Flag
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(target.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text("Target by ${target.targetDateMonthYear}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }

                                    IconButton(
                                        onClick = { editingTarget = target },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Target", modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Current: ${DateUtils.formatCurrency(currentVal, uiState.currencySymbol)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Goal: ${DateUtils.formatCurrency(target.targetAmount, uiState.currencySymbol)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { progress * coinFillAnim },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = if (progress >= 1f) IncomeGreen else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${(progress * 100).toInt()}% Achieved",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (progress >= 1f) IncomeGreen else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Req: ${DateUtils.formatCurrency(target.monthlyContributionNeeded, uiState.currencySymbol)}/mo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // AI Recommendations Sub-Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Chiko's AI Budget Rebalancing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Based on your spending velocity, discretionary dining accounts for your highest budget variance. Here are AI-recommended monthly adjustments:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        BudgetAiRecommendationCard(
                            title = "Trim Discretionary Dining Cap",
                            suggestion = "Reduce Food & Dining monthly threshold by 15% and redirect ₹2,500/mo into your Nifty 50 Index SIP.",
                            impact = "+₹30,000 Annual Savings"
                        )
                    }

                    item {
                        BudgetAiRecommendationCard(
                            title = "Buffer Utility Rate Fluctuations",
                            suggestion = "Increase Bills & Utilities limit by ₹1,000 to avoid artificial alerts during seasonal electricity tariff spikes.",
                            impact = "Zero false budget alarms"
                        )
                    }

                    item {
                        BudgetAiRecommendationCard(
                            title = "Automate Surplus Sweeps to High-Yield Debt",
                            suggestion = "At month-end, any remaining unspent allowance automatically rolls into liquid emergency reserves.",
                            impact = "Compounding safety net"
                        )
                    }
                }
            }
        }
    }

    editingTarget?.let { target ->
        AddEditWealthTargetDialog(
            initialTarget = target,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { editingTarget = null },
            onSave = { updated ->
                val index = wealthTargets.indexOfFirst { it.category == updated.category }
                if (index != -1) {
                    wealthTargets[index] = updated
                }
                editingTarget = null
            }
        )
    }
}

// Backward-compatible overload for legacy callers
@Composable
fun BudgetsScreen(
    uiState: ExpenseUiState,
    onEditBudget: (category: String, currentLimit: Double) -> Unit,
    onResetSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    onOpenChiko: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BudgetsScreen(
        uiState = uiState,
        onPeriodChange = {},
        onEditBudget = { cat, limit, _, _ -> onEditBudget(cat, limit) },
        onResetSampleData = onResetSampleData,
        onClearAllData = onClearAllData,
        onOpenChiko = onOpenChiko,
        modifier = modifier
    )
}

@Composable
fun CategoryBudgetCard(
    status: BudgetStatus,
    currencySymbol: String,
    onEdit: () -> Unit
) {
    val cat = Categories.getCategoryItem(status.category)
    val isOver = status.isExceeded
    val isNear = status.isNearLimit

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOver)
                ExpenseRed.copy(alpha = 0.08f)
            else if (isNear)
                Color(0xFFF59E0B).copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(cat.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = status.period.shortLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (status.limit > 0)
                                "Limit: ${DateUtils.formatCurrency(status.limit, currencySymbol)}"
                            else
                                "No limit set",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOver) {
                        Surface(shape = RoundedCornerShape(6.dp), color = ExpenseRed.copy(alpha = 0.2f)) {
                            Text(
                                text = "EXCEEDED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (isNear) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF59E0B).copy(alpha = 0.2f)) {
                            Text(
                                text = "NEARING LIMIT",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Limit", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { status.percentage.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (isOver) ExpenseRed else if (isNear) Color(0xFFF59E0B) else cat.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${DateUtils.formatCurrency(status.spent, currencySymbol)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOver) ExpenseRed else if (isNear) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (status.limit > 0)
                        "${(status.percentage * 100).toInt()}% used • Alert @ ${status.alertThresholdPercent.toInt()}%"
                    else
                        "No limit set",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isOver) ExpenseRed else if (isNear) Color(0xFFD97706) else cat.color
                )
            }
        }
    }
}

@Composable
fun BudgetAiRecommendationCard(
    title: String,
    suggestion: String,
    impact: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(6.dp), color = IncomeGreen.copy(alpha = 0.15f)) {
                    Text(impact, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = IncomeGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(suggestion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
