package com.abdulrhman.mowatib

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

import com.abdulrhman.mowatib.crash.CrashHandler
import com.abdulrhman.mowatib.crash.SafeMode
import com.abdulrhman.mowatib.data.local.database.MowatibDatabase
import com.abdulrhman.mowatib.data.local.database.DbLogBuffer
import com.abdulrhman.mowatib.data.repository.ShieldRepository
import com.abdulrhman.mowatib.data.preferences.UserPreferencesRepository
import com.abdulrhman.mowatib.ui.widget.AppStreakWidget
import com.abdulrhman.mowatib.ui.widget.GlobalStreakWidget
import com.abdulrhman.mowatib.ui.widget.TotalScreenTimeWidget
import com.abdulrhman.mowatib.ui.widget.RemainingTargetWidget
import com.abdulrhman.mowatib.ui.widget.PhoneFreeTimeWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Mowatib (مواظب) Application Context
 * Material Design 3 Expressive Digital Wellbeing & Focus Platform
 */
open class MowatibApplication : Application(), ImageLoaderFactory {

    private var lastUiMode: Int = 0
    private var startedActivityCount = 0
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        private var workersEnqueued = false
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }

    val shieldRepository: ShieldRepository by lazy {
        try {
            DbLogBuffer.d("MowatibDB", "Initializing database...")
            val database = MowatibDatabase.getDatabase(this)
            MowatibDatabase.verifyDatabase(database)
            ShieldRepository(
                database.shieldDao(),
                database.scheduleDao(),
                database.dailyUsageDao(),
                database.hourlyUsageDao(),
                database.websiteUsageDao(),
                database,
                userPreferencesRepository
            )
        } catch (e: Exception) {
            DbLogBuffer.e("MowatibDB", "DB_INIT_FAILED: ${e.message} - attempting destructive rebuild")
            MowatibDatabase.closeDatabase()
            DbLogBuffer.d("MowatibDB", "Rebuilding database after failure...")
            val database = MowatibDatabase.getDatabase(this)
            MowatibDatabase.verifyDatabase(database)
            DbLogBuffer.w("MowatibDB", "DB_REBUILT: Previous data may be lost after destructive rebuild")
            ShieldRepository(
                database.shieldDao(),
                database.scheduleDao(),
                database.dailyUsageDao(),
                database.hourlyUsageDao(),
                database.websiteUsageDao(),
                database,
                userPreferencesRepository
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        CrashHandler.install(this)
        com.abdulrhman.mowatib.data.local.MowatibTaskManager.init(this)
        com.abdulrhman.mowatib.data.local.MowatibHabitsManager.init(this)
        com.abdulrhman.mowatib.data.manager.MowatibSyncManager.init(this)

        lastUiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                startedActivityCount++
                com.abdulrhman.mowatib.service.AppStateHolder.isAppVisible = true
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
                com.abdulrhman.mowatib.service.AppStateHolder.isAppVisible = startedActivityCount > 0
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        if (SafeMode.isEnabled(this)) {
            DbLogBuffer.w(
                "MowatibApp",
                "SAFE_MODE: booting without user data; workers and monitoring stay off"
            )
            return
        }
        applicationScope.launch(Dispatchers.IO) {
            try {
                shieldRepository
            } catch (_: Exception) {
            }
        }

        if (!workersEnqueued) {
            workersEnqueued = true
            applicationScope.launch {
                try {
                    val prefs = userPreferencesRepository.userPreferencesFlow.first()
                    com.abdulrhman.mowatib.service.UsageSyncWorker.enqueue(this@MowatibApplication)
                    com.abdulrhman.mowatib.worker.StreakRefreshWorker.enqueue(this@MowatibApplication)
                    if (prefs.dailyRecapEnabled) {
                        com.abdulrhman.mowatib.worker.NotificationInsightsWorker.scheduleDailyRecap(this@MowatibApplication)
                    }
                    if (prefs.weeklyInsightEnabled) {
                        com.abdulrhman.mowatib.worker.NotificationInsightsWorker.scheduleWeeklyInsight(this@MowatibApplication)
                    }
                    if (prefs.alarmMasterEnabled) {
                        com.abdulrhman.mowatib.worker.AlarmWatchdogWorker.enqueue(this@MowatibApplication)
                    }
                } catch (_: Exception) {
                    try { com.abdulrhman.mowatib.service.UsageSyncWorker.enqueue(this@MowatibApplication) } catch (_: Exception) {}
                }
            }
        }

        applicationScope.launch {
            try {
                userPreferencesRepository.migrateAlarmIdsIfNeeded()
            } catch (_: Exception) {}
        }

        applicationScope.launch {
            try {
                val prefs = userPreferencesRepository.userPreferencesFlow.first()
                com.abdulrhman.mowatib.service.DndStateManager.reconcileWithPrefs(
                    this@MowatibApplication,
                    prefs.bedtimeEnabled,
                    prefs.bedtimeDndEnabled,
                    prefs.bedtimeStartTime,
                    prefs.bedtimeEndTime,
                    prefs.bedtimeDays
                )
            } catch (_: Exception) {}
        }

        applicationScope.launch {
            try {
                kotlinx.coroutines.delay(1000)
                AppStreakWidget().updateAll(this@MowatibApplication)
                GlobalStreakWidget().updateAll(this@MowatibApplication)
                TotalScreenTimeWidget().updateAll(this@MowatibApplication)
                RemainingTargetWidget().updateAll(this@MowatibApplication)
                PhoneFreeTimeWidget().updateAll(this@MowatibApplication)
            } catch (_: Exception) {}
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(com.abdulrhman.mowatib.util.coil.AppIconMapper())
                add(com.abdulrhman.mowatib.util.coil.AppIconFetcher.Factory(this@MowatibApplication))
                add(com.abdulrhman.mowatib.util.coil.FaviconMapper())
                add(com.abdulrhman.mowatib.util.coil.FaviconFetcher.Factory(this@MowatibApplication))
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.1)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            coil.Coil.imageLoader(this).memoryCache?.clear()
            try { TotalScreenTimeWidget.clearCache() } catch (_: Exception) {}
            try { RemainingTargetWidget.clearCache() } catch (_: Exception) {}
            try { PhoneFreeTimeWidget.clearCache() } catch (_: Exception) {}
            try { GlobalStreakWidget.clearCache() } catch (_: Exception) {}
            try { AppStreakWidget.clearCache() } catch (_: Exception) {}
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val newUiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (newUiMode != lastUiMode) {
            lastUiMode = newUiMode
            applicationScope.launch {
                try {
                    kotlinx.coroutines.delay(300)
                    AppStreakWidget().updateAll(this@MowatibApplication)
                    GlobalStreakWidget().updateAll(this@MowatibApplication)
                    TotalScreenTimeWidget().updateAll(this@MowatibApplication)
                    RemainingTargetWidget().updateAll(this@MowatibApplication)
                    PhoneFreeTimeWidget().updateAll(this@MowatibApplication)
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}

/**
 * Backward compatibility alias for legacy components & workers
 */
