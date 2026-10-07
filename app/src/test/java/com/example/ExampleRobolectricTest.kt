package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CoE", appName)
  }

  @Test
  fun `shake detector invokes callback on shake trigger`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    var shakeTriggered = false
    val detector = com.example.util.ShakeDetector(context) {
      shakeTriggered = true
    }

    assertEquals(true, detector.isEnabled)
    detector.simulateShake()
    assertEquals(true, shakeTriggered)

    // Verify disabled detector does not trigger
    shakeTriggered = false
    detector.isEnabled = false
    detector.simulateShake()
    assertEquals(false, shakeTriggered)
  }

  @Test
  fun `room database stores and retrieves transaction with amount category date and description`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val testTransaction = TransactionEntity(
      amount = 42.50,
      category = "Groceries",
      date = 1759000000000L,
      description = "Weekly organic market run"
    )

    db.transactionDao().insertTransaction(testTransaction)
    val transactions = db.transactionDao().getAllTransactions().first()

    assertEquals(1, transactions.size)
    val retrieved = transactions[0]
    assertEquals(42.50, retrieved.amount, 0.001)
    assertEquals("Groceries", retrieved.category)
    assertEquals(1759000000000L, retrieved.date)
    assertEquals("Weekly organic market run", retrieved.description)

    db.close()
  }

  @Test
  fun `room database stores and retrieves sale record with profit and margin calculations`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val testSale = com.example.data.SaleEntity(
      productName = "Artisan Coffee Beans",
      quantity = 4,
      sellingPrice = 20.00,
      costPrice = 8.00
    )

    db.saleDao().insertSale(testSale)
    val sales = db.saleDao().getAllSales().first()

    assertEquals(1, sales.size)
    val retrieved = sales[0]
    assertEquals("Artisan Coffee Beans", retrieved.productName)
    assertEquals(4, retrieved.quantity)
    assertEquals(80.00, retrieved.totalRevenue, 0.001)
    assertEquals(48.00, retrieved.totalProfit, 0.001)
    assertEquals(60.0, retrieved.profitMarginPercent, 0.001)

    db.close()
  }

  @Test
  fun `room database stores and calculates portfolio assets and liabilities for net worth`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val bankAsset = com.example.data.PortfolioAssetEntity(
      name = "HDFC Savings",
      category = "BANK",
      balanceOrValue = 150000.0,
      isLiability = false
    )
    val cardLiability = com.example.data.PortfolioAssetEntity(
      name = "Axis Credit Card",
      category = "CREDIT_CARD",
      balanceOrValue = 20000.0,
      isLiability = true
    )

    db.portfolioDao().insertAsset(bankAsset)
    db.portfolioDao().insertAsset(cardLiability)

    val assets = db.portfolioDao().getAllAssets().first()
    assertEquals(2, assets.size)

    val netWorth = assets.filter { !it.isLiability }.sumOf { it.balanceOrValue } - assets.filter { it.isLiability }.sumOf { it.balanceOrValue }
    assertEquals(130000.0, netWorth, 0.001)

    db.close()
  }
}
