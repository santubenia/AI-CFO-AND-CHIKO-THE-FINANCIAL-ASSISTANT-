package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.ExpenseRepository
import com.example.data.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise CSV Import and Export engine for CoE financial accounts.
 * Allows backup, offline export, and updating account records via standard spreadsheet CSV files.
 */
object CsvSyncManager {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Converts a list of TransactionEntity records into standard RFC 4180 CSV text.
     */
    fun exportToCsvString(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        // CSV Header
        sb.append("ID,Date,Title,Amount,Category,Type,PaymentMethod,Note,Tag\n")

        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.date))
            val cleanTitle = escapeCsvField(tx.title.ifEmpty { tx.description })
            val cleanCategory = escapeCsvField(tx.category)
            val cleanType = escapeCsvField(tx.type)
            val cleanPayment = escapeCsvField(tx.paymentMethod)
            val cleanNote = escapeCsvField(tx.note)
            val cleanTag = escapeCsvField(tx.tag)

            sb.append("${tx.id},\"$dateStr\",\"$cleanTitle\",${tx.amount},\"$cleanCategory\",\"$cleanType\",\"$cleanPayment\",\"$cleanNote\",\"$cleanTag\"\n")
        }

        return sb.toString()
    }

    /**
     * Shares the CSV file via Android Share sheet.
     */
    fun shareCsvFile(context: Context, transactions: List<TransactionEntity>) {
        try {
            val csvContent = exportToCsvString(transactions)
            val fileName = "CoE_Financial_Backup_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csvContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "CoE Account Data Backup")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export / Backup CoE Account CSV"))
        } catch (e: Exception) {
            // Fallback: Copy to clipboard and open generic text share
            val csvContent = exportToCsvString(transactions)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("CoE Account CSV", csvContent))
            Toast.makeText(context, "Copied CSV Backup to clipboard!", Toast.LENGTH_SHORT).show()

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csvContent)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share CSV Data"))
        }
    }

    /**
     * Imports transactions from raw CSV text content into the Room database.
     */
    suspend fun importFromCsvString(
        csvText: String,
        repository: ExpenseRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val lines = csvText.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("CSV content is empty"))
            }

            var successCount = 0
            val dataLines = if (lines[0].contains("Amount", ignoreCase = true) || lines[0].contains("Date", ignoreCase = true)) {
                lines.drop(1)
            } else {
                lines
            }

            for (line in dataLines) {
                val tokens = parseCsvLine(line)
                if (tokens.size < 4) continue

                // Format: ID, Date, Title, Amount, Category, Type, PaymentMethod, Note, Tag
                // Or fallback: Date, Title, Amount, Category
                val title: String
                val amount: Double
                val category: String
                val type: String
                val paymentMethod: String
                val note: String
                val dateMillis: Long

                if (tokens.size >= 7 && tokens[3].toDoubleOrNull() != null) {
                    dateMillis = parseDateSafe(tokens[1])
                    title = tokens[2]
                    amount = tokens[3].toDoubleOrNull() ?: 0.0
                    category = tokens[4].ifEmpty { "General" }
                    type = if (tokens[5].equals("INCOME", ignoreCase = true)) "INCOME" else "EXPENSE"
                    paymentMethod = tokens[6].ifEmpty { "Cash" }
                    note = if (tokens.size > 7) tokens[7] else ""
                } else {
                    // Simpler format: Title, Amount, Category, Type
                    title = tokens[0]
                    amount = tokens[1].toDoubleOrNull() ?: continue
                    category = if (tokens.size > 2) tokens[2].ifEmpty { "General" } else "General"
                    type = if (tokens.size > 3 && tokens[3].equals("INCOME", ignoreCase = true)) "INCOME" else "EXPENSE"
                    paymentMethod = if (tokens.size > 4) tokens[4] else "Cash"
                    note = if (tokens.size > 5) tokens[5] else ""
                    dateMillis = System.currentTimeMillis()
                }

                if (amount > 0) {
                    val entity = TransactionEntity(
                        amount = amount,
                        category = category,
                        date = dateMillis,
                        description = title,
                        title = title,
                        type = type,
                        timestamp = dateMillis,
                        paymentMethod = paymentMethod,
                        note = note,
                        tag = "CSV_IMPORT"
                    )
                    repository.insertTransaction(entity)
                    successCount++
                }
            }

            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Imports transactions from an InputStream (such as SAF file picker).
     */
    suspend fun importFromInputStream(
        inputStream: InputStream,
        repository: ExpenseRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val content = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            importFromCsvString(content, repository)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escapeCsvField(field: String): String {
        return field.replace("\"", "\"\"")
    }

    private fun parseDateSafe(dateStr: String): Long {
        return try {
            dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim())
                current.clear()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }
}
