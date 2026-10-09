package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatCurrency(amount: Double, symbol: String = "$"): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            maximumFractionDigits = 2
        }
        return "$symbol${formatter.format(amount)}"
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()
        cal.timeInMillis = timestamp

        val isSameDay = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        today.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        return when {
            isSameDay -> "Today"
            isYesterday -> "Yesterday"
            else -> {
                val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun formatDateFull(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatMonthYear(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatShortDay(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return "${formatDate(timestamp)} • ${formatTime(timestamp)}"
    }

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getStartOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getStartOfWeek(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun addDays(timestamp: Long, days: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.timeInMillis
    }

    fun getDaysLeftInCurrentMonth(): Int {
        val cal = Calendar.getInstance()
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        return (totalDays - currentDay + 1).coerceAtLeast(1)
    }

    // Quarterly (Q1: Jan-Mar, Q2: Apr-Jun, Q3: Jul-Sep, Q4: Oct-Dec)
    fun getStartOfQuarter(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val month = cal.get(Calendar.MONTH)
        val firstMonthOfQuarter = (month / 3) * 3
        cal.set(Calendar.MONTH, firstMonthOfQuarter)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfQuarter(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val month = cal.get(Calendar.MONTH)
        val lastMonthOfQuarter = ((month / 3) * 3) + 2
        cal.set(Calendar.MONTH, lastMonthOfQuarter)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun formatQuarterYear(timestamp: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val q = (cal.get(Calendar.MONTH) / 3) + 1
        val year = cal.get(Calendar.YEAR)
        val rangeStr = when (q) {
            1 -> "Jan - Mar"
            2 -> "Apr - Jun"
            3 -> "Jul - Sep"
            else -> "Oct - Dec"
        }
        return "Q$q $year ($rangeStr)"
    }

    fun getDaysLeftInCurrentQuarter(): Int {
        val now = System.currentTimeMillis()
        val end = getEndOfQuarter(now)
        val diff = end - now
        val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        return days.coerceAtLeast(1)
    }

    // Half-Yearly (H1: Jan-Jun, H2: Jul-Dec)
    fun getStartOfHalfYear(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val month = cal.get(Calendar.MONTH)
        val firstMonthOfHalf = if (month < 6) 0 else 6
        cal.set(Calendar.MONTH, firstMonthOfHalf)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfHalfYear(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val month = cal.get(Calendar.MONTH)
        val lastMonthOfHalf = if (month < 6) 5 else 11
        cal.set(Calendar.MONTH, lastMonthOfHalf)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun formatHalfYear(timestamp: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val h = if (cal.get(Calendar.MONTH) < 6) 1 else 2
        val year = cal.get(Calendar.YEAR)
        val rangeStr = if (h == 1) "Jan - Jun" else "Jul - Dec"
        return "H$h $year ($rangeStr)"
    }

    fun getDaysLeftInCurrentHalfYear(): Int {
        val now = System.currentTimeMillis()
        val end = getEndOfHalfYear(now)
        val diff = end - now
        val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        return days.coerceAtLeast(1)
    }

    // Yearly (Jan 1 - Dec 31)
    fun getStartOfYear(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfYear(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 31)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun formatYear(timestamp: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return "${cal.get(Calendar.YEAR)} Annual Plan"
    }

    fun getDaysLeftInCurrentYear(): Int {
        val now = System.currentTimeMillis()
        val end = getEndOfYear(now)
        val diff = end - now
        val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        return days.coerceAtLeast(1)
    }

    fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
