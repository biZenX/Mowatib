package com.etrisad.zenith

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

import com.etrisad.zenith.crash.CrashHandler
import com.etrisad.zenith.crash.SafeMode
import com.etrisad.zenith.data.local.database.ZenithDatabase
import com.etrisad.zenith.data.local.database.DbLogBuffer
import com.etrisad.zenith.data.repository.ShieldRepository
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import com.etrisad.zenith.ui.widget.AppStreakWidget
import com.etrisad.zenith.ui.widget.GlobalStreakWidget
import com.etrisad.zenith.ui.widget.TotalScreenTimeWidget
import com.etrisad.zenith.ui.widget.RemainingTargetWidget
import com.etrisad.zenith.ui.widget.PhoneFreeTimeWidget
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
            val database = ZenithDatabase.getDatabase(this)
            ZenithDatabase.verifyDatabase(database)
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
            ZenithDatabase.closeDatabase()
            DbLogBuffer.d("MowatibDB", "Rebuilding database after failure...")
            val database = ZenithDatabase.getDatabase(this)
            ZenithDatabase.verifyDatabase(database)
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
        com.etrisad.zenith.data.local.MowatibTaskManager.init(this)
        com.etrisad.zenith.data.local.MowatibHabitsManager.init(this)
        com.etrisad.zenith.data.manager.MowatibSyncManager.init(this)

        lastUiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                startedActivityCount++
                com.etrisad.zenith.service.AppStateHolder.isAppVisible = true
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
                com.etrisad.zenith.service.AppStateHolder.isAppVisible = startedActivityCount > 0
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        if (SafeMode.isEnabled(this)) {
            DbLogBuffer.w(
                "ZenithApp",
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
                    com.etrisad.zenith.service.UsageSyncWorker.enqueue(this@MowatibApplication)
                    com.etrisad.zenith.worker.StreakRefreshWorker.enqueue(this@MowatibApplication)
                    if (prefs.dailyRecapEnabled) {
                        com.etrisad.zenith.worker.NotificationInsightsWorker.scheduleDailyRecap(this@MowatibApplication)
                    }
                    if (prefs.weeklyInsightEnabled) {
                        com.etrisad.zenith.worker.NotificationInsightsWorker.scheduleWeeklyInsight(this@MowatibApplication)
                    }
                    if (prefs.alarmMasterEnabled) {
                        com.etrisad.zenith.worker.AlarmWatchdogWorker.enqueue(this@MowatibApplication)
                    }
                } catch (_: Exception) {
                    try { com.etrisad.zenith.service.UsageSyncWorker.enqueue(this@MowatibApplication) } catch (_: Exception) {}
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
                com.etrisad.zenith.service.DndStateManager.reconcileWithPrefs(
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
                add(com.etrisad.zenith.util.coil.AppIconMapper())
                add(com.etrisad.zenith.util.coil.AppIconFetcher.Factory(this@MowatibApplication))
                add(com.etrisad.zenith.util.coil.FaviconMapper())
                add(com.etrisad.zenith.util.coil.FaviconFetcher.Factory(this@MowatibApplication))
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
typealias ZenithApplication = MowatibApplication
