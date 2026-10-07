package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.ui.graphics.vector.ImageVector

data class PaymentMethodItem(
    val id: String,
    val name: String,
    val icon: ImageVector
)

object PaymentMethods {
    val all = listOf(
        PaymentMethodItem("Cash", "Cash", Icons.Default.LocalAtm),
        PaymentMethodItem("Credit Card", "Credit Card", Icons.Default.CreditCard),
        PaymentMethodItem("Debit Card", "Debit Card", Icons.Default.Payments),
        PaymentMethodItem("UPI / Online", "UPI / Online", Icons.Default.QrCode),
        PaymentMethodItem("Bank Transfer", "Bank Transfer", Icons.Default.AccountBalance)
    )

    fun getIcon(name: String): ImageVector {
        return all.find { it.name.equals(name, ignoreCase = true) }?.icon ?: Icons.Default.LocalAtm
    }
}

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
)

object Currencies {
    val options = listOf(
        CurrencyOption("USD", "$", "US Dollar ($)"),
        CurrencyOption("EUR", "€", "Euro (€)"),
        CurrencyOption("GBP", "£", "British Pound (£)"),
        CurrencyOption("INR", "₹", "Indian Rupee (₹)"),
        CurrencyOption("JPY", "¥", "Japanese Yen (¥)"),
        CurrencyOption("CAD", "CA$", "Canadian Dollar ($)"),
        CurrencyOption("AUD", "AU$", "Australian Dollar ($)")
    )
}
