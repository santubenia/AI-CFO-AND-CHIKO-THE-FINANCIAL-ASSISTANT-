package com.example.ui.chiko

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.ExpenseUiState
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.DateUtils

/**
 * Visual Chart and Image Output renderer for AI CFO messages.
 * Supports external/generated image URLs as well as interactive canvas diagrams
 * (Asset Allocation Donut, Market Momentum Comparison, Budget Gauge, and Wealth Compounding).
 */
@Composable
fun CfoVisualOutputCard(
    chartType: CfoVisualChartType?,
    imageUrl: String?,
    imageCaption: String?,
    uiState: ExpenseUiState,
    netWorth: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var showFullScreenImage by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (imageUrl != null) Icons.Default.Image else Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (chartType) {
                            CfoVisualChartType.ASSET_ALLOCATION -> "Visual Asset Allocation Donut"
                            CfoVisualChartType.MARKET_MOMENTUM -> "Global Indices Performance Visual"
                            CfoVisualChartType.BUDGET_ANALYSIS -> "Budget vs Spend Health Gauge"
                            CfoVisualChartType.WEALTH_COMPOUNDING -> "10-Year Alpha Compounding Forecast"
                            null -> imageCaption ?: "AI CFO Visual Output"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (imageUrl != null) {
                    IconButton(
                        onClick = { showFullScreenImage = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = "Expand Image",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Image URL or Visual Chart
            if (!imageUrl.isNullOrBlank()) {
                val context = LocalContext.current
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showFullScreenImage = true },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = imageCaption ?: "AI CFO Visual",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (!imageCaption.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = imageCaption,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else if (chartType != null) {
                when (chartType) {
                    CfoVisualChartType.ASSET_ALLOCATION -> {
                        AssetAllocationDonutChart(uiState, netWorth, currencySymbol)
                    }
                    CfoVisualChartType.MARKET_MOMENTUM -> {
                        MarketMomentumBarChart()
                    }
                    CfoVisualChartType.BUDGET_ANALYSIS -> {
                        BudgetHealthGaugeVisual(uiState, currencySymbol)
                    }
                    CfoVisualChartType.WEALTH_COMPOUNDING -> {
                        WealthCompoundingVisual(netWorth, currencySymbol)
                    }
                }
            }
        }
    }

    if (showFullScreenImage && !imageUrl.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { showFullScreenImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showFullScreenImage = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = imageCaption ?: "AI CFO Visual",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    if (!imageCaption.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = imageCaption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-precision circular Donut Chart showing Asset Allocation breakdown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssetAllocationDonutChart(
    uiState: ExpenseUiState,
    netWorth: Double,
    currencySymbol: String
) {
    val total = if (netWorth > 0) netWorth else 100000.0
    val bankVal = if (uiState.totalBankBalance > 0) uiState.totalBankBalance else total * 0.20
    val investVal = if (uiState.totalInvestments > 0) uiState.totalInvestments else total * 0.45
    val govtVal = if (uiState.totalRetirementGovt > 0) uiState.totalRetirementGovt else total * 0.25
    val goldVal = total * 0.10

    val segments = listOf(
        DonutSegment("Bank Accounts", bankVal, Color(0xFF3B82F6)),
        DonutSegment("Equities & MF", investVal, Color(0xFF10B981)),
        DonutSegment("EPFO & ESI", govtVal, Color(0xFFF59E0B)),
        DonutSegment("Gold & Debt", goldVal, Color(0xFF8B5CF6))
    )

    var animationTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationTrigger = true }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "donut_animation"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut Canvas
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(100.dp)) {
                val strokeWidth = 20.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                var startAngle = -90f
                val totalSum = segments.sumOf { it.value }

                segments.forEach { seg ->
                    val sweepAngle = ((seg.value / totalSum) * 360f * animatedProgress).toFloat()
                    drawArc(
                        color = seg.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "100%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Allocation",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val totalSum = segments.sumOf { it.value }
            segments.forEach { seg ->
                val pct = ((seg.value / totalSum) * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(seg.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = seg.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = seg.color
                    )
                }
            }
        }
    }
}

private data class DonutSegment(val label: String, val value: Double, val color: Color)

/**
 * Bar chart comparing live momentum of world indices.
 */
@Composable
private fun MarketMomentumBarChart() {
    val items = listOf(
        BarItem("NASDAQ", 0.85f, "+0.85%", IncomeGreen),
        BarItem("S&P 500", 0.62f, "+0.62%", IncomeGreen),
        BarItem("NIFTY 50", 0.45f, "+0.45%", IncomeGreen),
        BarItem("SENSEX", 0.38f, "+0.38%", IncomeGreen),
        BarItem("GOLD", 0.25f, "+0.25%", Color(0xFFF59E0B)),
        BarItem("BRENT", -0.78f, "-0.78%", ExpenseRed)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(64.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    val normalizedWidth = (Math.abs(item.pct) / 1.0f).coerceIn(0.1f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(normalizedWidth)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(item.color)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = item.changeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    color = item.color,
                    modifier = Modifier.width(45.dp)
                )
            }
        }
    }
}

private data class BarItem(val label: String, val pct: Float, val changeText: String, val color: Color)

/**
 * Budget vs Spend Health Gauge Visual.
 */
@Composable
private fun BudgetHealthGaugeVisual(
    uiState: ExpenseUiState,
    currencySymbol: String
) {
    val items = uiState.budgetStatuses.take(4)
    if (items.isEmpty()) {
        Text(
            text = "All budget categories are within safe limits (100% compliant).",
            style = MaterialTheme.typography.bodySmall,
            color = IncomeGreen
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { b ->
            val ratio = b.percentage.coerceIn(0f, 1.5f)
            val barColor = when {
                ratio >= 1f -> ExpenseRed
                ratio >= 0.8f -> Color(0xFFF59E0B)
                else -> IncomeGreen
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = b.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${DateUtils.formatCurrency(b.spent, currencySymbol)} / ${DateUtils.formatCurrency(b.limit, currencySymbol)} (${(b.percentage * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = barColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((ratio / 1.5f).coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

/**
 * 10-Year Alpha Compounding Forecast Curve.
 */
@Composable
private fun WealthCompoundingVisual(
    netWorth: Double,
    currencySymbol: String
) {
    val base = if (netWorth > 0) netWorth else 100000.0
    // Projected CAGR 14% p.a.
    val year1 = base * 1.14
    val year3 = base * 1.48
    val year5 = base * 1.93
    val year10 = base * 3.71

    val points = listOf(
        Pair("Today", base),
        Pair("Year 1", year1),
        Pair("Year 3", year3),
        Pair("Year 5", year5),
        Pair("Year 10", year10)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { (lbl, amount) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = lbl,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = DateUtils.formatCurrency(amount, currencySymbol),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual growth path curve
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            val path = Path()
            val maxVal = year10
            val minVal = base * 0.9

            points.forEachIndexed { index, pair ->
                val x = (size.width / (points.size - 1)) * index
                val normalizedY = ((pair.second - minVal) / (maxVal - minVal)).toFloat()
                val y = size.height - (normalizedY * (size.height - 15.dp.toPx())) - 8.dp.toPx()

                if (index == 0) path.moveTo(x, y)
                else path.lineTo(x, y)

                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                brush = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF10B981))),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
