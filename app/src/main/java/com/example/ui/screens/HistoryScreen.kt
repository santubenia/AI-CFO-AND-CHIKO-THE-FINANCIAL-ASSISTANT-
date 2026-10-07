package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.model.Categories
import com.example.ui.ExpenseUiState
import com.example.ui.components.ExpenseItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.DateUtils

@Composable
fun HistoryScreen(
    uiState: ExpenseUiState,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onTypeFilterChange: (String?) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }

    // Group filtered expenses by calendar date
    val groupedExpenses = remember(uiState.historyFilteredExpenses) {
        uiState.historyFilteredExpenses.groupBy {
            DateUtils.getStartOfDay(it.timestamp)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_history_input"),
                placeholder = { Text("Search by title, category, or note...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // 2. Filter row (Type & Categories)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Type Filter (All, Expense, Income)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL" to "All", "EXPENSE" to "Expenses", "INCOME" to "Income").forEach { (typeKey, label) ->
                            val isSelected = uiState.selectedTypeFilter == typeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTypeFilterChange(typeKey) },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.testTag("type_filter_$typeKey")
                            )
                        }
                    }

                    // Export Summary button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showExportDialog = true }
                            .testTag("export_summary_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Export",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Category Filter horizontal scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == null,
                            onClick = { onCategoryFilterChange(null) },
                            label = { Text("All Categories", fontSize = 12.sp) }
                        )
                    }

                    items(Categories.expenseCategories + Categories.incomeCategories) { cat ->
                        val isSelected = uiState.selectedCategoryFilter == cat.name
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onCategoryFilterChange(if (isSelected) null else cat.name)
                            },
                            label = { Text(cat.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cat.color.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }
        }

        // 3. Transactions count & stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.historyFilteredExpenses.size} Transactions Found",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4. Grouped Items by Date
        if (groupedExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search or filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            groupedExpenses.forEach { (dateMillis, dayList) ->
                val dayExpenseSum = dayList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val dayIncomeSum = dayList.filter { it.type == "INCOME" }.sumOf { it.amount }

                // Group Header
                item(key = "header_$dateMillis") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatDate(dateMillis),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (dayExpenseSum > 0) {
                                Text(
                                    text = "- ${DateUtils.formatCurrency(dayExpenseSum, uiState.currencySymbol)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ExpenseRed
                                )
                            }
                            if (dayIncomeSum > 0) {
                                Text(
                                    text = "+ ${DateUtils.formatCurrency(dayIncomeSum, uiState.currencySymbol)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IncomeGreen
                                )
                            }
                        }
                    }
                }

                // Items in this date group
                items(dayList, key = { it.id }) { item ->
                    ExpenseItemCard(
                        expense = item,
                        currencySymbol = uiState.currencySymbol,
                        onEdit = { onEditExpense(item) },
                        onDelete = { onDeleteExpense(item) }
                    )
                }
            }
        }
    }

    // Export Summary Dialog
    if (showExportDialog) {
        val totalSpent = uiState.historyFilteredExpenses.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val totalEarned = uiState.historyFilteredExpenses.filter { it.type == "INCOME" }.sumOf { it.amount }

        val exportText = buildString {
            appendLine("=== EXPENSE TRACKER REPORT ===")
            appendLine("Date: ${DateUtils.formatDateFull(System.currentTimeMillis())}")
            appendLine("Total Expenses: ${DateUtils.formatCurrency(totalSpent, uiState.currencySymbol)}")
            appendLine("Total Income: ${DateUtils.formatCurrency(totalEarned, uiState.currencySymbol)}")
            appendLine("Net Balance: ${DateUtils.formatCurrency(totalEarned - totalSpent, uiState.currencySymbol)}")
            appendLine("Transactions Count: ${uiState.historyFilteredExpenses.size}")
            appendLine("------------------------------")
            uiState.historyFilteredExpenses.forEach { exp ->
                val sign = if (exp.type == "EXPENSE") "-" else "+"
                val dateStr = DateUtils.formatDate(exp.timestamp)
                appendLine("$dateStr | ${exp.category} | ${exp.title} | $sign${DateUtils.formatCurrency(exp.amount, uiState.currencySymbol)} (${exp.paymentMethod})")
            }
            appendLine("==============================")
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Summary", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Report preview (${uiState.historyFilteredExpenses.size} transactions):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Text(
                            text = exportText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Expense Report", exportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
