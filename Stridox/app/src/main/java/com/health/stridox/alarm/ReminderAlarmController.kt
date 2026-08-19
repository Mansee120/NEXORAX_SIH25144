package com.health.stridox.alarm

import android.content.Context

class ReminderAlarmController(
    private val context: Context  // this will be ApplicationContext via Koin
) {

    fun schedule(reminderId: Long, title: String, note: String, date: String, time: String) {
        AlarmScheduler.scheduleDailyAlarm(context, reminderId, title, note, date, time)
    }

    fun cancel(reminderId: Long) {
        AlarmScheduler.cancelAlarm(context, reminderId)
    }
}
