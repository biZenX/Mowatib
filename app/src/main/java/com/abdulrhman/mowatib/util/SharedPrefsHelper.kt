package com.abdulrhman.mowatib.util

import android.content.Context
import com.abdulrhman.mowatib.data.preferences.UserPreferencesRepository

object SharedPrefsHelper {
    suspend fun setShortsScreenTimeMs(context: Context, ms: Long) {
        UserPreferencesRepository(context).setShortsScreenTimeMs(ms)
    }
}
