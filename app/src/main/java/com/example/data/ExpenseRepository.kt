package com.example.data

import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository layer abstracting Room database operations for TransactionEntity,
 * BudgetEntity, SaleEntity, and PortfolioAssetEntity.
 * Starts with a clean production slate (no pre-seeded sample amount data).
 */
class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val saleDao: SaleDao,
    private val portfolioDao: PortfolioDao,
    private val recurringDao: RecurringTransactionDao? = null
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allExpenses: Flow<List<TransactionEntity>> = allTransactions
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allPortfolioAssets: Flow<List<PortfolioAssetEntity>> = portfolioDao.getAllAssets()
    val allRecurringTransactions: Flow<List<RecurringTransactionEntity>> =
        recurringDao?.getAllRecurring() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(startTime, endTime)
    }

    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(startTime, endTime)
    }

    fun getSalesBetween(startTime: Long, endTime: Long): Flow<List<SaleEntity>> {
        return saleDao.getSalesBetween(startTime, endTime)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun insertExpense(expense: TransactionEntity): Long {
        return transactionDao.insertTransaction(expense)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun updateExpense(expense: TransactionEntity) {
        transactionDao.updateTransaction(expense)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteExpense(expense: TransactionEntity) {
        transactionDao.deleteTransaction(expense)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun deleteExpenseById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun insertSale(sale: SaleEntity): Long {
        return saleDao.insertSale(sale)
    }

    suspend fun updateSale(sale: SaleEntity) {
        saleDao.updateSale(sale)
    }

    suspend fun deleteSale(sale: SaleEntity) {
        saleDao.deleteSale(sale)
    }

    suspend fun deleteSaleById(id: Long) {
        saleDao.deleteSaleById(id)
    }

    suspend fun insertOrUpdateBudget(budget: BudgetEntity) {
        budgetDao.insertOrUpdate(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.delete(budget)
    }

    suspend fun deleteBudgetByCategoryAndPeriod(category: String, period: String) {
        budgetDao.deleteByCategoryAndPeriod(category, period)
    }

    // Portfolio Operations
    suspend fun insertPortfolioAsset(asset: PortfolioAssetEntity): Long {
        return portfolioDao.insertAsset(asset)
    }

    suspend fun updatePortfolioAsset(asset: PortfolioAssetEntity) {
        portfolioDao.updateAsset(asset)
    }

    suspend fun deletePortfolioAsset(asset: PortfolioAssetEntity) {
        portfolioDao.deleteAsset(asset)
    }

    suspend fun deletePortfolioAssetById(id: Long) {
        portfolioDao.deleteAssetById(id)
    }

    // Recurring Transactions Operations
    suspend fun insertRecurring(recurring: RecurringTransactionEntity): Long {
        return recurringDao?.insertRecurring(recurring) ?: 0L
    }

    suspend fun updateRecurring(recurring: RecurringTransactionEntity) {
        recurringDao?.updateRecurring(recurring)
    }

    suspend fun deleteRecurring(recurring: RecurringTransactionEntity) {
        recurringDao?.deleteRecurring(recurring)
    }

    suspend fun deleteRecurringById(id: Long) {
        recurringDao?.deleteRecurringById(id)
    }

    suspend fun checkAndExecuteDueRecurringTransactions(): Int {
        if (recurringDao == null) return 0
        val now = System.currentTimeMillis()
        val dueList = recurringDao.getDueRecurring(now)
        var count = 0
        for (item in dueList) {
            val transaction = TransactionEntity(
                amount = item.amount,
                category = item.category,
                date = item.nextDueDate,
                description = "[Auto Recurring] ${item.title}",
                type = item.type,
                paymentMethod = item.paymentMethod,
                note = "Auto-executed recurring ${item.frequency.lowercase()} transaction"
            )
            transactionDao.insertTransaction(transaction)
            count++

            // Advance nextDueDate
            val cal = Calendar.getInstance()
            cal.timeInMillis = item.nextDueDate
            when (item.frequency.uppercase()) {
                "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                else -> cal.add(Calendar.MONTH, 1)
            }
            recurringDao.updateRecurring(
                item.copy(
                    nextDueDate = cal.timeInMillis,
                    lastExecutedDate = now
                )
            )
        }
        return count
    }

    suspend fun clearAll() {
        transactionDao.clearAll()
        budgetDao.clearAll()
        saleDao.clearAll()
        portfolioDao.clearAll()
        recurringDao?.clearAll()
    }

    suspend fun seedInitialDataIfEmpty() {
        // Clean production slate: no sample amount data or placeholder transactions.
        // User starts with clean zero balances for genuine real-world accounting.
    }
}

typealias TransactionRepository = ExpenseRepository
