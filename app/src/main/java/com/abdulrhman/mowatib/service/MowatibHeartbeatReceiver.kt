package com.abdulrhman.mowatib.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

open class MowatibHeartbeatReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (com.abdulrhman.mowatib.crash.SafeMode.isEnabled(context)) return

        val action = intent.action ?: return
        
        val validActions = setOf(
            "com.abdulrhman.mowatib.action.HEARTBEAT",
            "com.abdulrhman.mowatib.action.REFRESH_SERVICES",
            "com.abdulrhman.mowatib.action.SCREEN_OFF_GOAL_CHECK",
            "com.abdulrhman.mowatib.action.TEST_GOAL_CALLER",
            "com.abdulrhman.mowatib.action.TEST_GOAL_CALLER_FIRE",
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )

        if (action in validActions) {
            val needsGate = action == Intent.ACTION_BOOT_COMPLETED ||
                Intent.ACTION_MY_PACKAGE_REPLACED == action ||
                action == "com.abdulrhman.mowatib.action.HEARTBEAT"
            if (needsGate) {
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val prefsRepo = com.abdulrhman.mowatib.data.preferences.UserPreferencesRepository(context.applicationContext)
                        val prefs = withTimeoutOrNull(3000) {
                            prefsRepo.userPreferencesFlow.first()
                        }
                        val needsService = prefs == null || prefs.alarmMasterEnabled ||
                            prefs.bedtimeEnabled || prefs.eyeCareEnabled ||
                            prefs.usageGlimpseEnabled || prefs.pomodoroEnabled
                        if (needsService) startMonitorLocked(context, action)
                    } catch (e: Exception) {
                        try { startMonitorLocked(context, action) } catch (_: Exception) {}
                    } finally {
                        try { pending.finish() } catch (_: Exception) {}
                    }
                }
                return
            }
            try {
                startMonitorLocked(context, action)
            } catch (e: Exception) {
            }
        }
    }

    private fun startMonitorLocked(context: Context, action: String) {
        val mappedAction = when (action) {
            "com.abdulrhman.mowatib.action.REFRESH_SERVICES" -> "com.abdulrhman.mowatib.action.REFRESH_DATA"
            "com.abdulrhman.mowatib.action.SCREEN_OFF_GOAL_CHECK" -> "com.abdulrhman.mowatib.action.SCREEN_OFF_GOAL_CHECK"
            "com.abdulrhman.mowatib.action.TEST_GOAL_CALLER" -> "com.abdulrhman.mowatib.action.TEST_GOAL_CALLER"
            "com.abdulrhman.mowatib.action.TEST_GOAL_CALLER_FIRE" -> "com.abdulrhman.mowatib.action.TEST_GOAL_CALLER_FIRE"
            else -> "com.abdulrhman.mowatib.action.HEARTBEAT"
        }
        AppUsageMonitorService.startMonitorService(context.applicationContext, mappedAction)
        if (MowatibService.isServiceRunning) {
            val accessIntent = Intent(context, MowatibService::class.java).apply {
                this.action = "com.abdulrhman.mowatib.action.REFRESH_DATA"
            }
            try {
                context.startService(accessIntent)
            } catch (_: Exception) {}
        }
    }
}

/**
 * Backward compatibility alias for heartbeat receiver
 */
