package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BudgetPeriod
import com.example.model.Categories
import com.example.model.Currencies
import com.example.ui.theme.ExpenseRed
import com.example.util.DateUtils

@Composable
fun DeleteConfirmationDialog(
    itemTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Transaction?", fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to delete '$itemTitle'? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BudgetEditDialog(
    categoryName: String,
    currentLimit: Double,
    currencySymbol: String,
    initialPeriod: BudgetPeriod = BudgetPeriod.MONTHLY,
    initialThreshold: Double = 80.0,
    availableCategories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double, period: BudgetPeriod, threshold: Double) -> Unit,
    onDelete: ((category: String, period: BudgetPeriod) -> Unit)? = null
) {
    var selectedCategory by remember { mutableStateOf(categoryName) }
    var selectedPeriod by remember { mutableStateOf(initialPeriod) }
    var limitText by remember {
        mutableStateOf(if (currentLimit > 0) currentLimit.toInt().toString() else "")
    }
    var alertThreshold by remember { mutableDoubleStateOf(initialThreshold) }
    var isError by remember { mutableStateOf(false) }

    val isOverall = selectedCategory.equals("OVERALL", ignoreCase = true) || selectedCategory.equals("Overall Budget", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isOverall) "Set ${selectedPeriod.title} Budget Plan" else "Set ${selectedPeriod.title} Budget: $selectedCategory",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // If adding a new budget without fixed category or when availableCategories is supplied
                if (categoryName.isBlank() && availableCategories.isNotEmpty()) {
                    Text(
                        text = "Select Category:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isOverall,
                            onClick = { selectedCategory = "OVERALL" },
                            label = { Text("Overall Budget") }
                        )
                        availableCategories.filter { it != "OVERALL" && it != "Overall Budget" }.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 1. Budget Period Selector Chips
                Text(
                    text = "Budget / Plan Period:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BudgetPeriod.entries.forEach { p ->
                        FilterChip(
                            selected = selectedPeriod == p,
                            onClick = { selectedPeriod = p },
                            label = { Text(p.title) },
                            modifier = Modifier.testTag("budget_period_chip_${p.name.lowercase()}")
                        )
                    }
                }

                Text(
                    text = "${selectedPeriod.title}: ${selectedPeriod.description} (${selectedPeriod.getFormattedPeriodLabel()})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // 2. Spending Limit Input
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            limitText = input
                            isError = false
                        }
                    },
                    label = { Text("${selectedPeriod.title} Limit") },
                    prefix = { Text("$currencySymbol ") },
                    isError = isError,
                    supportingText = {
                        if (isError) Text("Please enter a valid amount")
                        else Text("Max allowed spending for the entire ${selectedPeriod.shortLabel.lowercase()}")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input")
                )

                // Quick Amount suggestions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = when (selectedPeriod) {
                        BudgetPeriod.MONTHLY -> listOf(5000, 10000, 25000, 50000)
                        BudgetPeriod.QUARTERLY -> listOf(15000, 30000, 75000, 150000)
                        BudgetPeriod.HALF_YEARLY -> listOf(30000, 60000, 150000, 300000)
                        BudgetPeriod.YEARLY -> listOf(60000, 120000, 300000, 600000)
                    }
                    presets.forEach { amount ->
                        AssistChip(
                            onClick = { limitText = amount.toString() },
                            label = { Text("$currencySymbol$amount", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Proactive Alert Threshold
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Alert When Nearing Limit at:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(70.0 to "70%", 80.0 to "80% (Rec.)", 90.0 to "90%", 95.0 to "95%").forEach { (th, label) ->
                        FilterChip(
                            selected = alertThreshold == th,
                            onClick = { alertThreshold = th },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val currentLimitParsed = limitText.toDoubleOrNull() ?: 0.0
                val alertAmount = currentLimitParsed * (alertThreshold / 100.0)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = if (currentLimitParsed > 0)
                            "⚠️ You will receive an alert as soon as you reach ${alertThreshold.toInt()}% (${DateUtils.formatCurrency(alertAmount, currencySymbol)}) of your $currencySymbol${currentLimitParsed.toInt()} ${selectedPeriod.shortLabel.lowercase()} limit."
                        else
                            "⚠️ You will receive a notification when spending reaches ${alertThreshold.toInt()}% of this period's limit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Delete option if limit exists
                if (currentLimit > 0 && onDelete != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            val catToDel = if (isOverall) "OVERALL" else selectedCategory
                            onDelete(catToDel, selectedPeriod)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                        modifier = Modifier.fillMaxWidth().testTag("delete_budget_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove This ${selectedPeriod.title} Limit")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = limitText.toDoubleOrNull()
                    if (amount != null && amount >= 0) {
                        val catToSave = if (isOverall) "OVERALL" else selectedCategory
                        onSave(catToSave, amount, selectedPeriod, alertThreshold)
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text("Save Plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Backward-compatible overload for legacy callers
@Composable
fun BudgetEditDialog(
    categoryName: String,
    currentLimit: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    BudgetEditDialog(
        categoryName = categoryName,
        currentLimit = currentLimit,
        currencySymbol = currencySymbol,
        initialPeriod = BudgetPeriod.MONTHLY,
        initialThreshold = 80.0,
        onDismiss = onDismiss,
        onSave = { _, limit, _, _ -> onSave(limit) }
    )
}

@Composable
fun CurrencySelectorDialog(
    currentSymbol: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Currencies.options.forEach { option ->
                    val isSelected = currentSymbol == option.symbol
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option.symbol) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelect(option.symbol) }
                            )
                            Text(
                                text = "${option.name} (${option.symbol})",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
