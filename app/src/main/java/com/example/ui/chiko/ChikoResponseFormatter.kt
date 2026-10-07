package com.example.ui.chiko

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

/**
 * Formats AI CFO response text with rich Markdown parsing, highlighted stats,
 * executive callout cards, and high-fidelity Excel/Spreadsheet table components with clipboard export.
 */
@Composable
fun FormattedCfoResponse(
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val blocks = parseContentBlocks(text)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Header -> {
                    Text(
                        text = block.text,
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp)
                            2 -> MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp)
                            else -> MaterialTheme.typography.labelLarge
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is ContentBlock.Table -> {
                    ExcelSpreadsheetView(
                        headers = block.headers,
                        rows = block.rows,
                        rawMarkdown = block.rawText,
                        context = context
                    )
                }
                is ContentBlock.Callout -> {
                    ExecutiveCalloutBox(
                        type = block.type,
                        text = block.text
                    )
                }
                is ContentBlock.Bullet -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = renderAnnotatedMarkdown(block.text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
                is ContentBlock.Paragraph -> {
                    Text(
                        text = renderAnnotatedMarkdown(block.text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

/**
 * Excel / Spreadsheet component for financial tables in CFO chat.
 * Provides Microsoft Excel style styling, column auto-fit, zebra striping,
 * cell status badges, and one-tap TSV/CSV copy for instant paste into Excel or Google Sheets.
 */
@Composable
fun ExcelSpreadsheetView(
    headers: List<String>,
    rows: List<List<String>>,
    rawMarkdown: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    val excelEmerald = Color(0xFF107C41) // Microsoft Excel brand emerald green
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Column {
            // Excel Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(excelEmerald)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Executive Spreadsheet (${rows.size} rows)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val tsv = formatAsTsv(headers, rows)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Excel Spreadsheet", tsv))
                            Toast.makeText(context, "Copied Excel spreadsheet (TSV) to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy Excel Table",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Scrollable Grid Table
            val horizontalScroll = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScroll)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    // Column Headers Row
                    Row(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(vertical = 7.dp)
                    ) {
                        headers.forEachIndexed { index, header ->
                            Text(
                                text = header.trim(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .width(135.dp)
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Data Rows (Zebra striped with Smart Cell Badges)
                    rows.forEachIndexed { rowIndex, row ->
                        val rowBg = if (rowIndex % 2 == 0)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        else
                            Color.Transparent

                        Row(
                            modifier = Modifier
                                .background(rowBg, RoundedCornerShape(4.dp))
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.forEachIndexed { colIndex, cell ->
                                Box(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .padding(horizontal = 6.dp)
                                ) {
                                    SmartTableCell(text = cell.trim(), isFirstCol = colIndex == 0)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders cell content with smart status badges for financial states.
 */
@Composable
private fun SmartTableCell(text: String, isFirstCol: Boolean) {
    val lower = text.lowercase()
    val isPositiveStatus = lower.contains("bullish") || lower.contains("positive") || lower.contains("on track") || lower.contains("completed") || lower.contains("gain") || text.contains("+")
    val isNegativeStatus = lower.contains("bearish") || lower.contains("over limit") || lower.contains("loss") || lower.contains("danger") || (text.contains("-") && text.any { it.isDigit() } && !text.contains("/"))
    val isWarningStatus = lower.contains("caution") || lower.contains("moderate") || lower.contains("high") || lower.contains("review")

    if (isPositiveStatus && text.length <= 15) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = IncomeGreen.copy(alpha = 0.15f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = IncomeGreen,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    } else if (isNegativeStatus && text.length <= 15) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = ExpenseRed.copy(alpha = 0.15f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = ExpenseRed,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    } else if (isWarningStatus && text.length <= 15) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF59E0B).copy(alpha = 0.15f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD97706),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            fontWeight = if (isFirstCol) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Callout Card for Executive alerts and recommendations.
 */
@Composable
private fun ExecutiveCalloutBox(
    type: CalloutType,
    text: String
) {
    val (containerColor, contentColor, icon) = when (type) {
        CalloutType.WARNING -> Triple(ExpenseRed.copy(alpha = 0.1f), ExpenseRed, Icons.Default.Warning)
        CalloutType.SUCCESS -> Triple(IncomeGreen.copy(alpha = 0.1f), IncomeGreen, Icons.Default.CheckCircle)
        CalloutType.TIP -> Triple(Color(0xFFF59E0B).copy(alpha = 0.12f), Color(0xFFD97706), Icons.Default.Lightbulb)
        CalloutType.INFO -> Triple(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), MaterialTheme.colorScheme.primary, Icons.Default.Info)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = renderAnnotatedMarkdown(text),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

private enum class CalloutType { WARNING, SUCCESS, TIP, INFO }

private sealed class ContentBlock {
    data class Header(val text: String, val level: Int) : ContentBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>, val rawText: String) : ContentBlock()
    data class Bullet(val text: String) : ContentBlock()
    data class Callout(val type: CalloutType, val text: String) : ContentBlock()
    data class Paragraph(val text: String) : ContentBlock()
}

/**
 * Parses markdown text into discrete presentation blocks.
 */
private fun parseContentBlocks(text: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val lines = text.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i].trimEnd()

        // Check for table start (e.g. | Col 1 | Col 2 |)
        if (line.startsWith("|") && line.endsWith("|")) {
            val tableLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                tableLines.add(lines[i].trim())
                i++
            }

            if (tableLines.size >= 2) {
                val headers = tableLines[0].split("|").filter { it.isNotBlank() }
                val dataLines = tableLines.drop(1).filter { !it.contains("---") }
                val rows = dataLines.map { dl ->
                    dl.split("|").filter { it.isNotBlank() }
                }
                blocks.add(ContentBlock.Table(headers, rows, tableLines.joinToString("\n")))
            }
            continue
        }

        // Header Check
        if (line.startsWith("### ")) {
            blocks.add(ContentBlock.Header(line.removePrefix("### ").trim(), 3))
            i++
            continue
        } else if (line.startsWith("## ")) {
            blocks.add(ContentBlock.Header(line.removePrefix("## ").trim(), 2))
            i++
            continue
        } else if (line.startsWith("# ")) {
            blocks.add(ContentBlock.Header(line.removePrefix("# ").trim(), 1))
            i++
            continue
        }

        // Callout Checks
        if (line.startsWith("⚠️") || line.contains("**Action Alert**") || line.contains("**Budget Alert**")) {
            blocks.add(ContentBlock.Callout(CalloutType.WARNING, line.removePrefix("⚠️").trim()))
            i++
            continue
        } else if (line.startsWith("✅") || line.contains("**Budget Health**") || line.contains("Excellent discipline")) {
            blocks.add(ContentBlock.Callout(CalloutType.SUCCESS, line.removePrefix("✅").trim()))
            i++
            continue
        } else if (line.startsWith("💡") || line.contains("**CFO Recommendation**") || line.contains("**Action Plan**")) {
            blocks.add(ContentBlock.Callout(CalloutType.TIP, line.removePrefix("💡").trim()))
            i++
            continue
        }

        // Bullet Check
        if (line.startsWith("• ") || line.startsWith("- ") || line.startsWith("* ")) {
            blocks.add(ContentBlock.Bullet(line.substring(2).trim()))
            i++
            continue
        }

        // Standard Paragraph (if not blank)
        if (line.isNotBlank()) {
            blocks.add(ContentBlock.Paragraph(line.trim()))
        }
        i++
    }

    return blocks
}

/**
 * Renders annotated string supporting **bold** text and code tags cleanly.
 */
@Composable
private fun renderAnnotatedMarkdown(text: String) = buildAnnotatedString {
    val parts = text.split("**")
    var isBold = false
    for (part in parts) {
        if (isBold) {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
                append(part)
            }
        } else {
            append(part)
        }
        isBold = !isBold
    }
}

/**
 * Exports spreadsheet table into Tab-Separated Values (TSV) compatible with Excel / Google Sheets.
 */
private fun formatAsTsv(headers: List<String>, rows: List<List<String>>): String {
    val sb = StringBuilder()
    sb.append(headers.joinToString("\t")).append("\n")
    rows.forEach { row ->
        sb.append(row.joinToString("\t")).append("\n")
    }
    return sb.toString()
}
