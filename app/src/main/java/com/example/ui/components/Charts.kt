package com.example.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Categories
import com.example.ui.CategorySpend
import com.example.ui.DailySpendPoint
import com.example.ui.SalesTrendPoint
import com.example.util.DateUtils
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    categorySpends: List<CategorySpend>,
    totalExpense: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (categorySpends.isEmpty() || totalExpense <= 0.0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expense data for this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedCategory by remember { mutableStateOf<CategorySpend?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(categorySpends) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(700))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val strokeWidth = 32.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                var startAngle = -90f
                val totalAnim = animationProgress.value

                categorySpends.forEach { item ->
                    val sweepAngle = item.percentage * 360f * totalAnim
                    val color = Categories.getCategoryItem(item.categoryName).color
                    val isSelected = selectedCategory?.categoryName == item.categoryName

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = (sweepAngle - 2f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = if (isSelected) strokeWidth * 1.25f else strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                    startAngle += sweepAngle
                }
            }

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (selectedCategory != null) {
                    Text(
                        text = selectedCategory!!.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = DateUtils.formatCurrency(selectedCategory!!.amount, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(selectedCategory!!.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Categories.getCategoryItem(selectedCategory!!.categoryName).color
                    )
                } else {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatCurrency(totalExpense, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${categorySpends.size} Categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categorySpends.take(6).forEach { item ->
                val cat = Categories.getCategoryItem(item.categoryName)
                val isSelected = selectedCategory?.categoryName == item.categoryName
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) cat.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            selectedCategory = if (isSelected) null else item
                        }
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(cat.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${cat.name} (${(item.percentage * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyBarChart(
    trendPoints: List<DailySpendPoint>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (trendPoints.isEmpty()) return

    val maxAmount = max(trendPoints.maxOfOrNull { it.amount } ?: 1.0, 10.0)
    val barAnim = remember { Animatable(0f) }

    LaunchedEffect(trendPoints) {
        barAnim.snapTo(0f)
        barAnim.animateTo(1f, animationSpec = tween(600))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            trendPoints.forEach { point ->
                val barFraction = ((point.amount / maxAmount).toFloat() * barAnim.value).coerceIn(0.04f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (point.amount > 0) {
                        Text(
                            text = point.amount.toInt().toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (point.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            fontWeight = if (point.isToday) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height((90 * barFraction).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (point.isToday) {
                                    MaterialTheme.colorScheme.primary
                                } else if (point.amount > 0) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = point.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = if (point.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (point.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Dual bar chart displaying Sales (Revenue) and Gross Profit in proportion in the same chart.
 * Allows comparing revenue volume directly against profit generated per day.
 */
@Composable
fun SalesVsProfitChart(
    trendPoints: List<SalesTrendPoint>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (trendPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No sales records logged yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
        return
    }

    val maxVal = max(trendPoints.maxOfOrNull { max(it.revenue, it.profit) } ?: 1.0, 10.0)
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(trendPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(650))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF0D9488)) // Teal for Sales
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sales", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF10B981)) // Green for Profit
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Profit", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dual proportional bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            trendPoints.forEach { point ->
                val revenueFraction = ((point.revenue / maxVal).toFloat() * animProgress.value).coerceIn(0.04f, 1f)
                val profitFraction = ((point.profit.coerceAtLeast(0.0) / maxVal).toFloat() * animProgress.value).coerceIn(0.02f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Margin label above bars if revenue > 0
                    if (point.revenue > 0) {
                        Text(
                            text = "${point.marginPercent.toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Spacer(modifier = Modifier.height(13.dp))
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Grouped bars side-by-side in proportion
                    Row(
                        modifier = Modifier.height(105.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Sales Bar (Revenue)
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height((100 * revenueFraction).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (point.revenue > 0) Color(0xFF0D9488) else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )

                        Spacer(modifier = Modifier.width(2.dp))

                        // Profit Bar (directly in proportion to revenue)
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height((100 * profitFraction).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (point.profit > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = point.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = if (point.dayLabel == "Today") FontWeight.Bold else FontWeight.Normal,
                        color = if (point.dayLabel == "Today") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

