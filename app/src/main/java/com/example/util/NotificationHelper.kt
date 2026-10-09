package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_SHAKE_ID = "coe_shake_channel"
    const val CHANNEL_BUDGET_ID = "coe_budget_channel"
    const val NOTIFICATION_SHAKE_ID = 1001
    const val NOTIFICATION_BUDGET_ID = 2001
    const val NOTIFICATION_NEAR_BUDGET_ID = 2501

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val shakeChannel = NotificationChannel(
                CHANNEL_SHAKE_ID,
                "CoE Shake Logger Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background shake detection to quickly log transactions anytime"
                setShowBadge(false)
            }

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ID,
                "Budget Warnings & Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant alerts when expense categories approach or exceed spending limits"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(shakeChannel)
            notificationManager.createNotificationChannel(budgetChannel)
        }
    }

    fun buildShakeForegroundNotification(context: Context): android.app.Notification {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TRIGGER_SHAKE", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(context, CHANNEL_SHAKE_ID)
            .setSmallIcon(R.drawable.ic_coe_logo)
            .setContentTitle("⚡ CoE Shake Active")
            .setContentText("Shake your phone anytime to quickly record an expense, sale, or investment!")
            .setStyle(NotificationCompat.BigTextStyle().bigText("⚡ CoE Shake Active: Shake your phone anytime to quickly record an expense, sale, or investment!"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun showBudgetExceededNotification(
        context: Context,
        category: String,
        spent: Double,
        limit: Double,
        currencySymbol: String,
        periodName: String = "Monthly"
    ) {
        createNotificationChannels(context)
        val overage = spent - limit

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO_TAB", "BUDGETS")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ID)
            .setSmallIcon(R.drawable.ic_coe_logo)
            .setContentTitle("🚨 $periodName Budget Exceeded: $category")
            .setContentText("You've spent $currencySymbol${String.format("%.2f", spent)} (Over by $currencySymbol${String.format("%.2f", overage)})")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Alert: Your $periodName spending for $category has reached $currencySymbol${String.format("%.2f", spent)}, exceeding your limit of $currencySymbol${String.format("%.2f", limit)} by $currencySymbol${String.format("%.2f", overage)}."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_BUDGET_ID + (category + periodName).hashCode(), notification)
    }

    fun showNearBudgetLimitNotification(
        context: Context,
        category: String,
        spent: Double,
        limit: Double,
        percentage: Float,
        currencySymbol: String,
        periodName: String = "Monthly"
    ) {
        createNotificationChannels(context)
        val remaining = (limit - spent).coerceAtLeast(0.0)
        val pctFormatted = (percentage * 100).toInt()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO_TAB", "BUDGETS")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ID)
            .setSmallIcon(R.drawable.ic_coe_logo)
            .setContentTitle("⚠️ Nearing $periodName Budget Limit: $category ($pctFormatted%)")
            .setContentText("Spent $currencySymbol${String.format("%.2f", spent)} of $currencySymbol${String.format("%.2f", limit)} • $currencySymbol${String.format("%.2f", remaining)} left")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Budget Guard Alert: You've reached $pctFormatted% of your $periodName spending limit for $category ($currencySymbol${String.format("%.2f", spent)} / $currencySymbol${String.format("%.2f", limit)}). Only $currencySymbol${String.format("%.2f", remaining)} left for the rest of this period."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_NEAR_BUDGET_ID + (category + periodName).hashCode(), notification)
    }

    const val NOTIFICATION_SHAKE_ALERT_ID = 3001

    fun showShakeAlertNotification(context: Context) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_TRIGGER_SHAKE", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SHAKE_ID)
            .setSmallIcon(R.drawable.ic_coe_logo)
            .setContentTitle("⚡ CoE Quick Shake Triggered!")
            .setContentText("Tap or swipe down to immediately log your transaction or sales.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "⚡ Device Shake Detected! Tap now to record an expense, sales dairy entry, or investment into CoE."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_coe_logo, "Log Now", pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_SHAKE_ALERT_ID, notification)
    }
}
