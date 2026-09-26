package com.example.waterreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == ReminderScheduler.ACTION_ROLLOVER) {
            if (ReminderScheduler.isEnabled(context)) {
                ReminderScheduler.scheduleAll(context)
            }
        }
    }
}
