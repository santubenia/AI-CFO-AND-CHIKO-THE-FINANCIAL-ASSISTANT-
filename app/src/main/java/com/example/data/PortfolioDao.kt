package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PortfolioAssetEntity in Room Database.
 */
@Dao
interface PortfolioDao {

    @Query("SELECT * FROM portfolio_assets ORDER BY isLiability ASC, balanceOrValue DESC")
    fun getAllAssets(): Flow<List<PortfolioAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: PortfolioAssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<PortfolioAssetEntity>)

    @Update
    suspend fun updateAsset(asset: PortfolioAssetEntity)

    @Delete
    suspend fun deleteAsset(asset: PortfolioAssetEntity)

    @Query("DELETE FROM portfolio_assets WHERE id = :id")
    suspend fun deleteAssetById(id: Long)

    @Query("SELECT COUNT(*) FROM portfolio_assets")
    suspend fun getAssetCount(): Int

    @Query("DELETE FROM portfolio_assets")
    suspend fun clearAll()
}
