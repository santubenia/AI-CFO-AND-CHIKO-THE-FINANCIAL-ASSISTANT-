package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ExpenseEntity
import com.example.data.SaleEntity
import com.example.model.Categories
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.DateUtils

enum class QuickShakeLogType(val label: String, val icon: String) {
    SALE("Sale Record", "🛍️"),
    EXPENSE("Expense", "💸"),
    INCOME("Income", "💰")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickShakeLogDialog(
    currencySymbol: String = "$",
    onDismiss: () -> Unit,
    onSaveSale: (SaleEntity, openFullApp: Boolean) -> Unit,
    onSaveExpense: (ExpenseEntity, openFullApp: Boolean) -> Unit,
    onOpenFullAppDirectly: (QuickShakeLogType) -> Unit
) {
    var selectedType by remember { mutableStateOf(QuickShakeLogType.SALE) }
    var openFullAppAfterSave by remember { mutableStateOf(false) }

    // Sale Fields
    var saleProduct by remember { mutableStateOf("") }
    var saleQty by remember { mutableIntStateOf(1) }
    var salePriceText by remember { mutableStateOf("") }
    var saleCostText by remember { mutableStateOf("") }
    var saleError by remember { mutableStateOf(false) }

    // Expense / Income Fields
    var transAmountText by remember { mutableStateOf("") }
    var transTitle by remember { mutableStateOf("") }
    var transCategory by remember { mutableStateOf("Food & Dining") }
    var transError by remember { mutableStateOf(false) }

    val sellingPrice = salePriceText.toDoubleOrNull() ?: 0.0
    val costPrice = saleCostText.toDoubleOrNull() ?: 0.0
    val saleRevenue = sellingPrice * saleQty
    val saleProfit = (sellingPrice - costPrice) * saleQty
    val saleMargin = if (saleRevenue > 0) (saleProfit / saleRevenue) * 100.0 else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("quick_shake_log_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with shake badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Vibration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Quick Shake Logger",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "SHAKE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Enter details below without leaving your screen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("quick_shake_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Log / Transaction Type
                Text(
                    text = "Select Log Type:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickShakeLogType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                when (type) {
                                    QuickShakeLogType.SALE -> MaterialTheme.colorScheme.primary
                                    QuickShakeLogType.EXPENSE -> ExpenseRed
                                    QuickShakeLogType.INCOME -> IncomeGreen
                                }
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedType = type
                                    if (type == QuickShakeLogType.INCOME && transCategory == "Food & Dining") {
                                        transCategory = "Salary"
                                    }
                                }
                                .testTag("shake_type_${type.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(type.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form based on selected log type
                when (selectedType) {
                    QuickShakeLogType.SALE -> {
                        // Product Name
                        OutlinedTextField(
                            value = saleProduct,
                            onValueChange = {
                                saleProduct = it
                                saleError = false
                            },
                            label = { Text("Product Sold *") },
                            placeholder = { Text("e.g. Coffee Beans, Earbuds") },
                            singleLine = true,
                            isError = saleError && saleProduct.isBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shake_sale_product_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quantity & Prices
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Qty Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { if (saleQty > 1) saleQty-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "x$saleQty",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = { saleQty++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                                }
                            }

                            // Price Input
                            OutlinedTextField(
                                value = salePriceText,
                                onValueChange = {
                                    salePriceText = it
                                    saleError = false
                                },
                                label = { Text("Sell Price ($currencySymbol) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("shake_sale_price_input")
                            )

                            // Cost Input
                            OutlinedTextField(
                                value = saleCostText,
                                onValueChange = { saleCostText = it },
                                label = { Text("Cost ($currencySymbol)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("shake_sale_cost_input")
                            )
                        }

                        if (saleRevenue > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = IncomeGreen.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Revenue: ${DateUtils.formatCurrency(saleRevenue, currencySymbol)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Profit: +${DateUtils.formatCurrency(saleProfit, currencySymbol)} (${String.format("%.1f", saleMargin)}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen
                                    )
                                }
                            }
                        }
                    }

                    QuickShakeLogType.EXPENSE, QuickShakeLogType.INCOME -> {
                        // Amount
                        OutlinedTextField(
                            value = transAmountText,
                            onValueChange = {
                                transAmountText = it
                                transError = false
                            },
                            label = { Text("Amount ($currencySymbol) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            isError = transError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shake_trans_amount_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description / Title
                        OutlinedTextField(
                            value = transTitle,
                            onValueChange = { transTitle = it },
                            label = { Text("Description / What for?") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shake_trans_title_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val categories = if (selectedType == QuickShakeLogType.EXPENSE) {
                                listOf("Food & Dining", "Transport", "Groceries", "Shopping", "Entertainment", "Bills & Utilities")
                            } else {
                                listOf("Salary", "Freelance / Side Gig", "Investments", "Gifts", "Other")
                            }
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = transCategory == cat,
                                    onClick = { transCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // "Open full app" Option Toggle
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Open full app on save",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (openFullAppAfterSave)
                                    "Will open app directly to ${if (selectedType == QuickShakeLogType.SALE) "Sales" else "Daily"} tab"
                                else
                                    "Quickly saves and keeps you where you are",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Switch(
                            checked = openFullAppAfterSave,
                            onCheckedChange = { openFullAppAfterSave = it },
                            modifier = Modifier.testTag("open_full_app_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Button(
                    onClick = {
                        when (selectedType) {
                            QuickShakeLogType.SALE -> {
                                val price = salePriceText.toDoubleOrNull()
                                if (saleProduct.isBlank() || price == null || price <= 0) {
                                    saleError = true
                                    return@Button
                                }
                                val cost = saleCostText.toDoubleOrNull() ?: 0.0
                                val sale = SaleEntity(
                                    productName = saleProduct.trim(),
                                    quantity = saleQty,
                                    sellingPrice = price,
                                    costPrice = cost
                                )
                                onSaveSale(sale, openFullAppAfterSave)
                            }
                            QuickShakeLogType.EXPENSE, QuickShakeLogType.INCOME -> {
                                val amount = transAmountText.toDoubleOrNull()
                                if (amount == null || amount <= 0) {
                                    transError = true
                                    return@Button
                                }
                                val title = transTitle.ifBlank { transCategory }
                                val expense = ExpenseEntity(
                                    amount = amount,
                                    title = title.trim(),
                                    category = transCategory,
                                    type = if (selectedType == QuickShakeLogType.EXPENSE) "EXPENSE" else "INCOME"
                                )
                                onSaveExpense(expense, openFullAppAfterSave)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("shake_save_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (selectedType) {
                            QuickShakeLogType.SALE -> MaterialTheme.colorScheme.primary
                            QuickShakeLogType.EXPENSE -> ExpenseRed
                            QuickShakeLogType.INCOME -> IncomeGreen
                        }
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (openFullAppAfterSave) "Save & Open Full App" else "Save Record",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Direct Open Full App Button
                OutlinedButton(
                    onClick = { onOpenFullAppDirectly(selectedType) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("shake_open_full_app_button")
                ) {
                    Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open Full App Without Saving",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
