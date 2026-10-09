package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE period = :period")
    fun getBudgetsByPeriod(period: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE category = :category AND period = :period")
    suspend fun deleteByCategoryAndPeriod(category: String, period: String)

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun getBudgetCount(): Int

    @Query("DELETE FROM budgets")
    suspend fun clearAll()
}
