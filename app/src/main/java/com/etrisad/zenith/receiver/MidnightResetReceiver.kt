package com.etrisad.zenith.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.etrisad.zenith.worker.MidnightResetWorker

class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_START_MIDNIGHT_RESET) {
            val workRequest = OneTimeWorkRequestBuilder<MidnightResetWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    companion object {
        const val ACTION_START_MIDNIGHT_RESET = "com.etrisad.zenith.action.MIDNIGHT_RESET"
    }
}
