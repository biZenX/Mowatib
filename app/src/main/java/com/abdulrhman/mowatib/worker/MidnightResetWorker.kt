package com.abdulrhman.mowatib.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.abdulrhman.mowatib.data.preferences.UserPreferencesRepository
import com.abdulrhman.mowatib.util.AlarmTasksSchedulingHelper
import com.abdulrhman.mowatib.util.SharedPrefsHelper
import androidx.glance.appwidget.updateAll
import com.abdulrhman.mowatib.ui.widget.GlobalStreakWidget
import com.abdulrhman.mowatib.ui.widget.AppStreakWidget
import com.abdulrhman.mowatib.ui.widget.TotalScreenTimeWidget
import com.abdulrhman.mowatib.ui.widget.RemainingTargetWidget
import com.abdulrhman.mowatib.ui.widget.PhoneFreeTimeWidget
import kotlinx.coroutines.flow.first

class MidnightResetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (com.abdulrhman.mowatib.crash.SafeMode.isEnabled(applicationContext)) return Result.success()
        SharedPrefsHelper.setShortsScreenTimeMs(applicationContext, 0L)

        val prefsRepo = UserPreferencesRepository(applicationContext)
        val prefs = prefsRepo.userPreferencesFlow.first()
        val needsService = prefs.alarmMasterEnabled || prefs.bedtimeEnabled ||
            prefs.eyeCareEnabled || prefs.usageGlimpseEnabled || prefs.pomodoroEnabled
        if (needsService) {
            com.abdulrhman.mowatib.service.AppUsageMonitorService.startMonitorService(
                applicationContext,
                ACTION_MIDNIGHT_RESET_SERVICE
            )
        }
        if (needsService) {
            AlarmTasksSchedulingHelper.scheduleMidnightResetTask(applicationContext, prefs.dayStartHour, prefs.dayStartMinute)
        }
        
        GlobalStreakWidget().updateAll(applicationContext)
        AppStreakWidget().updateAll(applicationContext)
        TotalScreenTimeWidget().updateAll(applicationContext)
        RemainingTargetWidget().updateAll(applicationContext)
        PhoneFreeTimeWidget().updateAll(applicationContext)
        
        return Result.success()
    }

    companion object {
        const val ACTION_MIDNIGHT_RESET_SERVICE = "com.abdulrhman.mowatib.action.MIDNIGHT_RESET_SERVICE"
    }
}
