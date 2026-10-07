package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room Database for the CoE application.
 * Persists TransactionEntity, BudgetEntity, SaleEntity, and PortfolioAssetEntity.
 */
@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        SaleEntity::class,
        PortfolioAssetEntity::class,
        RecurringTransactionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    fun expenseDao(): TransactionDao = transactionDao()
    abstract fun budgetDao(): BudgetDao
    abstract fun saleDao(): SaleDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
