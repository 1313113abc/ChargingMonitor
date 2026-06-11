package com.example.chargingmonitor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.chargingmonitor.MainActivity
import com.example.chargingmonitor.R

class ChargingMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "charging_monitor_channel"
        const val NOTIFICATION_ID = 1001
        const val THRESHOLD_MA = 90
        const val UPDATE_INTERVAL_MS = 2000L
    }

    private lateinit var notificationManager: NotificationManager
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateChargingStatus()
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isMonitoring) {
                updateChargingStatus()
                handler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification(0, false, "初始化中...")
        startForeground(NOTIFICATION_ID, notification)

        isMonitoring = true
        handler.post(updateRunnable)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isMonitoring = false
        handler.removeCallbacks(updateRunnable)
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }

    private fun updateChargingStatus() {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val currentRaw = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = normalizeCurrent(currentRaw)

        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL ||
                (currentMa > THRESHOLD_MA)

        val notification = buildNotification(currentMa, isCharging)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun normalizeCurrent(raw: Int): Int {
        return when {
            raw == Integer.MIN_VALUE -> 0
            Math.abs(raw) > 10000 -> raw / 1000
            else -> raw
        }
    }

    private fun buildNotification(currentMa: Int, isCharging: Boolean, overrideText: String? = null): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (smallIcon, title, content, color) = if (overrideText != null) {
            Quadruple(R.drawable.ic_status_unknown, "充电状态检测器", overrideText, getColor(R.color.gray))
        } else when {
            currentMa == 0 && !isCharging -> {
                Quadruple(
                    R.drawable.ic_status_unknown,
                    "⚡ 充电状态检测器",
                    "无法读取电流数据",
                    getColor(R.color.gray)
                )
            }
            isCharging -> {
                Quadruple(
                    R.drawable.ic_status_charging,
                    "🔋 正在充电",
                    "电流: ${currentMa}mA  |  阈值: ${THRESHOLD_MA}mA",
                    getColor(R.color.green)
                )
            }
            else -> {
                Quadruple(
                    R.drawable.ic_status_not_charging,
                    "🔋 未充电",
                    "电流: ${currentMa}mA  |  阈值: ${THRESHOLD_MA}mA",
                    getColor(R.color.red)
                )
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(smallIcon)
            .setColor(color)
            .setColorized(true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "充电状态监控",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "实时监控充电电流状态"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
