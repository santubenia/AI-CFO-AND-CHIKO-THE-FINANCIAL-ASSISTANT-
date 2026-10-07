package com.example.ui.chiko

import android.util.Log
import com.example.BuildConfig
import com.example.ui.ExpenseUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiCfoClient {

    private const val TAG = "GeminiCfoClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateCfoResponse(
        conversationHistory: List<ChikoMessage>,
        uiState: ExpenseUiState,
        netWorth: Double,
        currencySymbol: String
    ): CfoResponseResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val lastUserMessage = conversationHistory.lastOrNull { it.sender == "USER" }?.text ?: ""

        // If key is empty or placeholder, fallback to local intelligent financial engine
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ChikoCfoEngine.answerQuery(lastUserMessage, uiState, netWorth, currencySymbol)
        }

        try {
            val systemPrompt = "You are Chiko, an elite Chief Financial Officer (CFO) and Chief Wealth Strategist at CoE (Council of Economics). " +
                    "Your role is to guide the user to financial independence, high-alpha portfolio compounding, and disciplined budgeting. " +
                    "Current user financial data:\n" +
                    "- Consolidated Net Worth: $currencySymbol$netWorth\n" +
                    "- Monthly Spending: $currencySymbol${uiState.monthExpenseTotal} (Income: $currencySymbol${uiState.monthIncomeTotal})\n" +
                    "- Selected Day Sales: Revenue $currencySymbol${uiState.selectedDaySalesRevenue}, Gross Profit $currencySymbol${uiState.selectedDaySalesProfit}, Margin: ${String.format("%.1f", uiState.selectedDaySalesMargin)}%\n" +
                    "- Over-budget alerts: ${uiState.exceededBudgetWarnings.joinToString { "${it.category} (Limit: $currencySymbol${it.limit}, Spent: $currencySymbol${it.spent})" }.ifBlank { "None, all under control" }}\n" +
                    "Formatting Rules:\n" +
                    "1. Whenever presenting numerical comparisons, portfolio allocations, budget numbers, market indices, or financial summaries, ALWAYS provide a Markdown Excel Table with clear headers (| Header 1 | Header 2 | ...) so the app renders an interactive Microsoft Excel-style spreadsheet.\n" +
                    "2. Use bold headers (### Title), concise bullet points, and actionable executive advice.\n" +
                    "3. Analyze both Indian markets (NSE/BSE, Nifty 50, Sensex, EPFO, ESI) and Global markets (US Tech, S&P 500, Nasdaq, Gold, commodities)."

            val rootJson = JSONObject()

            // System Instruction
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemPrompt))
            sysObj.put("parts", sysParts)
            rootJson.put("systemInstruction", sysObj)

            // Conversation history (limit to last 10 messages for speed)
            val contentsArray = JSONArray()
            val recentMessages = conversationHistory.takeLast(10)
            for (msg in recentMessages) {
                val turnObj = JSONObject()
                turnObj.put("role", if (msg.sender == "USER") "user" else "model")
                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", msg.text))
                turnObj.put("parts", partsArray)
                contentsArray.put(turnObj)
            }
            rootJson.put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = rootJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API returned error code ${response.code}: $responseBody")
                return@withContext ChikoCfoEngine.answerQuery(lastUserMessage, uiState, netWorth, currencySymbol)
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                val chartType = when {
                    lastUserMessage.contains("market", ignoreCase = true) || lastUserMessage.contains("stock", ignoreCase = true) || lastUserMessage.contains("nifty", ignoreCase = true) -> CfoVisualChartType.MARKET_MOMENTUM
                    lastUserMessage.contains("asset", ignoreCase = true) || lastUserMessage.contains("net worth", ignoreCase = true) || lastUserMessage.contains("balance", ignoreCase = true) || lastUserMessage.contains("allocation", ignoreCase = true) -> CfoVisualChartType.ASSET_ALLOCATION
                    lastUserMessage.contains("budget", ignoreCase = true) || lastUserMessage.contains("warn", ignoreCase = true) || lastUserMessage.contains("limit", ignoreCase = true) -> CfoVisualChartType.BUDGET_ANALYSIS
                    lastUserMessage.contains("plan", ignoreCase = true) || lastUserMessage.contains("compound", ignoreCase = true) || lastUserMessage.contains("target", ignoreCase = true) -> CfoVisualChartType.WEALTH_COMPOUNDING
                    else -> null
                }

                CfoResponseResult(
                    text = text.trim(),
                    chartType = chartType,
                    caption = if (chartType != null) "AI CFO Telemetry Visual" else null,
                    emotion = CfoVoiceEmotion.CONFIDENT_CFO
                )
            } else {
                ChikoCfoEngine.answerQuery(lastUserMessage, uiState, netWorth, currencySymbol)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed, using local CFO brain", e)
            ChikoCfoEngine.answerQuery(lastUserMessage, uiState, netWorth, currencySymbol)
        }
    }
}
