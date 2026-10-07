package com.example.data

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.R
import com.example.util.DateUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.StringReader

/**
 * Manages two-way synchronization between the local Room database and Firebase Firestore.
 * Automatically persists user transactions to the cloud under users/{userId}/transactions
 * allowing seamless multi-device persistence and real-time backup.
 * Also provides CSV import and export capabilities for account data backup and restoration.
 */
class FirestoreSyncManager(private val context: Context) {

    private val db: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val dbId = context.getString(R.string.firestore_database_id)
                FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize custom Firestore database instance", e)
            null
        }
    }

    /**
     * Uploads local Room transactions to Firestore for the authenticated user.
     */
    suspend fun uploadTransactionsToCloud(
        userId: String,
        localTransactions: List<TransactionEntity>
    ): Result<Int> = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore database is not available"))
        if (userId.isBlank() || userId == "guest_user" || userId == "guest_offline") {
            return@withContext Result.failure(IllegalArgumentException("User must be authenticated with Google to sync with cloud"))
        }

        try {
            var syncedCount = 0
            val userTxColl = firestore.collection("users").document(userId).collection("transactions")

            // Batch writes up to 400 items per batch
            val chunks = localTransactions.chunked(400)
            for (chunk in chunks) {
                val batch = firestore.batch()
                for (tx in chunk) {
                    val docRef = userTxColl.document(tx.id.toString())
                    val data = hashMapOf(
                        "id" to tx.id,
                        "amount" to tx.amount,
                        "category" to tx.category,
                        "date" to tx.date,
                        "description" to tx.description,
                        "type" to tx.type,
                        "paymentMethod" to tx.paymentMethod,
                        "note" to tx.note,
                        "tag" to tx.tag,
                        "lastModified" to System.currentTimeMillis()
                    )
                    batch.set(docRef, data, SetOptions.merge())
                }
                batch.commit().await()
                syncedCount += chunk.size
            }

            // Update user metadata doc
            firestore.collection("users").document(userId).set(
                hashMapOf(
                    "lastCloudSync" to System.currentTimeMillis(),
                    "deviceTransactionCount" to localTransactions.size
                ),
                SetOptions.merge()
            ).await()

            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload transactions to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads cloud transactions from Firestore and merges them into the local Room database.
     */
    suspend fun syncFromCloud(
        userId: String,
        repository: ExpenseRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext Result.failure(IllegalStateException("Firestore database is not available"))
        if (userId.isBlank() || userId == "guest_user" || userId == "guest_offline") {
            return@withContext Result.failure(IllegalArgumentException("User must be authenticated with Google to sync from cloud"))
        }

        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .await()

            var importedCount = 0
            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                val id = (data["id"] as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: 0L
                val amount = (data["amount"] as? Number)?.toDouble() ?: 0.0
                val category = data["category"] as? String ?: "General"
                val date = (data["date"] as? Number)?.toLong() ?: System.currentTimeMillis()
                val description = data["description"] as? String ?: ""
                val type = data["type"] as? String ?: "EXPENSE"
                val paymentMethod = data["paymentMethod"] as? String ?: "Cash"
                val note = data["note"] as? String ?: ""
                val tag = data["tag"] as? String ?: ""

                val entity = TransactionEntity(
                    id = id,
                    amount = amount,
                    category = category,
                    date = date,
                    description = description,
                    title = description.ifEmpty { category },
                    type = type,
                    timestamp = date,
                    paymentMethod = paymentMethod,
                    note = note,
                    tag = tag
                )
                repository.insertTransaction(entity)
                importedCount++
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync transactions from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Performs a bidirectional sync: uploads local Room data and fetches remote records.
     */
    suspend fun performFullCloudSync(
        userId: String,
        localTransactions: List<TransactionEntity>,
        repository: ExpenseRepository
    ): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        val uploadRes = uploadTransactionsToCloud(userId, localTransactions)
        val downloadRes = syncFromCloud(userId, repository)

        if (uploadRes.isSuccess && downloadRes.isSuccess) {
            Result.success(Pair(uploadRes.getOrDefault(0), downloadRes.getOrDefault(0)))
        } else {
            val ex = uploadRes.exceptionOrNull() ?: downloadRes.exceptionOrNull() ?: Exception("Cloud sync failed")
            Result.failure(ex)
        }
    }

    /**
     * Generates a standard formatted CSV backup string from transaction entities.
     */
    fun exportTransactionsToCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Amount,Category,DateMillis,FormattedDate,Description,Type,PaymentMethod,Note,Tag\n")
        for (tx in transactions) {
            val formattedDate = DateUtils.formatDate(tx.date)
            sb.append("${tx.id},")
                .append("${tx.amount},")
                .append("${escapeCsv(tx.category)},")
                .append("${tx.date},")
                .append("${escapeCsv(formattedDate)},")
                .append("${escapeCsv(tx.description)},")
                .append("${tx.type},")
                .append("${escapeCsv(tx.paymentMethod)},")
                .append("${escapeCsv(tx.note)},")
                .append("${escapeCsv(tx.tag)}\n")
        }
        return sb.toString()
    }

    /**
     * Shares CSV backup via Android Share Sheet.
     */
    fun shareCsvBackup(context: Context, transactions: List<TransactionEntity>) {
        val csvData = exportTransactionsToCsv(transactions)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvData)
            putExtra(Intent.EXTRA_SUBJECT, "CoE Finance Backup - ${DateUtils.formatDate(System.currentTimeMillis())}.csv")
            type = "text/csv"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Export & Backup CoE Account CSV").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    /**
     * Imports transactions from CSV content into Room database.
     */
    suspend fun importTransactionsFromCsv(
        csvText: String,
        repository: ExpenseRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var count = 0
            val reader = BufferedReader(StringReader(csvText))
            var line: String? = reader.readLine() // Read header
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.isBlank()) continue
                val parts = parseCsvLine(currentLine)
                if (parts.size >= 3) {
                    val amount = parts.getOrNull(1)?.toDoubleOrNull() ?: continue
                    val category = parts.getOrNull(2)?.ifBlank { "General" } ?: "General"
                    val date = parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis()
                    val description = parts.getOrNull(5) ?: parts.getOrNull(4) ?: ""
                    val type = parts.getOrNull(6)?.uppercase()?.takeIf { it == "INCOME" || it == "EXPENSE" } ?: "EXPENSE"
                    val paymentMethod = parts.getOrNull(7)?.ifBlank { "Cash" } ?: "Cash"
                    val note = parts.getOrNull(8) ?: ""
                    val tag = parts.getOrNull(9) ?: ""

                    val entity = TransactionEntity(
                        id = 0, // Auto-generate new primary key on import
                        amount = amount,
                        category = category,
                        date = date,
                        description = description,
                        title = description.ifEmpty { category },
                        type = type,
                        timestamp = date,
                        paymentMethod = paymentMethod,
                        note = note,
                        tag = tag
                    )
                    repository.insertTransaction(entity)
                    count++
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse and import CSV", e)
            Result.failure(e)
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                        sb.append('\"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    companion object {
        private const val TAG = "FirestoreSyncManager"
    }
}
