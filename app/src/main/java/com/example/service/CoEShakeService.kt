package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.MainActivity
import com.example.util.NotificationHelper
import kotlin.math.abs

/**
 * Background Service ensuring the Shake feature is available when opted in by user,
 * with battery-optimized vibration settings and launch debouncing.
 */
class CoEShakeService : Service(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var vibrator: Vibrator? = null

    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var lastShakeLaunchTime: Long = 0
    private var vibrationMode: String = "Soft (Battery Saver)"
    private var launchMode: String = "FIRST_TIME_ONLY"
    private var hasLaunchedSinceClosed: Boolean = false

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("VIBRATION_MODE")?.let {
            vibrationMode = it
        }
        intent?.getStringExtra("LAUNCH_MODE")?.let {
            launchMode = it
        }
        if (intent?.getBooleanExtra("RESET_LAUNCH_FLAG", false) == true) {
            hasLaunchedSinceClosed = false
        }

        try {
            val notification = NotificationHelper.buildShakeForegroundNotification(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    try {
                        startForeground(
                            NotificationHelper.NOTIFICATION_SHAKE_ID,
                            notification,
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        )
                    } catch (e: SecurityException) {
                        try {
                            startForeground(
                                NotificationHelper.NOTIFICATION_SHAKE_ID,
                                notification,
                                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                            )
                        } catch (e2: Exception) {
                            startForeground(NotificationHelper.NOTIFICATION_SHAKE_ID, notification)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    startForeground(
                        NotificationHelper.NOTIFICATION_SHAKE_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
                    )
                }
            } else {
                startForeground(NotificationHelper.NOTIFICATION_SHAKE_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("CoEShakeService", "Unable to start foreground service safely", e)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val gX = event.values[0] / SensorManager.GRAVITY_EARTH
        val gY = event.values[1] / SensorManager.GRAVITY_EARTH
        val gZ = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = kotlin.math.sqrt(gX * gX + gY * gY + gZ * gZ)

        val threshold = when {
            vibrationMode.contains("Fast") -> 1.75f
            vibrationMode.contains("Soft") -> 2.45f
            else -> 2.05f
        }

        if (gForce > threshold) {
            val curTime = System.currentTimeMillis()
            if ((curTime - lastShakeLaunchTime) > 1200) {
                lastShakeLaunchTime = curTime
                onDeviceShaken()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun onDeviceShaken() {
        if (launchMode == "FIRST_TIME_ONLY" && hasLaunchedSinceClosed) {
            return
        }
        hasLaunchedSinceClosed = true

        // Haptic feedback
        if (vibrationMode != "Off") {
            val duration = if (vibrationMode.contains("Soft")) 40L else 90L
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(duration)
                }
            } catch (_: Exception) {}
        }

        // 1. High-Priority Full Screen Intent Notification
        try {
            NotificationHelper.showShakeAlertNotification(this)
        } catch (e: Exception) {
            android.util.Log.e("CoEShakeService", "Failed to show shake notification", e)
        }

        // 2. Direct Activity Launch on Unlocked Device Home Screen
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_TRIGGER_SHAKE", true)
            }
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.w("CoEShakeService", "Background startActivity gated by system overlay policy", e)
        }
    }
}
