package com.health.stridox.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.health.stridox.data.dao.IntakeReminderDao

@Database(
    entities = [IntakeReminder::class],
    version = 1
)

abstract class StridoxDatabase : RoomDatabase() {
    abstract fun intakeReminderDao(): IntakeReminderDao
}