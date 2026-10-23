package com.etrisad.zenith.util

import android.content.Context
import com.etrisad.zenith.data.preferences.UserPreferencesRepository

object SharedPrefsHelper {
    suspend fun setShortsScreenTimeMs(context: Context, ms: Long) {
        UserPreferencesRepository(context).setShortsScreenTimeMs(ms)
    }
}
