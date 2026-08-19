package com.health.stridox.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

object AlarmScheduler {
    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_NOTE = "extra_note"
    const val EXTRA_DATE = "extra_date"
    const val EXTRA_TIME = "extra_time"
    private const val TAG = "AlarmScheduler"

    private fun buildPendingIntent(
        context: Context,
        reminderId: Long,
        title: String,
        note: String,
        date: String,
        time: String
    ): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_NOTE, note)
            putExtra(EXTRA_DATE, date)
            putExtra(EXTRA_TIME, time)
        }
        // Unique requestCode by reminderId (fits into Int)
        val requestCode = (reminderId and 0x7FFFFFFF).toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleDailyAlarm(
        context: Context,
        reminderId: Long,
        title: String,
        note: String,
        date: String,
        hhmm: String
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val parts = hhmm.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        // Parse the date string
        val localDate = try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $date", e)
            LocalDate.now() // Fallback to current date
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.YEAR, localDate.year)
            set(Calendar.MONTH, localDate.monthValue - 1) // Calendar.MONTH is 0-indexed
            set(Calendar.DAY_OF_MONTH, localDate.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        // If time already passed for today, schedule for tomorrow
        // Only advance if the reminder date is today or in the past.
        // If the reminder date is in the future, we schedule for that date directly.
        val now = System.currentTimeMillis()
        if (calendar.timeInMillis <= now) {
            // Check if the reminder is for today and the time has passed
            val reminderIsToday = localDate.isEqual(LocalDate.now())
            if (reminderIsToday && calendar.timeInMillis <= now) {
                // If it's today and time passed, schedule for next day
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            } else if (!reminderIsToday) {
                // If the reminder date is in the past, schedule for the next day after the *current* date.
                // This handles cases where a past reminder is re-enabled.
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (today.timeInMillis <= now) {
                    today.add(Calendar.DAY_OF_YEAR, 1)
                }
                calendar.timeInMillis = today.timeInMillis
            }
        }


        val pi = buildPendingIntent(context, reminderId, title, note, date, hhmm)

        // --- FIX START: Check permission before using setExact ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                // We have permission, set exact alarm
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pi
                )
            } else {
                Log.w(TAG, "Exact alarm permission missing. Falling back to standard alarm.")
                am.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pi
                )
            }
        } else {
            // Old Android versions don't need special permission
            am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pi
            )
        }
    }

    fun cancelAlarm(context: Context, reminderId: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // title/note/date/time not needed to cancel, but we use placeholders to get same PendingIntent
        val pi = buildPendingIntent(context, reminderId, "", "", "", "")
        am.cancel(pi)
        pi.cancel()
    }
}
