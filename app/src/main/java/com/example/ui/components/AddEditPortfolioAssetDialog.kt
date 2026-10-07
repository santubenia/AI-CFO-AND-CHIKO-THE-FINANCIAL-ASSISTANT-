package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PortfolioAssetEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditPortfolioAssetDialog(
    initialAsset: PortfolioAssetEntity? = null,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (PortfolioAssetEntity) -> Unit
) {
    val categories = listOf(
        "BANK" to "Bank A/C",
        "CREDIT_CARD" to "Credit Card",
        "MUTUAL_FUND" to "Mutual Fund",
        "STOCK_INDIAN" to "Indian Stocks",
        "STOCK_FOREIGN" to "Foreign Stocks",
        "EPFO" to "EPFO Fund",
        "ESI" to "ESI Reserve",
        "OTHER_ASSET" to "Gold / Other"
    )

    var name by remember { mutableStateOf(initialAsset?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(initialAsset?.category ?: "BANK") }
    var balanceText by remember {
        mutableStateOf(if (initialAsset != null && initialAsset.balanceOrValue > 0) initialAsset.balanceOrValue.toString() else "")
    }
    var investedText by remember {
        mutableStateOf(if (initialAsset != null && initialAsset.investedAmount > 0) initialAsset.investedAmount.toString() else "")
    }
    var institution by remember { mutableStateOf(initialAsset?.institution ?: "") }
    var accountMask by remember { mutableStateOf(initialAsset?.accountNumberMasked ?: "") }
    var notes by remember { mutableStateOf(initialAsset?.notes ?: "") }
    var error by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("add_portfolio_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (initialAsset == null) "Add Account / Asset" else "Edit Portfolio Asset",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Chips
                Text(
                    text = "Asset / Account Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { selectedCategory = key },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = false
                    },
                    label = { Text("Account / Asset Name *") },
                    placeholder = { Text("e.g. HDFC Salary, Nifty 50 ETF, Apple Shares") },
                    singleLine = true,
                    isError = error && name.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Balance / Value & Invested Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = {
                            balanceText = it
                            error = false
                        },
                        label = { Text("Current Value ($currencySymbol) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = error && balanceText.toDoubleOrNull() == null,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = investedText,
                        onValueChange = { investedText = it },
                        label = { Text("Invested ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Institution & Account Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = institution,
                        onValueChange = { institution = it },
                        label = { Text("Institution / Broker") },
                        placeholder = { Text("e.g. SBI, Zerodha, EPFO") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = accountMask,
                        onValueChange = { accountMask = it },
                        label = { Text("Account / Mask") },
                        placeholder = { Text("e.g. •••• 4821") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        val parsedVal = balanceText.toDoubleOrNull()
                        if (name.isBlank() || parsedVal == null || parsedVal < 0) {
                            error = true
                            return@Button
                        }
                        val parsedInv = investedText.toDoubleOrNull() ?: parsedVal
                        val isLiability = selectedCategory == "CREDIT_CARD"

                        val asset = (initialAsset ?: PortfolioAssetEntity(
                            name = name.trim(),
                            category = selectedCategory,
                            balanceOrValue = parsedVal,
                            investedAmount = parsedInv,
                            institution = institution.trim(),
                            accountNumberMasked = accountMask.trim(),
                            notes = notes.trim(),
                            isLiability = isLiability
                        )).copy(
                            name = name.trim(),
                            category = selectedCategory,
                            balanceOrValue = parsedVal,
                            investedAmount = parsedInv,
                            institution = institution.trim(),
                            accountNumberMasked = accountMask.trim(),
                            notes = notes.trim(),
                            isLiability = isLiability
                        )
                        onSave(asset)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialAsset == null) "Save to Portfolio" else "Update Asset",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
