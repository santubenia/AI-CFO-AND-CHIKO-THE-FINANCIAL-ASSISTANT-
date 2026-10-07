package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CatBills
import com.example.ui.theme.CatEducation
import com.example.ui.theme.CatEntertainment
import com.example.ui.theme.CatFood
import com.example.ui.theme.CatGroceries
import com.example.ui.theme.CatHealth
import com.example.ui.theme.CatOther
import com.example.ui.theme.CatShopping
import com.example.ui.theme.CatTransport
import com.example.ui.theme.IncomeGreen

enum class CategoryType {
    EXPENSE,
    INCOME
}

data class CategoryItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: CategoryType = CategoryType.EXPENSE,
    val defaultSuggestions: List<String> = emptyList()
)

object Categories {
    val expenseCategories = listOf(
        CategoryItem(
            id = "Food & Dining",
            name = "Food & Dining",
            icon = Icons.Default.Restaurant,
            color = CatFood,
            defaultSuggestions = listOf("Morning Coffee", "Lunch", "Dinner", "Snacks", "Bakery")
        ),
        CategoryItem(
            id = "Groceries",
            name = "Groceries",
            icon = Icons.Default.LocalGroceryStore,
            color = CatGroceries,
            defaultSuggestions = listOf("Supermarket", "Fruits & Veggies", "Milk & Dairy", "Household items")
        ),
        CategoryItem(
            id = "Transport",
            name = "Transport",
            icon = Icons.Default.DirectionsCar,
            color = CatTransport,
            defaultSuggestions = listOf("Fuel / Gas", "Bus / Metro Pass", "Uber / Taxi", "Parking", "Toll")
        ),
        CategoryItem(
            id = "Shopping",
            name = "Shopping",
            icon = Icons.Default.ShoppingBag,
            color = CatShopping,
            defaultSuggestions = listOf("Clothing", "Electronics", "Home Decor", "Shoes", "Books")
        ),
        CategoryItem(
            id = "Bills & Utilities",
            name = "Bills & Utilities",
            icon = Icons.Default.ReceiptLong,
            color = CatBills,
            defaultSuggestions = listOf("Electricity", "Water bill", "Internet / Wi-Fi", "Mobile Recharge", "Rent")
        ),
        CategoryItem(
            id = "Entertainment",
            name = "Entertainment",
            icon = Icons.Default.ConfirmationNumber,
            color = CatEntertainment,
            defaultSuggestions = listOf("Cinema / Movie", "Streaming Subscription", "Video Games", "Concert")
        ),
        CategoryItem(
            id = "Health & Fitness",
            name = "Health & Fitness",
            icon = Icons.Default.Favorite,
            color = CatHealth,
            defaultSuggestions = listOf("Pharmacy / Medicine", "Gym Membership", "Doctor Visit", "Vitamins")
        ),
        CategoryItem(
            id = "Education",
            name = "Education",
            icon = Icons.Default.School,
            color = CatEducation,
            defaultSuggestions = listOf("Course / Tuition", "Books & Stationery", "Certifications")
        ),
        CategoryItem(
            id = "Other",
            name = "Other",
            icon = Icons.Default.MoreHoriz,
            color = CatOther,
            defaultSuggestions = listOf("Miscellaneous", "Donation", "Gifts", "Pet Care")
        )
    )

    val incomeCategories = listOf(
        CategoryItem(
            id = "Salary",
            name = "Salary",
            icon = Icons.Default.AccountBalanceWallet,
            color = IncomeGreen,
            type = CategoryType.INCOME,
            defaultSuggestions = listOf("Monthly Salary", "Advance", "Overtime")
        ),
        CategoryItem(
            id = "Freelance",
            name = "Freelance / Side Gig",
            icon = Icons.Default.AccountBalance,
            color = Color(0xFF0D9488),
            type = CategoryType.INCOME,
            defaultSuggestions = listOf("Client Project", "Consulting", "Design Work")
        ),
        CategoryItem(
            id = "Investment",
            name = "Investment / Dividends",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF059669),
            type = CategoryType.INCOME,
            defaultSuggestions = listOf("Stock Dividends", "Interest", "Crypto Profit")
        ),
        CategoryItem(
            id = "Gift & Others",
            name = "Gift / Bonus / Other",
            icon = Icons.Default.CardGiftcard,
            color = Color(0xFF10B981),
            type = CategoryType.INCOME,
            defaultSuggestions = listOf("Birthday Gift", "Festival Bonus", "Cashback / Refund")
        )
    )

    fun getCategoryItem(name: String): CategoryItem {
        return expenseCategories.find { it.name.equals(name, ignoreCase = true) }
            ?: incomeCategories.find { it.name.equals(name, ignoreCase = true) }
            ?: CategoryItem(
                id = name,
                name = name,
                icon = Icons.Default.MoreHoriz,
                color = CatOther
            )
    }
}
