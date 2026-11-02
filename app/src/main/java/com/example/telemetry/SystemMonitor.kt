package com.example.telemetry

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MemoryStats(
    val totalMb: Long,
    val availableMb: Long,
    val usedMb: Long,
    val usedPercent: Int,
    val isLowMemory: Boolean
)

data class BatteryStats(
    val levelPercent: Int,
    val temperatureCelsius: Float,
    val statusText: String,
    val isPowerSaveMode: Boolean,
    val voltageMv: Int
)

class SystemMonitor(private val context: Context) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val _memoryStats = MutableStateFlow(getMemorySnapshot())
    val memoryStats: StateFlow<MemoryStats> = _memoryStats.asStateFlow()

    private val _batteryStats = MutableStateFlow(
        BatteryStats(
            levelPercent = 85,
            temperatureCelsius = 33.0f,
            statusText = "Good",
            isPowerSaveMode = false,
            voltageMv = 4100
        )
    )
    val batteryStats: StateFlow<BatteryStats> = _batteryStats.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            intent?.let { updateBattery(it) }
        }
    }

    private var isRegistered = false

    fun start() {
        if (!isRegistered) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val sticky = context.registerReceiver(batteryReceiver, filter)
            sticky?.let { updateBattery(it) }
            isRegistered = true
        }
        refreshMemory()
    }

    fun stop() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(batteryReceiver)
            } catch (_: Exception) {}
            isRegistered = false
        }
    }

    fun refreshMemory(): MemoryStats {
        val snapshot = getMemorySnapshot()
        _memoryStats.value = snapshot
        return snapshot
    }

    private fun getMemorySnapshot(): MemoryStats {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedMb = (totalMb - availMb).coerceAtLeast(0)
        val usedPct = if (totalMb > 0) ((usedMb.toDouble() / totalMb) * 100).toInt() else 50

        return MemoryStats(
            totalMb = totalMb,
            availableMb = availMb,
            usedMb = usedMb,
            usedPercent = usedPct.coerceIn(0, 100),
            isLowMemory = memInfo.lowMemory
        )
    }

    private fun updateBattery(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val levelPct = if (level >= 0 && scale > 0) (level * 100) / scale else 80

        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 330)
        val tempC = tempTenths / 10.0f

        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val statusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Optimal"
        }

        val isPowerSave = powerManager.isPowerSaveMode

        _batteryStats.value = BatteryStats(
            levelPercent = levelPct,
            temperatureCelsius = tempC,
            statusText = statusStr,
            isPowerSaveMode = isPowerSave,
            voltageMv = voltage
        )
    }
}
