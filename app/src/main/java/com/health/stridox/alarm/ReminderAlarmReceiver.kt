package com.health.stridox.alarm

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Reminder"
        val note = intent.getStringExtra(AlarmScheduler.EXTRA_NOTE) ?: ""
        val date = intent.getStringExtra(AlarmScheduler.EXTRA_DATE) ?: ""
        val time = intent.getStringExtra(AlarmScheduler.EXTRA_TIME) ?: ""

        NotificationUtils.createNotificationChannel(context)
        val notification = NotificationUtils.buildNotification(
            context = context,
            id = reminderId.toInt(),
            title = title,
            body = note.ifBlank { "Time: $time" }
        )
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(reminderId.toInt(), notification)

        // Reschedule next day for same time (so it becomes a daily alarm)
        if (reminderId != -1L && date.isNotBlank() && time.isNotBlank()) {
            AlarmScheduler.scheduleDailyAlarm(context, reminderId, title, note, date, time)
        }
    }
}
