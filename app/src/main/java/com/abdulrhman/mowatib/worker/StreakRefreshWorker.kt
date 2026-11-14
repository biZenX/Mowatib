package com.abdulrhman.mowatib.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.abdulrhman.mowatib.MowatibApplication
import com.abdulrhman.mowatib.ui.widget.AppStreakWidget
import com.abdulrhman.mowatib.ui.widget.GlobalStreakWidget
import com.abdulrhman.mowatib.ui.widget.PhoneFreeTimeWidget
import com.abdulrhman.mowatib.ui.widget.RemainingTargetWidget
import com.abdulrhman.mowatib.ui.widget.TotalScreenTimeWidget
import java.util.concurrent.TimeUnit

class StreakRefreshWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (com.abdulrhman.mowatib.crash.SafeMode.isEnabled(applicationContext)) return Result.success()
        return try {
            val app = applicationContext as MowatibApplication
            app.userPreferencesRepository.refreshGlobalStreak(app.shieldRepository)
            app.userPreferencesRepository.refreshAppStreaks(app.shieldRepository)
            app.userPreferencesRepository.refreshWebStreaks(app.shieldRepository)
            GlobalStreakWidget().updateAll(applicationContext)
            AppStreakWidget().updateAll(applicationContext)
            TotalScreenTimeWidget().updateAll(applicationContext)
            RemainingTargetWidget().updateAll(applicationContext)
            PhoneFreeTimeWidget().updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<StreakRefreshWorker>(6, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "StreakRefreshWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
