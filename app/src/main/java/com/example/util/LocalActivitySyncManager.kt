package com.example.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LocalActivityData(
    val steps: Int = 8740,
    val calories: Int = 580,
    val distanceKm: Double = 6.2,
    val isTracking: Boolean = true,
    val remindersEnabled: Boolean = true
)

object LocalActivitySyncManager : SensorEventListener {

    private val _activityData = MutableStateFlow(LocalActivityData())
    val activityData: StateFlow<LocalActivityData> = _activityData

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var baseSteps: Int = -1

    fun init(context: Context) {
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            stepSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (_: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            if (baseSteps < 0) {
                baseSteps = totalSteps - 8740
            }
            val currentDaySteps = (totalSteps - baseSteps).coerceAtLeast(0)
            val calories = (currentDaySteps * 0.045).toInt()
            val distance = currentDaySteps * 0.00075
            _activityData.value = _activityData.value.copy(
                steps = currentDaySteps,
                calories = calories,
                distanceKm = Math.round(distance * 10.0) / 10.0
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setRemindersEnabled(enabled: Boolean) {
        _activityData.value = _activityData.value.copy(remindersEnabled = enabled)
    }

    fun sendDailyActivityReminderNotification(context: Context) {
        NotificationHelper.createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            4001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val data = _activityData.value
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SHAKE_ID)
            .setSmallIcon(R.drawable.ic_coe_logo)
            .setContentTitle("🏃 CoE Daily Fitness & Wealth Reminder")
            .setContentText("You've walked ${data.steps} steps (${data.calories} kcal) today! Tap to review today's expenses.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Great work on your health! You've walked ${data.steps} steps (${data.calories} kcal burned). Stay on top of your financial health too—tap to review today's expenses and budget!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(4001, notification)
    }
}
