package com.example.waterreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {

    const val PREFS = "water_reminder_prefs"
    const val KEY_ENABLED = "enabled"

    // Reminder window: 08:00 to 17:00 inclusive, every 30 minutes.
    private const val START_HOUR = 8
    private const val END_HOUR = 17
    private const val STEP_MINUTES = 30

    /** Returns the list of (hour, minute) slots for the day, e.g. 08:00, 08:30, ..., 17:00 */
    fun buildSlots(): List<Pair<Int, Int>> {
        val slots = mutableListOf<Pair<Int, Int>>()
        var totalMin = START_HOUR * 60
        val endMin = END_HOUR * 60
        while (totalMin <= endMin) {
            slots.add(Pair(totalMin / 60, totalMin % 60))
            totalMin += STEP_MINUTES
        }
        return slots
    }

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun canScheduleExact(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } else true
    }

    /** Schedules every remaining slot for today, plus a rollover alarm that re-schedules tomorrow. */
    fun scheduleAll(context: Context) {
        if (!isEnabled(context)) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Calendar.getInstance()

        buildSlots().forEachIndexed { index, (hour, minute) ->
            val trigger = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // Skip slots already passed today; they'll naturally resume tomorrow.
            if (trigger.before(now)) return@forEachIndexed

            val intent = Intent(context, AlarmReceiver::class.java).putExtra("slot", "%02d:%02d".format(hour, minute))
            val pendingIntent = PendingIntent.getBroadcast(
                context, index, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setExact(am, trigger.timeInMillis, pendingIntent)
        }

        scheduleDailyRollover(context)
    }

    /** A daily alarm at 00:05 that re-runs scheduleAll for the new day. */
    private fun scheduleDailyRollover(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val trigger = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, if (get(Calendar.HOUR_OF_DAY) >= 0 && get(Calendar.MINUTE) >= 5) 1 else 0)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
        }
        val intent = Intent(context, BootReceiver::class.java).setAction(ACTION_ROLLOVER)
        val pendingIntent = PendingIntent.getBroadcast(
            context, ROLLOVER_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(am, trigger.timeInMillis, pendingIntent)
    }

    private fun setExact(am: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /** Cancels every scheduled reminder alarm for today. */
    fun cancelAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        buildSlots().forEachIndexed { index, _ ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, index, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pendingIntent)
        }
        val rolloverIntent = Intent(context, BootReceiver::class.java).setAction(ACTION_ROLLOVER)
        val rolloverPending = PendingIntent.getBroadcast(
            context, ROLLOVER_REQUEST_CODE, rolloverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(rolloverPending)
    }

    const val ACTION_ROLLOVER = "com.example.waterreminder.ACTION_ROLLOVER"
    private const val ROLLOVER_REQUEST_CODE = 9999
}
