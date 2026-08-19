package com.health.stridox.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.health.stridox.data.repo.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderDoneReceiver : BroadcastReceiver(), KoinComponent {

    private val repo: ReminderRepository by inject()
    private val alarmController: ReminderAlarmController by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        if (reminderId == -1L) return

        NotificationManagerCompat.from(context).cancel(reminderId.toInt())
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = repo.getAllReminders()
                val r = reminders.firstOrNull { it.id == reminderId } ?: return@launch
                alarmController.cancel(reminderId) // Cancel existing alarm
                alarmController.schedule(
                    reminderId,
                    r.title,
                    r.note,
                    r.date,
                    r.time
                ) // Reschedule with updated reminder details including date

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
